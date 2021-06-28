/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.grpc_api_client.table;

import com.google.common.annotations.VisibleForTesting;
import gnu.trove.list.TLongList;
import gnu.trove.list.linked.TLongLinkedList;
import io.deephaven.base.verify.Assert;
import io.deephaven.configuration.Configuration;
import io.deephaven.db.tables.ColumnDefinition;
import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.TableDefinition;
import io.deephaven.db.tables.live.LiveTable;
import io.deephaven.db.tables.live.LiveTableMonitor;
import io.deephaven.db.tables.live.LiveTableRegistrar;
import io.deephaven.db.tables.live.NotificationQueue;
import io.deephaven.db.tables.utils.DBDateTime;
import io.deephaven.db.tables.utils.TableTools;
import io.deephaven.db.v2.QueryTable;
import io.deephaven.db.v2.ShiftAwareListener;
import io.deephaven.db.v2.sources.ArrayBackedColumnSource;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.LogicalClock;
import io.deephaven.db.v2.sources.RedirectedColumnSource;
import io.deephaven.db.v2.sources.WritableChunkSink;
import io.deephaven.db.v2.sources.WritableSource;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.ChunkType;
import io.deephaven.db.v2.sources.chunk.WritableLongChunk;
import io.deephaven.db.v2.utils.BarrageMessage;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.RedirectionIndex;
import io.deephaven.db.v2.utils.UpdatePerformanceTracker;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.log.LogEntry;
import io.deephaven.io.log.LogLevel;
import io.deephaven.io.logger.Logger;
import io.deephaven.util.MultiException;
import io.deephaven.util.SafeCloseableList;
import io.deephaven.util.annotations.InternalUseOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;

/**
 * A client side viewport of a server side {@link Table}.
 *
 * Note that in this case <b>viewport</b> is defined as a set of positions into the original table.
 */
public class BarrageSourcedTable extends QueryTable implements LiveTable, BarrageMessage.Listener {

    private static final boolean REQUEST_LIVE_TABLE_MONITOR_REFRESH = Configuration.getInstance().getBooleanWithDefault("BarrageSourcedTable.requestLiveTableMonitorRefresh", true);
    public static final boolean REPLICATED_TABLE_DEBUG = Configuration.getInstance().getBooleanWithDefault("BarrageSourcedTable.debug", false);

    private static final Logger log = LoggerFactory.getLogger(BarrageSourcedTable.class);

    private final LiveTableRegistrar registrar;
    private final NotificationQueue notificationQueue;

    private final UpdatePerformanceTracker.Entry refreshEntry;

    /** the capacity that the destSources been set to */
    private int capacity = 0;
    /** the reinterpretted destination writable sources */
    private final WritableSource<?>[] destSources;
    /** we compact the parent table's key-space and instead redirect; ideal for viewport */
    private final RedirectionIndex redirectionIndex;
    /** represents which rows in writable source exist but are not mapped to any parent rows */
    private Index freeset = Index.FACTORY.getEmptyIndex();


    /** unsubscribed must never be reset to false once it has been set to true */
    private volatile boolean unsubscribed = false;
    /** indicates whether or not this table should immediately stop propagating updates */
    private volatile boolean frozen = false;
    private final boolean isViewPort;

    /**
     * The client and the server run in two different processes. The client requests a viewport, and when convenient,
     * the server will send the client the snapshot for the request and continue to send data that is inside of that view.
     * Due to the asynchronous aspect of this protocol, the client may have multiple requests in-flight and the server
     * may choose to honor the most recent request and assumes that the client no longer wants earlier but unacked viewport
     * changes.
     *
     * The server notifies the client which viewport it is respecting by including it inside of each snapshot. Note
     * that the server assumes that the client has maintained its state prior to these server-side viewport acks and will
     * not re-send data that the client should already have within the existing viewport.
     */
    private Index serverViewport;
    private BitSet serverColumns;

    // the Index of rows that we have indicated should be freed; but we can not free until our next refresh cycle
    private Index pendingFree = null;

    private final ConcurrentLinkedQueue<Throwable> errorQueue = new ConcurrentLinkedQueue<>();

    private volatile long processedDelta = -1;

