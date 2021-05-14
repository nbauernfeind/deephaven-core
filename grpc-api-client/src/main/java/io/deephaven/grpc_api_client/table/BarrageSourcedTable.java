/*
 * Copyright (c) 2016-2019 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.grpc_api_client.table;

import com.google.common.annotations.VisibleForTesting;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.linked.TLongLinkedList;
import io.deephaven.base.Function;
import io.deephaven.base.formatters.FormatBitSet;
import io.deephaven.base.verify.Assert;
import io.deephaven.configuration.Configuration;
import io.deephaven.db.exceptions.QueryCancellationException;
import io.deephaven.db.tables.ColumnDefinition;
import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.TableDefinition;
import io.deephaven.db.tables.live.LiveTable;
import io.deephaven.db.tables.live.LiveTableMonitor;
import io.deephaven.db.tables.live.LiveTableRegistrar;
import io.deephaven.db.tables.live.NotificationQueue;
import io.deephaven.db.tables.utils.DBDateTime;
import io.deephaven.db.v2.InstrumentedListenerAdapter;
import io.deephaven.db.v2.ShiftAwareListener;
import io.deephaven.db.v2.remote.InitialSnapshotTable;
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
import io.deephaven.db.v2.utils.IndexBuilder;
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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A client side viewport of a server side {@link Table}.
 *
 * Clients may {@link #getViewportClientId() request a client id} to set
 * a specific viewport.  This class will subscribe to updates for all of the specified rows and columns in all set viewports. </br></br>
 *
 * Note that in this case <b>viewport</b> is defined as a set of positions into the original table.
 */
public class BarrageSourcedTable extends InitialSnapshotTable implements LiveTable, BarrageMessage.Listener {

    private static final boolean REQUEST_LIVE_TABLE_MONITOR_REFRESH = Configuration.getInstance().getBooleanWithDefault("BarrageSourcedTable.requestLiveTableMonitorRefresh", true);
    public static final boolean REPLICATED_TABLE_DEBUG = Configuration.getInstance().getBooleanWithDefault("BarrageSourcedTable.debug", false);

    private static final Logger log = LoggerFactory.getLogger(BarrageSourcedTable.class);

    private final LiveTableRegistrar registrar;
    private final NotificationQueue notificationQueue;