    /** synchronize access to pendingUpdates */
    private final Object pendingUpdatesLock = new Object();

    /** accumulate pending updates until we refresh this LiveTable */
    private ArrayDeque<BarrageMessage> pendingUpdates = new ArrayDeque<>();

    /** alternative pendingUpdates container to avoid allocating, and resizing, a new instance */
    private ArrayDeque<BarrageMessage> shadowPendingUpdates = new ArrayDeque<>();

    private final List<Object> processedData;
    private final TLongList processedStep;

    protected BarrageSourcedTable(final LiveTableRegistrar registrar,
                                  final NotificationQueue notificationQueue,
                                  final LinkedHashMap<String, ColumnSource<?>> columns,
                                  final WritableSource<?>[] writableSources,
                                  final RedirectionIndex redirectionIndex,
                                  @Nullable final BitSet subscribedColumns,
                                  final boolean isViewPort) {
        super(Index.FACTORY.getEmptyIndex(), columns); //, writableSources, redirectionIndex, subscribedColumns);
        this.registrar = registrar;
        this.notificationQueue = notificationQueue;

        this.redirectionIndex = redirectionIndex;
        this.refreshEntry = UpdatePerformanceTracker.getInstance().getEntry("BarrageSourcedTable refresh " + System.identityHashCode(this));

        this.isViewPort = isViewPort;
        if (isViewPort) {
            serverViewport = Index.CURRENT_FACTORY.getEmptyIndex();
        } else {
            serverViewport = null;
        }

        this.destSources = new WritableSource<?>[writableSources.length];
        for (int ii = 0; ii < writableSources.length; ++ii) {
            WritableSource<?> source = writableSources[ii];
            final Class<?> columnType = source.getType();

            if (columnType == DBDateTime.class) {
                source = (WritableSource<?>) source.reinterpret(long.class);
            } else if (columnType == boolean.class || columnType == Boolean.class) {
                source = (WritableSource<?>) source.reinterpret(byte.class);
            }

            destSources[ii] = source;
        }

        // we always start empty, and can be notified this cycle if we are refreshed
        final long currentClockValue = LogicalClock.DEFAULT.currentValue();
        setLastNotificationStep(LogicalClock.getState(currentClockValue) == LogicalClock.State.Updating
                ? LogicalClock.getStep(currentClockValue) - 1
                : LogicalClock.getStep(currentClockValue));

        registrar.addTable(this);

        setAttribute(Table.DO_NOT_MAKE_REMOTE_ATTRIBUTE, true);

        if (REPLICATED_TABLE_DEBUG) {
            processedData = new LinkedList<>();
            processedStep = new TLongLinkedList();
        } else {
            processedData = null;
            processedStep = null;
        }
    }

    public long getProcessedDelta() {
        return processedDelta;
    }

    public ChunkType[] getWireChunkTypes() {
        return Arrays.stream(destSources).map(s -> ChunkType.fromElementType(s.getType())).toArray(ChunkType[]::new);
    }

    public Class<?>[] getWireTypes() {
        return Arrays.stream(destSources).map(ColumnSource::getType).toArray(Class<?>[]::new);
    }

    public Class<?>[] getWireComponentTypes() {
        return Arrays.stream(destSources).map(ColumnSource::getComponentType).toArray(Class<?>[]::new);
    }

    /**
     * Invoke sealTable to prevent further updates from being processed and to mark this source table as static.
     */
    public synchronized void sealTable() {
        setRefreshing(false);
        unsubscribed = true;
    }

    @Override
    public void handleBarrageMessage(final BarrageMessage update) {
        if (unsubscribed) {
            beginLog(LogLevel.INFO).append(": Discarding update for unsubscribed table!").endl();
            return;
        }

        synchronized (pendingUpdatesLock) {
            pendingUpdates.add(update.clone());
        }
        doWakeup();
    }

    @Override
    public void handleBarrageError(Throwable t) {
        errorQueue.add(t);
        doWakeup();
    }

    private Index.IndexUpdateCoalescer processUpdate(final BarrageMessage update, final Index.IndexUpdateCoalescer coalescer) {
        if (REPLICATED_TABLE_DEBUG) {
            saveForDebugging(update);
            beginLog(LogLevel.INFO).append(": Processing delta updates ")
                    .append(update.firstSeq).append("-").append(update.lastSeq).endl();
        }

        if (update.isSnapshot) {
            serverViewport = update.snapshotIndex == null ? null : update.snapshotIndex.clone();
            serverColumns = update.snapshotColumns == null ? null : (BitSet) update.snapshotColumns.clone();
        }

        // make sure that these index updates make some sense compared with each other, and our current view of the table
        final Index currentIndex = getIndex();
        final boolean initialSnapshot = currentIndex.empty() && update.isSnapshot;
        currentIndex.remove(update.rowsRemoved);

        try (final SafeCloseableList ignored = new SafeCloseableList();
             // this is remote-LTMs index prior to this update
             final Index preShiftPostRemoveIndex = currentIndex.clone();
             final Index populatedRows = (serverViewport != null ? currentIndex.subindexByPos(serverViewport) : currentIndex.clone())) {

            update.shifted.apply(currentIndex);
            currentIndex.insert(update.rowsAdded);

            if (REPLICATED_TABLE_DEBUG) {
                // the included modifications must be a subset of the actual modifications
                for (int i = 0; i < update.modColumnData.length; ++i) {
                    final BarrageMessage.ModColumnData column = update.modColumnData[i];
                    Assert.assertion(column.rowsIncluded.subsetOf(column.rowsModified),
                            "column.rowsIncluded.subsetOf(column.rowsModified)");
                }
            }

            // removes
            freeRows(update.rowsRemoved);

            // shifts
            if (update.shifted.nonempty()) {
                redirectionIndex.applyShift(preShiftPostRemoveIndex, update.shifted);
            }

            final Index totalMods = Index.FACTORY.getEmptyIndex();
            final Index includedMods = Index.FACTORY.getEmptyIndex();
            for (int i = 0; i < update.modColumnData.length; ++i) {
                final BarrageMessage.ModColumnData column = update.modColumnData[i];
                totalMods.insert(column.rowsModified);
                includedMods.insert(column.rowsIncluded);
            }
            includedMods.retain(populatedRows);

            // post shift space removals due to mods outside of viewport
            if (serverViewport != null && totalMods.nonempty()) {
                try (final Index modsOutOfViewport = totalMods.minus(includedMods)) {
                    freeRows(modsOutOfViewport);
                }
            }

            if (serverViewport != null && update.rowsIncluded.nonempty()) {
                // if we are a viewport, we might have mappings for some of these additions as they include scoped rows

                try (final Index destinationIndex = getFreeRows(update.rowsIncluded.size());
                     final Index.Iterator destIter = destinationIndex.iterator();
                     final Index.Iterator outerIter = update.rowsIncluded.iterator();
                     final WritableLongChunk<Attributes.KeyIndices> keys = WritableLongChunk.makeWritableChunk(update.rowsIncluded.intSize())) {

                    int numNewKeys = 0;
                    for (int i = 0; i < keys.size(); ++i) {
                        final long outerKey = outerIter.nextLong();
                        long redirDest = redirectionIndex.get(outerKey);
                        if (redirDest == Index.NULL_KEY) {
                            ++numNewKeys;
                            redirDest = destIter.nextLong();
                            redirectionIndex.put(outerKey, redirDest);
                        }
                        keys.set(i, redirDest);
                    }

                    if (destinationIndex.intSize() < numNewKeys) {
                        // give back the unneeded free rows
                        freeset.insert(destinationIndex.subindexByPos(numNewKeys, destinationIndex.intSize() - 1));
                    }

                    // Update data in a fill-unordered manner:
                    for (int ii = 0; ii < update.addColumnData.length; ++ii) {
                        if (update.addColumnData[ii] != null && isSubscribedColumn(ii)) {
                            try (final WritableChunkSink.FillFromContext ctxt = destSources[ii].makeFillFromContext(keys.size())) {
                                destSources[ii].fillFromChunkUnordered(ctxt, update.addColumnData[ii].data, keys);
                            }
                        }
                    }
                }
            } else if (update.rowsIncluded.nonempty()) {
                try (final Index destinationIndex = getFreeRows((update.rowsIncluded.size()))) {
                    // Update redirection mapping:
                    final Index.Iterator destKeyIt = destinationIndex.iterator();
                    update.rowsIncluded.forAllLongs(realKey -> redirectionIndex.put(realKey, destKeyIt.nextLong()));

                    // Update data chunk-wise:
                    for (int ii = 0; ii < update.addColumnData.length; ++ii) {
                        if (isSubscribedColumn(ii)) {
                            final Chunk<? extends Attributes.Values> data = update.addColumnData[ii].data;
                            Assert.eq(data.size(), "delta.includedAdditions.size()", destinationIndex.size(), "destinationIndex.size()");
                            try (final WritableChunkSink.FillFromContext ctxt = destSources[ii].makeFillFromContext(destinationIndex.intSize())) {
                                destSources[ii].fillFromChunk(ctxt, data, destinationIndex);
                            }
                        }
                    }
                }
            }

            modifiedColumnSet.clear();
            for (int ii = 0; ii < update.modColumnData.length; ++ii) {
                final BarrageMessage.ModColumnData column = update.modColumnData[ii];
                if (!isSubscribedColumn(ii)) {
                    continue;
                }

                if (column.rowsIncluded.empty()) {
                    continue;
                }

                modifiedColumnSet.setColumnWithIndex(ii);

                try (final Index.Iterator outerIter = column.rowsIncluded.iterator();
                     final WritableLongChunk<Attributes.KeyIndices> keys = WritableLongChunk.makeWritableChunk(column.rowsIncluded.intSize())) {
                    for (int i = 0; i < keys.size(); ++i) {
                        final long outerKey = outerIter.nextLong();
                        keys.set(i, redirectionIndex.get(outerKey));
                        Assert.notEquals(keys.get(i), "keys[i]", Index.NULL_KEY, "Index.NULL_KEY");
                    }

                    try (final WritableChunkSink.FillFromContext ctxt = destSources[ii].makeFillFromContext(keys.size())) {
                        destSources[ii].fillFromChunkUnordered(ctxt, column.data, keys);
                    }
                }
            }

            processedDelta = update.lastSeq;

            if (!update.isSnapshot || initialSnapshot) {
                final ShiftAwareListener.Update downstream = new ShiftAwareListener.Update(
                        update.rowsAdded.clone(), update.rowsRemoved.clone(), totalMods, update.shifted, modifiedColumnSet);
                return (coalescer == null) ? new Index.IndexUpdateCoalescer(preShiftPostRemoveIndex, downstream) : coalescer.update(downstream);
            } else {
                return coalescer;
            }
        }
    }