    protected BarrageSourcedTable(final LiveTableRegistrar registrar,
                                  final NotificationQueue notificationQueue,
                                  final LinkedHashMap<String, ColumnSource<?>> columns,
                                  final WritableSource<?>[] writableSources,
                                  final RedirectionIndex redirectionIndex,
                                  final BitSet subscribedColumns,
                                  final boolean isViewPort) {
        super(columns, writableSources, redirectionIndex, subscribedColumns);
        this.registrar = registrar;
        this.notificationQueue = notificationQueue;

        this.refreshEntry = UpdatePerformanceTracker.getInstance().getEntry("BarrageSourcedTable refresh " + System.identityHashCode(this));

        this.isViewPort = isViewPort;
        if (isViewPort) {
            viewport = Index.CURRENT_FACTORY.getEmptyIndex();
            serverViewport = Index.CURRENT_FACTORY.getEmptyIndex();
        } else {
            viewport = null;
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

        // TODO: propagate table errors?

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

    private final UpdatePerformanceTracker.Entry refreshEntry;

    private final WritableSource<?>[] destSources;
    // unsubscribed must never be reset to false once it has been set to true
    private volatile boolean unsubscribed = false;
    private boolean frozen = false;
    private final boolean isViewPort;

    /**
     * A Positional index that defines the union of all UI viewports on this table; it is in position space.
     */
    private Index viewport;

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

    private final Map<Integer, Index > viewportByClient = new HashMap<>();
    private final Map<Integer, Index > suspendedViewportByClient = new HashMap<>();
    private final Map<Integer, BitSet> columnsByClient = new HashMap<>();

    // the Index of rows that we have indicated should be freed; but we can not free until our next refresh cycle
    private Index pendingFree = null;

    private int nextViewportClient = 0;

    private final ConcurrentLinkedQueue<Throwable> errorQueue = new ConcurrentLinkedQueue<>();
    private final Object waitForDataLock = new Object();

    private volatile long processedDelta = -1;
    // we synchronize access to initialSnapshot, deltaSequence, pendingUpdates, pendingColumnSnapshots, and pendingRowSnapshots on pendingUpdatesLock
    private final Object pendingUpdatesLock = new Object();

    // this is where we accumulate the values
    private ArrayDeque<BarrageMessage> pendingUpdates = new ArrayDeque<>();

    // we flip back and forth between the shadow containers to avoid allocation
    private ArrayDeque<BarrageMessage> shadowPendingUpdates = new ArrayDeque<>();

    private final List<Object> processedData;
    private final TLongList processedStep;

    private final CopyOnWriteArrayList<WeakReference<PopulationListener>> populationListeners = new CopyOnWriteArrayList<>();

    public long getProcessedDelta() {
        return processedDelta;
    }

    public ChunkType[] getWireChunkTypes() {
        return Arrays.stream(destSources).map(s -> ChunkType.fromElementType(s.getType())).toArray(ChunkType[]::new);
    }

    public Class<?>[] getWireTypes() {
        return Arrays.stream(destSources).map(ColumnSource::getType).toArray(Class<?>[]::new);
    }

    @Override
    public void handleBarrageMessage(final BarrageMessage update) {
        if (unsubscribed) {
            beginLog(LogLevel.INFO).append(System.identityHashCode(this)).append(": Discarding update for unsubscribed table!").endl();
            return;
        }

        synchronized (pendingUpdatesLock) {
            pendingUpdates.add(update.clone());
        }
        doWakeup();
    }

    private Index.IndexUpdateCoalescer processUpdate(final BarrageMessage update, final Index.IndexUpdateCoalescer coalescer) {
        if (REPLICATED_TABLE_DEBUG) {
            saveForDebugging(update);
            beginLog(LogLevel.INFO).append(": Processing delta updates ")
                    .append(update.firstSeq).append("-").append(update.lastSeq).endl();
        }

        if (update.isSnapshot && update.snapshotIndex != null) {
            serverViewport = update.snapshotIndex.clone();
        }
        if (update.isSnapshot && update.snapshotColumns != null) {
            final BitSet currColumns = getSubscribedColumns();
            final BitSet rmColumns = (BitSet) currColumns.clone();
            rmColumns.andNot(update.snapshotColumns);

            currColumns.clear();
            currColumns.or(update.snapshotColumns);

            for (int colIdx = rmColumns.nextSetBit(0); colIdx >= 0; colIdx = rmColumns.nextSetBit(colIdx + 1)) {
                currColumns.set(colIdx, false);
            }
        }

        // make sure that these index updates make some sense compared with each other, and our current view of the table
        final Index currentIndex = getIndex();
        final boolean initialSnapshot = currentIndex.empty() && update.isSnapshot;
        currentIndex.remove(update.rowsRemoved);

        // We need a copy of this, but note that it is not the prevIndex from the perspective of this LTM, but rather
        // of our remote PUT's LTM.
        try (final SafeCloseableList closer = new SafeCloseableList();
             final Index prevIndex = currentIndex.clone()) {

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
            populatedRows.remove(update.rowsRemoved);
            for (final Index populatedPerColumn : populatedCells) {
                if (populatedPerColumn != null) {
                    populatedPerColumn.remove(update.rowsRemoved);
                }
            }

            // shifts
            if (update.shifted.nonempty()) {
                redirectionIndex.applyShift(prevIndex, update.shifted);
                update.shifted.apply(populatedRows);
                for (final Index populatedPerColumn : populatedCells) {
                    if (populatedPerColumn != null) {
                        update.shifted.apply(populatedPerColumn);
                    }
                }
            }

            final Index totalMods = Index.FACTORY.getEmptyIndex();
            final Index includedMods = Index.FACTORY.getEmptyIndex();
            for (int i = 0; i < update.modColumnData.length; ++i) {
                final BarrageMessage.ModColumnData column = update.modColumnData[i];
                totalMods.insert(column.rowsModified);
                includedMods.insert(column.rowsIncluded);
            }
            includedMods.retain(populatedRows);
            final Index dataOnHand = closer.add(update.rowsIncluded.union(includedMods));

            // post shift space removals due to mods outside of viewport
            if (serverViewport != null && totalMods.nonempty()) {
                try (final Index modsOutOfViewport = totalMods.minus(includedMods)) {
                    freeRows(modsOutOfViewport);
                    populatedRows.remove(modsOutOfViewport);
                    for (final Index populatedPerColumn : populatedCells) {
                        if (populatedPerColumn != null) {
                            populatedPerColumn.remove(modsOutOfViewport);
                        }
                    }
                }

                for (int i = 0, colIdx = update.modColumns.nextSetBit(0); i < update.modColumnData.length; ++i, colIdx = update.modColumns.nextSetBit(colIdx + 1)) {
                    final BarrageMessage.ModColumnData column = update.modColumnData[i];
                    final Index colModified = column.rowsModified;
                    final Index colIncluded = column.rowsIncluded;
                    if (colModified.size() == colIncluded.size()) {
                        continue; // nothing to depopulate
                    }
                    try (final Index modsOutOfViewport = colModified.minus(colIncluded)) {
                        populatedCells[colIdx].remove(modsOutOfViewport);
                    }
                }
            }

            // adds (this includes scoped rows / snapshot rows if its a viewport)
            if (update.rowsIncluded.nonempty()) {
                populatedRows.insert(update.rowsIncluded);
                for (final Index populatedPerColumn : populatedCells) {
                    if (populatedPerColumn != null) {
                        populatedPerColumn.insert(update.rowsIncluded);
                    }
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
                    for (int ii = 0, cidx = update.addColumns.nextSetBit(0); ii < update.addColumnData.length; ++ii, cidx = update.addColumns.nextSetBit(cidx + 1)) {
                        if (isSubscribedColumn(cidx)) {
                            try (final WritableChunkSink.FillFromContext ctxt = destSources[cidx].makeFillFromContext(keys.size())) {
                                destSources[cidx].fillFromChunkUnordered(ctxt, update.addColumnData[ii].data, keys);
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
                    for (int ii = 0, colIdx = update.addColumns.nextSetBit(0); ii < update.addColumnData.length; ++ii, colIdx = update.addColumns.nextSetBit(colIdx + 1)) {
                        if (isSubscribedColumn(colIdx)) {
                            final Chunk<? extends Attributes.Values> data = update.addColumnData[colIdx].data;
                            Assert.eq(data.size(), "delta.includedAdditions.size()", destinationIndex.size(), "destinationIndex.size()");
                            try (final WritableChunkSink.FillFromContext ctxt = destSources[colIdx].makeFillFromContext(destinationIndex.intSize())) {
                                destSources[colIdx].fillFromChunk(ctxt, data, destinationIndex);
                            }
                        }
                    }
                }
            }

            modifiedColumnSet.clear();
            for (int ii = 0, colIdx = update.modColumns.nextSetBit(0); ii < update.modColumnData.length; ++ii, colIdx = update.modColumns.nextSetBit(colIdx + 1)) {
                final BarrageMessage.ModColumnData column = update.modColumnData[ii];
                modifiedColumnSet.setColumnWithIndex(colIdx);

                if (column.rowsIncluded.empty()) {
                    continue;
                }

                try (final Index.Iterator outerIter = column.rowsIncluded.iterator();
                     final WritableLongChunk<Attributes.KeyIndices> keys = WritableLongChunk.makeWritableChunk(column.rowsIncluded.intSize())) {
                    for (int i = 0; i < keys.size(); ++i) {
                        final long outerKey = outerIter.nextLong();
                        keys.set(i, redirectionIndex.get(outerKey));
                        Assert.notEquals(keys.get(i), "keys[i]", Index.NULL_KEY, "Index.NULL_KEY");
                    }

                    try (final WritableChunkSink.FillFromContext ctxt = destSources[colIdx].makeFillFromContext(keys.size())) {
                        destSources[colIdx].fillFromChunkUnordered(ctxt, column.data, keys);
                    }
                }
            }

            beginLog(LogLevel.DEBUG).append(": Populated: ").append(populatedRows).append(", Index=").append(getIndex()).endl();

            if (REPLICATED_TABLE_DEBUG) {
                Assert.assertion(isViewPort || currentIndex.subsetOf(populatedRows), "isViewPort || currentIndex.subsetOf(populatedRows)");
            }

            processedDelta = update.lastSeq;
            notifyOnRowsPopulated(dataOnHand);

            if (!update.isSnapshot || initialSnapshot) {
                final ShiftAwareListener.Update downstream = new ShiftAwareListener.Update(
                        update.rowsAdded.clone(), update.rowsRemoved.clone(), totalMods, update.shifted, modifiedColumnSet);
                return (coalescer == null) ? new Index.IndexUpdateCoalescer(prevIndex, downstream) : coalescer.update(downstream);
            } else {
                return coalescer;
            }
        }
    }

    private void freeRows(final Index removedIndex) {
        if (removedIndex.empty()) {
            return;
        }

        final Index rowsToFree = removedIndex.intersect(populatedRows);
        doFreeRows(rowsToFree);
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
        final TLongArrayList freedRows = new TLongArrayList(rowsToFree.intSize());

        for (final Index.Iterator removedIt = rowsToFree.iterator(); removedIt.hasNext();) {
            final long next = removedIt.nextLong();
            final long prevIndex = redirectionIndex.remove(next);
            if (prevIndex == -1) {
                Assert.assertion(false, "prevIndex != -1", prevIndex, "prevIndex", next, "next");
            }
            freedRows.add(prevIndex);
        }

        freeset.insert(Index.FACTORY.getIndexByValues(freedRows));
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
            notifyWaiters();
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
     * Unsubscribe this table from the server.  The table will no longer be usable afterwards
     */
    public void unsubscribe() {
        synchronized (this) {
            if (unsubscribed) {
                return;
            }
            setUnsubscribed();
        }
        synchronized (pendingUpdatesLock) {
            pendingUpdates.clear();
        }
        // TODO: close subscription
    }

    /**
     * Set the set of columns subscribed for the specified client id.
     *
     * @param client the client id
     * @param newColumns the columns that should be subscribed.
     */
    public void updateColumnSubscriptions(final int client, final BitSet newColumns) {
        synchronized (this) {
            final GetColumnUpdates getColumnUpdates = new GetColumnUpdates(client, newColumns).invoke();
            if (!getColumnUpdates.requiresServerSideUpdate()) {
                return;
            }
            // TODO: update subscription: getColumnUpdates.getCollapsedColumns();
        }
    }

    private synchronized BitSet collapseColumns() {
        final BitSet result = new BitSet(getSubscribedColumns().size());
        columnsByClient.values().forEach(result::or);
        return result;
    }

    /**
     * Allocate and return a new viewport client ID for use with {@link #setViewPort(int, Index)} and it's overrides.
     *
     * @return the newly allocated client id.
     */
    public synchronized int getViewportClientId() {
        if (unsubscribed) {
            throw new IllegalStateException("Can not allocate viewport client after unsubscribed!");
        }
        final int newClient = ++nextViewportClient;
        viewportByClient.put(newClient, Index.FACTORY.getEmptyIndex());
        columnsByClient.put(newClient, new BitSet());
        return newClient;
    }

    /**
     * Set the row viewport for the specified client.
     *
     * @param client the client id
     * @param firstPosition the first position in the viewport
     * @param lastPosition the last position in the viewport (exclusive)
     */
    public void setViewPort(final int client, final long firstPosition, final long lastPosition) {
        final Index newViewPort;
        if (lastPosition > firstPosition) {
            newViewPort = Index.FACTORY.getIndexByRange(firstPosition, lastPosition - 1);
        } else {
            newViewPort = Index.FACTORY.getEmptyIndex();
        }
        setViewPort(client, newViewPort);
    }

    /**
     * Set the row and column viewport for the specified client.
     *
     * @param client the client id
     * @param firstPosition the first position in the viewport
     * @param lastPosition the last position in the viewport (exclusive)
     * @param columns the columns in the viewport
     */
    public void setViewportAndColumns(final int client, final long firstPosition, final long lastPosition, final BitSet columns) {
        final Index newViewPort;
        if (lastPosition > firstPosition) {
            newViewPort = Index.FACTORY.getIndexByRange(firstPosition, lastPosition - 1);
        } else {
            newViewPort = Index.FACTORY.getEmptyIndex();
        }
        setViewportAndColumns(client, newViewPort, columns);
    }

    /**
     * Remove the specified client from the set of viewports.  This will remove any subscribed viewports for this client.
     *
     * @param client the client to remove.
     */
    public void removeViewportClient(final int client) {
        beginLog(LogLevel.INFO).append(": Removing viewport client ").append(client).endl();
        final boolean empty;
        final Index newViewPort;
        final BitSet currentColumns;

        // We need to lock the RTH before we lock ourselves to avoid a potential lock inversion with close()
//        synchronized (subscription) {
        synchronized (this) {
            final Index removedViewport = viewportByClient.remove(client);
            final Index removedSuspendedViewport = suspendedViewportByClient.remove(client);
            Assert.assertion(removedViewport != null || removedSuspendedViewport != null, "removedViewport != null || removedSuspendedViewport != null", removedViewport, "removedViewport", removedSuspendedViewport, "removedSuspendedViewport", client, "client");
            empty = viewportByClient.isEmpty() && suspendedViewportByClient.isEmpty();
            currentColumns = getSubscribedColumns();
            columnsByClient.remove(client);
            newViewPort = collapseViewports(viewportByClient);

            if (!empty) {
                final BitSet removedColumns;
                final BitSet addedColumns;
                final BitSet collapsedColumns;

                final GetCollapsedAndRemovedColumns getCollapsedAndRemovedColumns = new GetCollapsedAndRemovedColumns(currentColumns).invoke();

                removedColumns = getCollapsedAndRemovedColumns.getRemovedColumns();
                addedColumns = getCollapsedAndRemovedColumns.getAddedColumns();
                collapsedColumns = getCollapsedAndRemovedColumns.getCollapsedColumns();

                // once we unsubscribe we can no longer trust these values
                for (int ii = removedColumns.nextSetBit(0); ii >= 0; ii = removedColumns.nextSetBit(ii + 1)) {
                    populatedCells[ii].clear();
                }

                final boolean viewportNeedsUpdate = updateLocalViewport(newViewPort);
                final boolean columnsNeedUpdate = removedColumns.cardinality() > 0 || addedColumns.cardinality() > 0;

                if (viewportNeedsUpdate || columnsNeedUpdate) {
                    // TODO: update subscription: newViewPort, collapsedColumns
                }
            }
//            }
        }

        if (empty) {
            unsubscribe();
        }
        beginLog(LogLevel.DEBUG).append(": Removed viewport client ").append(client).append(", empty=").append(empty).endl();
    }

    public void suspendViewportClient(final int client) {
        beginLog(LogLevel.INFO).append(System.identityHashCode(this)).append(": Suspending viewport client ").append(client).endl();
        final Index newViewPort;
        synchronized (this) {
            final Index removedViewport = viewportByClient.remove(client);
            suspendedViewportByClient.put(client, removedViewport);
            Assert.assertion(removedViewport != null, "removedViewport != null", client, "client");
            newViewPort = collapseViewports(viewportByClient);
            setViewPort(newViewPort);
        }
        beginLog(LogLevel.DEBUG).append(": Suspended viewport client ").append(client).endl();
        log.debug().append(System.identityHashCode(this)).append(": Suspended viewport client ").append(client).endl();
    }

    public void setViewPort(final int client, final @NotNull Index newViewPort) {
        synchronized (this) {
            final GetViewportToSet getViewportToSet = new GetViewportToSet(client, newViewPort).invoke();
            final Index viewportToSet = getViewportToSet.getViewportToSet();

            if (viewportToSet != null) {
                setViewPort(viewportToSet);
            }
        }
    }

    /**
     * Set the positional viewport and relevant columns for the specified client
     *
     * @param client the client id
     * @param newViewPort the positional viewport
     * @param columns the columns to include
     */
    public void setViewportAndColumns(final int client, final Index newViewPort, final BitSet columns) {
        final GetViewportToSet getViewportToSet;
        final GetColumnUpdates getColumnUpdates;

        synchronized (this) {
            getViewportToSet = new GetViewportToSet(client, newViewPort).invoke();
            getColumnUpdates = new GetColumnUpdates(client, columns).invoke();
            final Index viewportToSet = getViewportToSet.getViewportToSet();
            final boolean columnUpdateRequired = getColumnUpdates.requiresServerSideUpdate();

            if (viewportToSet == null && !columnUpdateRequired) {
                return;
            }

            if (viewportToSet == null) {
                // TODO: update subscription: getColumnUpdates.getCollapsedColumns();
            } else if (!columnUpdateRequired) {
                setViewPort(viewportToSet);
            } else {
                // we need to do both
                if (updateLocalViewport(viewportToSet)) {
                    // TODO: update subscription: newViewPort, getColumnUpdates.getCollapsedColumns();
                } else {
                    // TODO: update subscription: getColumnUpdates.getCollapsedColumns();
                }
            }
        }
    }

    /**
     * Collapse all client viewports into a single unified positional index.
     *
     * @param viewportByClient The viewports for each client
     * @return an Index of positions representing the union of all client viewports
     */
    private synchronized Index collapseViewports(final Map<Integer, Index> viewportByClient) {
        if (viewportByClient.isEmpty()) {
            return Index.FACTORY.getEmptyIndex();
        } else if (viewportByClient.size() == 1) {
            return viewportByClient.values().iterator().next();
        }

        final IndexBuilder viewBuilder = Index.FACTORY.getBuilder();
        viewportByClient.values().forEach(viewBuilder::addIndex);
        return viewBuilder.getIndex();
    }

    /**
     * Update the local viewport and subscriptions with the parameter.
     *
     * @param newViewPort the new unified viewport
     * @return a future that can be waited on for the subscription update to complete
     */
    private void setViewPort(final Index newViewPort) {
        if (updateLocalViewport(newViewPort)) {
            // TODO: update subscription: newViewPort
        }
    }

    /**
     * Update the unified local viewport with the specified one.
     *
     * @param newViewPort the updated unified viewport
     * @return true if the viewport was updated, false otherwise.
     */
    private synchronized boolean updateLocalViewport(final Index newViewPort) {
        if (newViewPort.equals(viewport)) {
            beginLog(LogLevel.DEBUG).append(": Viewport Remains Unchanged: ").append(viewport).endl();
            return false;
        }

        Assert.assertion(isViewPort, "isViewPort");

        beginLog(LogLevel.INFO).append(": Viewport Change: ").append(viewport).append(" -> ").append(newViewPort).endl();

        if (unsubscribed) {
            throw new IllegalStateException("Can not update viewport for already unsubscribed subscription");
        }

        viewport = newViewPort.clone();
        return true;
    }

    /**
     * Check if this table has been {@link #setUnsubscribed() unsubscribed}.
     *
     * @return true if the table has been unsubscribed
     */
    public synchronized boolean getUnsubscribed() {
        return unsubscribed;
    }

    /**
     * Set this table as unsubscribed.
     */
    public synchronized void setUnsubscribed() {
        unsubscribed = true;
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
     * Set up a Replicated table from the given proxy, id and columns.  This is intended for Deephaven use only.
     *
     * @param tableDefinition the table definition
     * @param subscribedColumns a bitset of columns that are subscribed
     * @param isViewPort true if the table will be a viewport.
     *
     * @return a properly initialized {@link BarrageSourcedTable}
     */
    @InternalUseOnly
    public static BarrageSourcedTable make(final TableDefinition tableDefinition,
                                           final BitSet subscribedColumns,
                                           final boolean isViewPort) {
        return make(LiveTableMonitor.DEFAULT, LiveTableMonitor.DEFAULT, tableDefinition, subscribedColumns, isViewPort);
    }

    @VisibleForTesting
    public static BarrageSourcedTable make(final LiveTableRegistrar registrar,
                                           final NotificationQueue queue,
                                           final TableDefinition tableDefinition,
                                           final BitSet subscribedColumns,
                                           final boolean isViewPort) {
        final ColumnDefinition<?>[] columns = tableDefinition.getColumns();
        final WritableSource<?>[] writableSources = new WritableSource[columns.length];
        final RedirectionIndex redirectionIndex = RedirectionIndex.FACTORY.createRedirectionIndex(8);
        final LinkedHashMap<String, ColumnSource<?>> finalColumns = makeColumns(columns, writableSources, redirectionIndex);

        final BarrageSourcedTable table = new BarrageSourcedTable(registrar, queue, finalColumns, writableSources, redirectionIndex, subscribedColumns, isViewPort);

        // TODO: is this true on static tables? is it safe to make false if DoPut is sealed?
        table.setRefreshing(true);

        return table;
    }

    /**
     * Setup the columns for the replicated table.
     *
     * NB: Your emptyRedirectionIndex must be initialized and empty.
     */
    @NotNull
    protected static LinkedHashMap<String, ColumnSource<?>> makeColumns(final ColumnDefinition<?>[] columns,
                                                                        final WritableSource<?>[] writableSources,
                                                                        final RedirectionIndex emptyRedirectionIndex) {
        final LinkedHashMap<String, ColumnSource<?>> finalColumns = new LinkedHashMap<>();
        for (int ii = 0; ii < columns.length; ii++) {
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
        notifyWaiters();
        if (REQUEST_LIVE_TABLE_MONITOR_REFRESH) {
            registrar.requestRefresh(this);
        }
    }

    /**
     * Wait for initial data to be available.  This method will block.
     */
    public void waitForData() {
        final AtomicBoolean flag = new AtomicBoolean(false);

        if (getProcessedDelta() >= 0)
            return;

        // set retainReference = false as this thread will block and hold the reference to this listener until its finished
        final InstrumentedListenerAdapter listener = new InstrumentedListenerAdapter(null, this, false) {
            @Override
            public void onUpdate(Index added, Index removed, Index modified) {
                final long localProcessedDelta = getProcessedDelta();
                beginLog(LogLevel.INFO)
                        .append(": waitForData listener fired with delta=")
                        .append(localProcessedDelta).endl();

                if (localProcessedDelta < 0 && !unsubscribed) {
                    throw new IllegalStateException("BarrageSourcedTable.waitForData() listener invoked while processedDelta=" +
                            localProcessedDelta + " < 0 and unsubscribed=" + unsubscribed);
                }
                synchronized (waitForDataLock) {
                    flag.set(true);
                    waitForDataLock.notifyAll();
                }
            }

            @Override
            public void onFailureInternal(Throwable originalException, UpdatePerformanceTracker.Entry sourceEntry) {
                beginLog(LogLevel.INFO).append(": waitForData listener fired with error ").append(originalException).endl();
                synchronized (waitForDataLock) {
                    flag.set(true);
                    waitForDataLock.notifyAll();
                }
                super.onFailureInternal(originalException, sourceEntry);
            }
        };

        this.listenForDirectUpdates(listener);

        beginLog(LogLevel.INFO)
                .append(": waitForData: listener installed")
                .append(" delta=").append(getProcessedDelta())
                .append(", flag=").append(flag.get()).endl();

        while (true) {
            synchronized (waitForDataLock) {
                final long processedDelta = getProcessedDelta();
                if (processedDelta >= 0 || flag.get()) {
                    break;
                }

                // TODO: this needs to be properly fixed with a real wait (previous version suffered from busy-spinning)
                try {
                    beginLog(LogLevel.INFO).append(": waitForData: start").append(", delta=").append(processedDelta).append(", flag=").append(flag.get()).endl();
                    waitForDataLock.wait(1000);
                } catch (InterruptedException interruptIsCancel) {
                    throw new QueryCancellationException("Interrupt detected", interruptIsCancel);
                }
                beginLog(LogLevel.INFO).append(": waitForData end: ").append(", delta=").append(getProcessedDelta()).append(", flag=").append(flag.get()).endl();
            }

            selfRefresh();
        }

        this.removeDirectUpdateListener(listener);
    }

    private void selfRefresh() {
        // we should refresh this table
        registrar.maybeRefreshTable(this,true);
    }

    /**
     * Waits for the specified position to become populated.
     * This method automatically converts the specified position into the table's keyspace.
     *
     * @param position the position
     * @param column the column
     */
    public void waitForPopulation(final long position, final int column) {
        waitForPopulation(() -> column < 0 ? isRowPopulated(position) : isPopulated(position, column),
                () -> column < 0 ? ", position: " + position : ", (" + position + "," +  column + ")"
        );
    }

    /**
     * Wait for all of the columns and rows in the viewport to become populated.
     */
    public void waitForPopulation() {
        waitForPopulation(() -> viewport.subsetOf(serverViewport),
                () -> ", unpopulated: " + viewport.minus(serverViewport).size()
        );
    }

    /**
     * Waits for all of the columns in the specified positions to become populated.
     * This method automatically converts the specified positions into the table's keyspace.
     *
     * @param positions the rows to inspect
     * @param columns a BitSet of column indices to check for population
     */
    public void waitForPopulation(final Index positions, final BitSet columns) {
        waitForPopulation(() -> {
                    if (!positions.subsetOf(serverViewport)) {
                        return false;
                    }

                    final Index effectiveIndex = getIndex().subindexByPos(positions);
                    for (int ii = columns.nextSetBit(0); ii >= 0; ii = columns.nextSetBit(ii+1)) {
                        if (!effectiveIndex.subsetOf(populatedCells[ii])) {
                            return false;
                        }
                    }

                    return true;
                },
                () -> ", unpopulated: " + positions.minus(serverViewport).size()
        );
    }

    private void waitForPopulation(Function.Nullary<Boolean> populationCheck, Function.Nullary<String> description) {
        final PopulationListener populationListener = new PopulationListener() {
            @Override
            public void onRowsPopulated(Index rows) {
                wakeUp();
            }

            @Override
            public void onColumnsPopulated(Index rows, BitSet columns) {
                wakeUp();
            }

            private void wakeUp() {
                notifyWaiters();
            }
        };

        this.addPopulationListener(populationListener);

        do {
            final boolean populated = populationCheck.call();
            if (populated) {
                break;
            }

            beginLog(LogLevel.INFO).append(": waitForPopulation: start ").append(description.call()).endl();
            try {
                synchronized (waitForDataLock) {
                    waitForDataLock.wait(100);
                }
            } catch (InterruptedException interruptIsCancel) {
                throw new QueryCancellationException("Interrupt detected", interruptIsCancel);
            }
            beginLog(LogLevel.INFO).append(": waitForPopulation end: ").append(description.call()).endl();

            selfRefresh();
        } while (true);

        this.removePopulationListener(populationListener);
    }

    private void notifyWaiters() {
        synchronized (waitForDataLock) {
            waitForDataLock.notifyAll();
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

    /**
     * Check if the specified position is populated.
     * This method automatically converts the specified position into the table's keyspace.
     *
     * @param position the position
     * @return true of it has been populated
     */
    private synchronized boolean isRowPopulated(long position) {
        final long indexKey = getIndex().get(position);
        return populatedRows.find(indexKey) >= 0;
    }

    /**
     * Check if the specified position is populated.
     * This method automatically converts the specified position into the table's keyspace.
     *
     * @param position the position
     * @param column the column
     * @return true of it has been populated
     */
    public synchronized boolean isPopulated(long position, int column) {
        final long indexKey = getIndex().get(position);
        return populatedCells[column].find(indexKey) >= 0;
    }

    /**
     * Get the current positional viewport index.  If this BarrageSourcedTable is a viewport for another table (see ...) then
     * this returns the union of all viewports from all clients. Otherwise it returns a flat set of positions from
     * 0 - tableSize -1;
     *
     * @return an index of viewport positions
     */
    public synchronized Index getViewportIndex() {
        return (isViewPort) ? viewport : Index.FACTORY.getFlatIndex(getIndex().size());
    }

    /**
     * Get the set of populated rows in Key space.
     *
     * @return the populated keys.
     */
    public synchronized Index getPopulatedRows() {
        return populatedRows;
    }

    /**
     * Check if All of the columns in the specified row are populated.  Note that the input row index is expected
     * to be in <b>position</b> space.  It is internally converted to the table's keyspace.
     *
     * @param row The row to inspect
     * @param columns a BitSet of column indices to check for population
     */
    public synchronized boolean isPopulated(long row, BitSet columns) {
        final long indexKey = getIndex().get(row);

        for (int i = columns.nextSetBit(0); i >= 0; i = columns.nextSetBit(i+1)) {
            if (populatedCells[i].find(indexKey) < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if All of the columns in the specified row are populated.  Note that the input row index is expected
     * to be in <b>position</b> space. It is internally converted to the table's keyspace.
     *
     * @param rows the rows to inspect
     * @param columns a BitSet of column indices to check for population
     */
    public synchronized boolean isPopulated(Index rows, BitSet columns) {
        final Index effectiveIndex = getIndex().subindexByPos(rows);
        for (int i = columns.nextSetBit(0); i >= 0; i = columns.nextSetBit(i+1)) {
            if (!effectiveIndex.subsetOf(populatedCells[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the column specified is fully populated.
     *
     * @param column the column index
     * @return true if the column is completely populated.
     */
    public synchronized boolean isColumnFullyPopulated(int column) {
        return getIndex().subsetOf(populatedCells[column]);
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
     * A listener for population events within this table.
     */
    public interface PopulationListener {
        /**
         * Handle the newly populated keys.
         *
         * @param rows the row keys that were populated.
         */
        void onRowsPopulated(Index rows);

        /**
         * Handle the newly populated keys for the specified columns.
         *
         * @param rows the row keys that are populated
         * @param columns the columns for which these keys were populated.
         */
        void onColumnsPopulated(Index rows, BitSet columns);
    }

    /**
     * Listen for population events.  Note that these are stored as {@link WeakReference}s, so you must retain a reference
     * to them somewhere or they will be reclaimed by the garbage collector.
     *
     * @param listener the listener
     */
    public void addPopulationListener(@NotNull PopulationListener listener) {
        populationListeners.add(new WeakReference<>(listener));
    }

    /**
     * Remove the specified listener from notifications.
     *
     * @param listener the listener to remove
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void removePopulationListener(@NotNull PopulationListener listener) {
        final ArrayList<WeakReference<PopulationListener>> collectedRefs = new ArrayList<>();
        for (WeakReference<PopulationListener> listenerRef : populationListeners) {
            final PopulationListener checkListener = listenerRef.get();
            if (checkListener == null || checkListener == listener) {
                collectedRefs.add(listenerRef);
            }
        }
        populationListeners.removeAll(collectedRefs);
    }

    private void notifyOnRowsPopulated(Index rows) {
        final ArrayList<WeakReference<PopulationListener>> collectedRefs = new ArrayList<>();
        for (WeakReference<PopulationListener> listenerRef : populationListeners) {
            final PopulationListener listener = listenerRef.get();
            if (listener == null) {
                collectedRefs.add(listenerRef);
            } else {
                listener.onRowsPopulated(rows);
            }
        }
        populationListeners.removeAll(collectedRefs);
    }

    private void notifyOnColumnsPopulated(Index rows, BitSet columns) {
        final ArrayList<WeakReference<PopulationListener>> collectedRefs = new ArrayList<>();
        for (WeakReference<PopulationListener> listenerRef : populationListeners) {
            final PopulationListener listener = listenerRef.get();
            if (listener == null) {
                collectedRefs.add(listenerRef);
            } else {
                listener.onColumnsPopulated(rows, columns);
            }
        }
        populationListeners.removeAll(collectedRefs);
    }

    private class GetCollapsedAndRemovedColumns {
        private final BitSet currentColumns;
        private BitSet removedColumns;
        private BitSet addedColumns;
        private BitSet collapsedColumns;

        GetCollapsedAndRemovedColumns(BitSet currentColumns) {
            this.currentColumns = currentColumns;
        }

        BitSet getRemovedColumns() {
            return removedColumns;
        }

        BitSet getAddedColumns() {
            return addedColumns;
        }

        BitSet getCollapsedColumns() {
            return collapsedColumns;
        }

        public GetCollapsedAndRemovedColumns invoke() {
            Assert.assertion(isViewPort, "isViewPort");
            Assert.neqNull(getSubscribedColumns(), "getSubscribedColumns()");

            if (unsubscribed) {
                throw new IllegalStateException("Unsubscribed from table");
            }

            collapsedColumns = collapseColumns();

            removedColumns = new BitSet(currentColumns.size());
            removedColumns.or(currentColumns);
            removedColumns.andNot(collapsedColumns);

            addedColumns = new BitSet(currentColumns.size());
            addedColumns.or(collapsedColumns);
            addedColumns.andNot(currentColumns);

            currentColumns.and(collapsedColumns);
            currentColumns.or(collapsedColumns);
            return this;
        }
    }

    /**
     * Given a client and viewport for that client compute the new overall viewport for this table
     */
    private class GetViewportToSet {
        /** The client id */
        private final int client;
        /** The overall viewport for this client*/
        private final Index newViewPort;
        /** Output; The newly computed overall viewport after {@link #invoke()} */
        private Index viewportToSet;

        GetViewportToSet(int client, Index newViewPort) {
            this.client = client;
            this.newViewPort = newViewPort;
        }

        Index getViewportToSet() {
            return viewportToSet;
        }

        /**
         * Update the viewport for this client and compute a new overall viewport and overall suspended viuewport.
         *
         * @return this object.
         */
        public GetViewportToSet invoke() {
            synchronized (BarrageSourcedTable.this) {
                Assert.assertion(client >= 0 && client <= nextViewportClient, "client > 0 && client <= nextViewportClient", client, "client", nextViewportClient, "nextViewportClient");
                beginLog(LogLevel.INFO).append(": Setting client viewport for client=").append(client).append(" to ").append(newViewPort).endl();

                suspendedViewportByClient.remove(client);

                final Index clientViewport = viewportByClient.get(client);
                viewportByClient.put(client, newViewPort);

                if (clientViewport == null) {
                    if (!newViewPort.subsetOf(BarrageSourcedTable.this.viewport)) {
                        viewportToSet = BarrageSourcedTable.this.viewport.union(newViewPort);
                    } else {
                        viewportToSet = null;
                    }
                } else {
                    viewportToSet = collapseViewports(viewportByClient);
                }
            }
            return this;
        }
    }

    /**
     * Given a client and a set of viewport columns compute the new overall set of viewport columns for this table
     */
    private class GetColumnUpdates {
        /** The client id */
        private final int client;
        /** The new set of columns to be subscribed for the client */
        private final BitSet newColumns;
        /** Output: The computed unified set of required columns to be subscribed */
        private BitSet collapsedColumns;
        /** Output: If the column change requires an update from the server */
        private boolean requiresServerSideUpdate;

        GetColumnUpdates(int client, BitSet newColumns) {
            this.client = client;
            this.newColumns = newColumns;
        }

        BitSet getCollapsedColumns() {
            return collapsedColumns;
        }

        boolean requiresServerSideUpdate() {
            return requiresServerSideUpdate;
        }

        public GetColumnUpdates invoke() {
            final BitSet removedColumns;
            final BitSet addedColumns;
            synchronized (BarrageSourcedTable.this) {
                final BitSet currentColumns = getSubscribedColumns();
                columnsByClient.put(client, newColumns);

                final GetCollapsedAndRemovedColumns getCollapsedAndRemovedColumns = new GetCollapsedAndRemovedColumns(currentColumns).invoke();

                removedColumns = getCollapsedAndRemovedColumns.getRemovedColumns();
                addedColumns = getCollapsedAndRemovedColumns.getAddedColumns();
                collapsedColumns = getCollapsedAndRemovedColumns.getCollapsedColumns();
            }

            // once we unsubscribe we can no longer trust these values
            for (int ii = removedColumns.nextSetBit(0); ii >= 0; ii = removedColumns.nextSetBit(ii+1)) {
                populatedCells[ii].clear();
            }

            // This should be done at a higher level!
            requiresServerSideUpdate = removedColumns.cardinality() > 0 || addedColumns.cardinality() > 0;

            beginLog(LogLevel.INFO).append(": Setting client columns for client=").append(client).append(" to ").append(FormatBitSet.formatBitSet(newColumns)).append(", collapsed=").append(FormatBitSet.formatBitSet(collapsedColumns)).append(", serverUpdate=").append(requiresServerSideUpdate).append(", removed=").append(FormatBitSet.formatBitSet(removedColumns)).append(", added=").append(FormatBitSet.formatBitSet(addedColumns)).endl();

            return this;
        }
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