    private boolean isSubscribedColumn(int i) {
        return serverColumns == null || serverColumns.get(i);
    }

    private Index getFreeRows(long size) {
        boolean needsResizing = false;
        if (capacity == 0) {
            capacity = Integer.highestOneBit((int) Math.max(size * 2, 8));
            freeset = Index.FACTORY.getFlatIndex(capacity);
            needsResizing = true;
        } else if (freeset.size() < size) {
            int allocatedSize = (int) (capacity - freeset.size());
            int prevCapacity = capacity;
            do {
                capacity *= 2;
            } while ((capacity - allocatedSize) < size);
            freeset.insertRange(prevCapacity, capacity - 1);
            needsResizing = true;
        }

        if (needsResizing) {
            for (final WritableSource<?> source : destSources) {
                source.ensureCapacity(capacity);
            }
        }

        final Index result = freeset.subindexByPos(0, (int) size);
        Assert.assertion(result.size() == size,"result.size() == size");
        freeset = freeset.subindexByPos((int) size, (int) freeset.size());
        return result;
    }

    private void freeRows(final Index removedIndex) {
        if (removedIndex.empty()) {
            return;
        }

        // full subscriptions free everything
        if (serverViewport == null) {
            doFreeRows(removedIndex);
            return;
        }

        // viewport subscriptions are currently
        try (final Index populatedRows = getIndex().subindexByPos(serverViewport);
             final Index rowsToFree = removedIndex.intersect(populatedRows)) {
            doFreeRows(rowsToFree);
        }
    }

    private void processPendingFrees() {
        if (pendingFree == null) {
            return;
        }
        doFreeRows(pendingFree);
        pendingFree.close();
        pendingFree = null;
    }

    private void doFreeRows(final Index rowsToFree) {
        // Note: these are NOT OrderedKeyIndices until after the call to .sort()
        final WritableLongChunk<Attributes.OrderedKeyIndices> redirectedRows
                = WritableLongChunk.makeWritableChunk(rowsToFree.intSize("BarrageSourcedTable"));
        redirectedRows.setSize(0);

        for (final Index.Iterator removedIt = rowsToFree.iterator(); removedIt.hasNext();) {
            final long next = removedIt.nextLong();
            final long prevIndex = redirectionIndex.remove(next);
            if (prevIndex == -1) {
                Assert.assertion(false, "prevIndex != -1", prevIndex, "prevIndex", next, "next");
            }
            redirectedRows.add(prevIndex);
        }

        redirectedRows.sort(); // now they're truly ordered
        freeset.insert(redirectedRows, 0, redirectedRows.size());
    }

    @Override
    public void refresh() {
        refreshEntry.onUpdateStart();
        try {
            realRefresh();
        } catch (Exception e) {
            beginLog(LogLevel.ERROR).append(": Failure during BarrageSourcedTable refresh: ").append(e).endl();
            notifyListenersOnError(e, null);
        } finally {
            refreshEntry.onUpdateEnd();
        }
    }

    private synchronized void realRefresh() {
        if (!errorQueue.isEmpty()) {
            Throwable t;
            final List<Throwable> enqueuedErrors = new ArrayList<>();
            while ((t = errorQueue.poll()) != null) {
                enqueuedErrors.add(t);
            }
            notifyListenersOnError(MultiException.maybeWrapInMultiException("BarrageSourcedTable errors", enqueuedErrors), null);
            // once we notify on error we are done, we can not notify any further, we are failed
            clearPendingData();
            return;
        }
        if (unsubscribed) {
            if (!frozen) {
                if (getIndex().nonempty()) {
                    final Index allRows = getIndex().clone();
                    getIndex().remove(allRows);
                    notifyListeners(Index.FACTORY.getEmptyIndex(), allRows, Index.FACTORY.getEmptyIndex());
                }
            }
            registrar.removeTable(this);
            clearPendingData();
            // we are quite certain the shadow copies should have been drained on the last refresh
            Assert.eqZero(shadowPendingUpdates.size(), "shadowPendingUpdates.size()");
            return;
        }

        // before doing any other work, we should get rid of rows that have been freed because of viewport updates,
        // but have not actually
        processPendingFrees();

        final ArrayDeque<BarrageMessage> localPendingUpdates;

        synchronized (pendingUpdatesLock) {
            localPendingUpdates = pendingUpdates;
            pendingUpdates = shadowPendingUpdates;
            shadowPendingUpdates = localPendingUpdates;

            // we should allow the next pass to start fresh, so we make sure that the queues were actually drained
            // on the last refresh
            Assert.eqZero(pendingUpdates.size(), "pendingUpdates.size()");
        }

        if (frozen) {
            synchronized (pendingUpdatesLock) {
                for (final BarrageMessage update : pendingUpdates) {
                    update.close();
                }
                pendingUpdates.clear();
            }
            localPendingUpdates.clear();
        }

        Index.IndexUpdateCoalescer coalescer = null;
        for (final BarrageMessage update : localPendingUpdates) {
            coalescer = processUpdate(update, coalescer);
            update.close();
        }
        localPendingUpdates.clear();

        if (coalescer != null) {
            notifyListeners(coalescer.coalesce());
        }
    }

    private void clearPendingData() {
        synchronized (pendingUpdatesLock) {
            // release any pending snapshots, as we will never process them
            pendingUpdates.clear();
        }
    }

    @Override
    protected NotificationQueue getNotificationQueue() {
        return notificationQueue;
    }

    private void saveForDebugging(final BarrageMessage snapshotOrDelta) {
        if (!REPLICATED_TABLE_DEBUG) {
            return;
        }
        if (processedData.size() > 10) {
            final BarrageMessage msg = (BarrageMessage) processedData.remove(0);
            msg.close();
            processedStep.remove(0);
        }
        processedData.add(snapshotOrDelta.clone());
        processedStep.add(LogicalClock.DEFAULT.currentStep());
    }

    /**
     * Freeze the table.  This will stop all update propagation.
     */
    public synchronized void freeze() {
        frozen = true;
    }

    /**
     * Enqueue an error to be reported on the next refresh cycle.
     *
     * @param e The error
     */
    public void enqueueError(final Throwable e) {
        errorQueue.add(e);
        doWakeup();
    }

    /**
     * Set up a Replicated table from the given proxy, id and columns.  This is intended for internal use only.
     *
     * @param tableDefinition the table definition
     * @param subscribedColumns a bitset of columns that are subscribed
     * @param isViewPort true if the table will be a viewport.
     *
     * @return a properly initialized {@link BarrageSourcedTable}
     */
    @InternalUseOnly
    public static BarrageSourcedTable make(final TableDefinition tableDefinition,
                                           @Nullable final BitSet subscribedColumns,
                                           final boolean isViewPort) {
        return make(LiveTableMonitor.DEFAULT, LiveTableMonitor.DEFAULT, tableDefinition, subscribedColumns, isViewPort);
    }

    @VisibleForTesting
    public static BarrageSourcedTable make(final LiveTableRegistrar registrar,
                                           final NotificationQueue queue,
                                           final TableDefinition tableDefinition,
                                           @Nullable final BitSet subscribedColumns,
                                           final boolean isViewPort) {
        final ColumnDefinition<?>[] columns = tableDefinition.getColumns();
        final WritableSource<?>[] writableSources = new WritableSource[columns.length];
        final RedirectionIndex redirectionIndex = RedirectionIndex.FACTORY.createRedirectionIndex(8);
        final LinkedHashMap<String, ColumnSource<?>> finalColumns = makeColumns(columns, writableSources, redirectionIndex);

        final BarrageSourcedTable table = new BarrageSourcedTable(registrar, queue, finalColumns, writableSources, redirectionIndex, subscribedColumns, isViewPort);

        // Even if this source table will eventually be static, the data isn't here already. Static tables need to
        // have refreshing set to false after processing data but prior to publishing the object to consumers.
        table.setRefreshing(true);

        return table;
    }

    /**
     * Setup the columns for the replicated table.
     *
     * @apiNote emptyRedirectionIndex must be initialized and empty.
     */
    @NotNull
    protected static LinkedHashMap<String, ColumnSource<?>> makeColumns(final ColumnDefinition<?>[] columns,
                                                                        final WritableSource<?>[] writableSources,
                                                                        final RedirectionIndex emptyRedirectionIndex) {
        final LinkedHashMap<String, ColumnSource<?>> finalColumns = new LinkedHashMap<>();
        for (int ii = 0; ii < columns.length; ii++) {
            //noinspection unchecked
            writableSources[ii] = ArrayBackedColumnSource.getMemoryColumnSource(0, columns[ii].getDataType(), columns[ii].getComponentType());
            finalColumns.put(columns[ii].getName(), new RedirectedColumnSource<>(emptyRedirectionIndex, writableSources[ii], 0));
        }

        for (final WritableSource<?> ws : writableSources) {
            ws.startTrackingPrevValues();
        }
        emptyRedirectionIndex.startTrackingPrevValues();

        return finalColumns;
    }

    private void doWakeup() {
        if (REQUEST_LIVE_TABLE_MONITOR_REFRESH) {
            registrar.requestRefresh(this);
        }
    }

    /**
     * Check if this table is a viewport.  A viewport table is a partial view of another table.  If this returns false
     * then this table contains the entire source table it was based on.
     *
     * @return true if this table was a viewport.
     */
    public boolean isViewPort() {
        return isViewPort;
    }

    @Override
    public Object getAttribute(@NotNull String key) {
        final Object localAttribute = super.getAttribute(key);
        if (localAttribute != null) {
            if (key.equals(INPUT_TABLE_ATTRIBUTE)) {
                // TODO: return proxy for input table
                throw new UnsupportedOperationException();
            }
        }
        return localAttribute;
    }

    /**
     * Convenience method for writing consistent log messages from this object.
     *
     * @param level the log level
     * @return a LogEntry
     */
    private LogEntry beginLog(LogLevel level) {
        return log.getEntry(level).append(System.identityHashCode(this));
    }
}
