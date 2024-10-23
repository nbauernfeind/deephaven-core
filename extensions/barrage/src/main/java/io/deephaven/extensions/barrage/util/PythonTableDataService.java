//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.extensions.barrage.util;

import io.deephaven.UncheckedDeephavenException;
import io.deephaven.api.SortColumn;
import io.deephaven.base.log.LogOutput;
import io.deephaven.base.verify.Assert;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.configuration.Configuration;
import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.rowset.RowSetFactory;
import io.deephaven.engine.table.BasicDataIndex;
import io.deephaven.engine.table.ColumnDefinition;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.TableDefinition;
import io.deephaven.engine.table.impl.PartitionAwareSourceTable;
import io.deephaven.engine.table.impl.chunkboxer.ChunkBoxer;
import io.deephaven.engine.table.impl.locations.*;
import io.deephaven.engine.table.impl.locations.impl.*;
import io.deephaven.engine.table.impl.sources.regioned.*;
import io.deephaven.extensions.barrage.chunk.ChunkInputStreamGenerator;
import io.deephaven.extensions.barrage.chunk.ChunkReader;
import io.deephaven.extensions.barrage.chunk.DefaultChunkReadingFactory;
import io.deephaven.generic.region.*;
import io.deephaven.io.log.impl.LogOutputStringImpl;
import io.deephaven.util.SafeCloseable;
import io.deephaven.util.annotations.ScriptApi;
import org.apache.arrow.flatbuf.MessageHeader;
import org.apache.arrow.flatbuf.RecordBatch;
import org.apache.arrow.flatbuf.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jpy.PyObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

import static io.deephaven.extensions.barrage.util.ArrowToTableConverter.parseArrowIpcMessage;

@ScriptApi
public class PythonTableDataService extends AbstractTableDataService {

    private static final int DEFAULT_PAGE_SIZE = Configuration.getInstance()
            .getIntegerForClassWithDefault(PythonTableDataService.class, "DEFAULT_PAGE_SIZE", 1 << 16);
    private static final long REGION_MASK = RegionedColumnSource.ROW_KEY_TO_SUB_REGION_ROW_INDEX_MASK;

    private final BackendAccessor backend;
    private final ChunkReader.Factory chunkReaderFactory;
    private final StreamReaderOptions streamReaderOptions;
    private final int pageSize;

    @ScriptApi
    public static PythonTableDataService create(
            @NotNull final PyObject pyTableDataService,
            @Nullable final ChunkReader.Factory chunkReaderFactory,
            @Nullable final StreamReaderOptions streamReaderOptions,
            final int pageSize) {
        return new PythonTableDataService(
                pyTableDataService,
                chunkReaderFactory == null ? DefaultChunkReadingFactory.INSTANCE : chunkReaderFactory,
                streamReaderOptions == null ? BarrageUtil.DEFAULT_SNAPSHOT_DESER_OPTIONS : streamReaderOptions,
                pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize);
    }

    /**
     * Construct a Deephaven {@link io.deephaven.engine.table.impl.locations.TableDataService TableDataService} wrapping
     * the provided Python TableDataServiceBackend.
     *
     * @param pyTableDataService The Python TableDataService
     * @param pageSize The page size to use for all regions
     */
    private PythonTableDataService(
            @NotNull final PyObject pyTableDataService,
            @NotNull final ChunkReader.Factory chunkReaderFactory,
            @NotNull final StreamReaderOptions streamReaderOptions,
            final int pageSize) {
        super("PythonTableDataService");
        this.backend = new BackendAccessor(pyTableDataService);
        this.chunkReaderFactory = chunkReaderFactory;
        this.streamReaderOptions = streamReaderOptions;
        this.pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    /**
     * Get a Deephaven {@link Table} for the supplied {@link TableKey}.
     *
     * @param tableKey The table key
     * @param live Whether the table should update as new data becomes available
     * @return The {@link Table}
     */
    @ScriptApi
    public Table makeTable(@NotNull final TableKeyImpl tableKey, final boolean live) {
        final TableLocationProviderImpl tableLocationProvider =
                (TableLocationProviderImpl) getTableLocationProvider(tableKey);
        return new PartitionAwareSourceTable(
                tableLocationProvider.tableDefinition,
                tableKey.toString(),
                RegionedTableComponentFactoryImpl.INSTANCE,
                tableLocationProvider,
                live ? ExecutionContext.getContext().getUpdateGraph() : null);
    }

    /**
     * This Backend impl marries the Python TableDataService with the Deephaven TableDataService. By performing the
     * object translation here, we can keep the Python TableDataService implementation simple and focused on the Python
     * side of the implementation.
     */
    private class BackendAccessor {
        private final PyObject pyTableDataService;

        private BackendAccessor(
                @NotNull final PyObject pyTableDataService) {
            this.pyTableDataService = pyTableDataService;
        }

        /**
         * Get two schemas, the first for partitioning columns whose values will be derived from TableLocationKey and
         * applied to all rows in the associated TableLocation, and the second specifying the table data to be read
         * chunk-wise (in columnar fashion) from theTableLocations.
         *
         * @param tableKey the table key
         * @return the schemas
         */
        public BarrageUtil.ConvertedArrowSchema[] getTableSchema(
                @NotNull final TableKeyImpl tableKey) {
            // The schemas are
            final BarrageUtil.ConvertedArrowSchema[] schemas = new BarrageUtil.ConvertedArrowSchema[2];
            final Consumer<ByteBuffer[]> onRawSchemas = byteBuffers -> {
                if (byteBuffers.length != schemas.length) {
                    throw new IllegalArgumentException("Expected two Arrow IPC messages: found " + byteBuffers.length);
                }

                for (int ii = 0; ii < schemas.length; ++ii) {
                    schemas[ii] = BarrageUtil.convertArrowSchema(ArrowToTableConverter.parseArrowSchema(
                            ArrowToTableConverter.parseArrowIpcMessage(byteBuffers[ii])));
                }
            };

            pyTableDataService.call("_table_schema", tableKey.key, onRawSchemas);

            return schemas;
        }

        /**
         * Get the existing table locations for the provided {@code tableKey}.
         *
         * @param definition the table definition to validate partitioning columns against
         * @param tableKey the table key
         * @param listener the listener to call with each existing table location key
         */
        public void getTableLocations(
                @NotNull final TableDefinition definition,
                @NotNull final TableKeyImpl tableKey,
                @NotNull final Consumer<TableLocationKeyImpl> listener) {
            final BiConsumer<TableLocationKeyImpl, ByteBuffer[]> convertingListener =
                    (tableLocationKey, byteBuffers) -> processTableLocationKey(definition, tableKey, listener,
                            tableLocationKey, byteBuffers);

            pyTableDataService.call("_table_locations", tableKey.key, convertingListener);
        }

        /**
         * Subscribe to table location updates for the provided {@code tableKey}.
         * <p>
         * The listener must be called with all existing table locations before returning. If the listener is invoked
         * asynchronously then those callers will block until this method has completed.
         *
         * @param definition the table definition to validate partitioning columns against
         * @param tableKey the table key
         * @param listener the listener to call with each table location key
         * @return a {@link SafeCloseable} that can be used to cancel the subscription
         */
        public SafeCloseable subscribeToTableLocations(
                @NotNull final TableDefinition definition,
                @NotNull final TableKeyImpl tableKey,
                @NotNull final Consumer<TableLocationKeyImpl> listener) {
            final BiConsumer<TableLocationKeyImpl, ByteBuffer[]> convertingListener =
                    (tableLocationKey, byteBuffers) -> processTableLocationKey(definition, tableKey, listener,
                            tableLocationKey, byteBuffers);

            final PyObject cancellationCallback = pyTableDataService.call(
                    "_subscribe_to_table_locations", tableKey.key, convertingListener);
            return () -> cancellationCallback.call("__call__");
        }

        private void processTableLocationKey(
                @NotNull final TableDefinition definition,
                @NotNull final TableKeyImpl tableKey,
                @NotNull final Consumer<TableLocationKeyImpl> listener,
                @NotNull final TableLocationKeyImpl tableLocationKey,
                @NotNull final ByteBuffer[] byteBuffers) {
            if (byteBuffers.length == 0) {
                listener.accept(tableLocationKey);
                return;
            }

            if (byteBuffers.length != 2) {
                throw new IllegalArgumentException("Expected Single Record Batch: found " + byteBuffers.length);
            }

            // note that we recompute chunk readers for each location to support some schema evolution
            final Map<String, Comparable<?>> partitioningValuesUnordered = new HashMap<>();
            final Schema partitioningValuesSchema = ArrowToTableConverter.parseArrowSchema(
                    ArrowToTableConverter.parseArrowIpcMessage(byteBuffers[0]));
            final BarrageUtil.ConvertedArrowSchema schemaPlus =
                    BarrageUtil.convertArrowSchema(partitioningValuesSchema);

            try {
                definition.checkCompatibility(schemaPlus.tableDef);
            } catch (TableDefinition.IncompatibleTableDefinitionException err) {
                throw new IllegalArgumentException("Partitioning schema is incompatible with table schema", err);
            }

            final ChunkReader[] readers = schemaPlus.computeChunkReaders(
                    DefaultChunkReadingFactory.INSTANCE,
                    partitioningValuesSchema,
                    BarrageUtil.DEFAULT_SNAPSHOT_DESER_OPTIONS);

            final BarrageProtoUtil.MessageInfo recordBatchMessageInfo = parseArrowIpcMessage(byteBuffers[1]);
            if (recordBatchMessageInfo.header.headerType() != MessageHeader.RecordBatch) {
                throw new IllegalArgumentException("byteBuffers[1] is not a valid Arrow RecordBatch IPC message");
            }
            final RecordBatch batch = (RecordBatch) recordBatchMessageInfo.header.header(new RecordBatch());

            final Iterator<ChunkInputStreamGenerator.FieldNodeInfo> fieldNodeIter =
                    new FlatBufferIteratorAdapter<>(batch.nodesLength(),
                            i -> new ChunkInputStreamGenerator.FieldNodeInfo(batch.nodes(i)));

            final PrimitiveIterator.OfLong bufferInfoIter = ArrowToTableConverter.extractBufferInfo(batch);

            // extract partitioning values and box them to be used as Comparable in the map
            for (int ci = 0; ci < partitioningValuesSchema.fieldsLength(); ++ci) {
                try (final WritableChunk<Values> columnValues = readers[ci].readChunk(
                        fieldNodeIter, bufferInfoIter, recordBatchMessageInfo.inputStream, null, 0, 0)) {

                    if (columnValues.size() != 1) {
                        throw new IllegalArgumentException("Expected Single Row: found " + columnValues.size());
                    }

                    partitioningValuesUnordered.put(
                            partitioningValuesSchema.fields(ci).name(), ChunkBoxer.boxedGet(columnValues, 0));
                } catch (final IOException unexpected) {
                    throw new UncheckedDeephavenException(unexpected);
                }
            }

            // partitioning values must be in the same order as the partitioning keys
            final Map<String, Comparable<?>> partitioningValues = definition.getPartitioningColumns().stream()
                    .map(ColumnDefinition::getName)
                    .collect(Collectors.toMap(
                            key -> key,
                            partitioningValuesUnordered::get,
                            Assert::neverInvoked,
                            LinkedHashMap::new));
            listener.accept(new TableLocationKeyImpl(tableLocationKey.locationKey, partitioningValues));
        }

        /**
         * Get the size of the given {@code tableLocationKey}.
         *
         * @param tableKey the table key
         * @param tableLocationKey the table location key
         * @param listener the listener to call with the table location size
         */
        public void getTableLocationSize(
                @NotNull final TableKeyImpl tableKey,
                @NotNull final TableLocationKeyImpl tableLocationKey,
                @NotNull final LongConsumer listener) {
            pyTableDataService.call("_table_location_size", tableKey.key, tableLocationKey.locationKey, listener);
        }

        /**
         * Subscribe to the existing size and future size changes of a table location.
         *
         * @param tableKey the table key
         * @param tableLocationKey the table location key
         * @param listener the listener to call with the partition size
         * @return a {@link SafeCloseable} that can be used to cancel the subscription
         */
        public SafeCloseable subscribeToTableLocationSize(
                @NotNull final TableKeyImpl tableKey,
                @NotNull final TableLocationKeyImpl tableLocationKey,
                @NotNull final LongConsumer listener) {

            final PyObject cancellationCallback = pyTableDataService.call(
                    "_subscribe_to_table_location_size", tableKey.key, tableLocationKey.locationKey, listener);

            return () -> cancellationCallback.call("__call__");
        }

        /**
         * Get a range of data for a column.
         *
         * @param tableKey the table key
         * @param tableLocationKey the table location key
         * @param columnDefinition the column definition
         * @param firstRowPosition the first row position
         * @param minimumSize the minimum size
         * @param maximumSize the maximum size
         * @return the column values
         */
        public List<WritableChunk<Values>> getColumnValues(
                @NotNull final TableKeyImpl tableKey,
                @NotNull final TableLocationKeyImpl tableLocationKey,
                @NotNull final ColumnDefinition<?> columnDefinition,
                final long firstRowPosition,
                final int minimumSize,
                final int maximumSize) {

            final ArrayList<WritableChunk<Values>> resultChunks = new ArrayList<>();
            final Consumer<ByteBuffer[]> onMessages = messages -> {
                if (messages.length < 2) {
                    throw new IllegalArgumentException("Expected at least two Arrow IPC messages: found "
                            + messages.length);
                }
                resultChunks.ensureCapacity(messages.length - 1);

                final Schema schema = ArrowToTableConverter.parseArrowSchema(
                        ArrowToTableConverter.parseArrowIpcMessage(messages[0]));
                final BarrageUtil.ConvertedArrowSchema schemaPlus = BarrageUtil.convertArrowSchema(schema);

                if (schema.fieldsLength() > 1) {
                    throw new UnsupportedOperationException("More columns returned than requested.");
                }
                if (!columnDefinition.isCompatible(schemaPlus.tableDef.getColumns().get(0))) {
                    throw new IllegalArgumentException("Returned column is not compatible with requested column");
                }

                final ChunkReader reader = schemaPlus.computeChunkReaders(
                        chunkReaderFactory, schema, streamReaderOptions)[0];
                try {
                    for (int ii = 1; ii < messages.length; ++ii) {
                        final BarrageProtoUtil.MessageInfo recordBatchMessageInfo = parseArrowIpcMessage(messages[ii]);
                        if (recordBatchMessageInfo.header.headerType() != MessageHeader.RecordBatch) {
                            throw new IllegalArgumentException(
                                    "byteBuffers[" + ii + "] is not a valid Arrow RecordBatch IPC message");
                        }
                        final RecordBatch batch = (RecordBatch) recordBatchMessageInfo.header.header(new RecordBatch());

                        final Iterator<ChunkInputStreamGenerator.FieldNodeInfo> fieldNodeIter =
                                new FlatBufferIteratorAdapter<>(batch.nodesLength(),
                                        i -> new ChunkInputStreamGenerator.FieldNodeInfo(batch.nodes(i)));

                        final PrimitiveIterator.OfLong bufferInfoIter = ArrowToTableConverter.extractBufferInfo(batch);

                        resultChunks.add(reader.readChunk(
                                fieldNodeIter, bufferInfoIter, recordBatchMessageInfo.inputStream, null, 0, 0));
                    }
                } catch (final IOException unexpected) {
                    SafeCloseable.closeAll(resultChunks.iterator());
                    throw new UncheckedDeephavenException(unexpected);
                }
            };

            pyTableDataService.call("_column_values",
                    tableKey.key, tableLocationKey.locationKey, columnDefinition.getName(), firstRowPosition,
                    minimumSize, maximumSize, onMessages);

            return resultChunks;
        }
    }

    @Override
    protected @NotNull TableLocationProvider makeTableLocationProvider(@NotNull final TableKey tableKey) {
        if (!(tableKey instanceof TableKeyImpl)) {
            throw new UnsupportedOperationException(String.format("%s: Unsupported TableKey %s", this, tableKey));
        }
        return new TableLocationProviderImpl((TableKeyImpl) tableKey);
    }

    /**
     * {@link TableKey} implementation for TableService.
     */
    public static class TableKeyImpl implements ImmutableTableKey {

        private final PyObject key;
        private int cachedHashCode;

        public TableKeyImpl(@NotNull final PyObject key) {
            this.key = key;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TableKeyImpl)) {
                return false;
            }
            final TableKeyImpl otherTableKey = (TableKeyImpl) other;
            return this.key.equals(otherTableKey.key);
        }

        @Override
        public int hashCode() {
            if (cachedHashCode == 0) {
                final int computedHashCode = Long.hashCode(key.call("__hash__").getLongValue());
                // Don't use 0; that's used by StandaloneTableKey, and also our sentinel for the need to compute
                if (computedHashCode == 0) {
                    final int fallbackHashCode = TableKeyImpl.class.hashCode();
                    cachedHashCode = fallbackHashCode == 0 ? 1 : fallbackHashCode;
                } else {
                    cachedHashCode = computedHashCode;
                }
            }
            return cachedHashCode;
        }

        @Override
        public LogOutput append(@NotNull final LogOutput logOutput) {
            return logOutput.append(getImplementationName())
                    .append("[key=").append(key.toString()).append(']');
        }

        @Override
        public String toString() {
            return new LogOutputStringImpl().append(this).toString();
        }

        @Override
        public String getImplementationName() {
            return "PythonTableDataService.TableKeyImpl";
        }
    }

    /**
     * {@link TableLocationProvider} implementation for TableService.
     */
    private class TableLocationProviderImpl extends AbstractTableLocationProvider {

        private final TableDefinition tableDefinition;

        private Subscription subscription = null;

        private TableLocationProviderImpl(@NotNull final TableKeyImpl tableKey) {
            super(tableKey, true);
            final BarrageUtil.ConvertedArrowSchema[] schemas = backend.getTableSchema(tableKey);

            final TableDefinition partitioningDef = schemas[0].tableDef;
            final TableDefinition tableDataDef = schemas[1].tableDef;
            final Map<String, ColumnDefinition<?>> columns = new LinkedHashMap<>(tableDataDef.numColumns());

            // all partitioning columns default to the front
            for (final ColumnDefinition<?> column : partitioningDef.getColumns()) {
                columns.put(column.getName(), column.withPartitioning());
            }

            for (final ColumnDefinition<?> column : tableDataDef.getColumns()) {
                final ColumnDefinition<?> existingDef = columns.get(column.getName());

                if (existingDef == null) {
                    columns.put(column.getName(), column);
                } else if (!existingDef.isCompatible(column)) {
                    // validate that both definitions are the same
                    throw new IllegalArgumentException(String.format(
                            "Column %s has conflicting definitions in table data and partitioning schemas: %s vs %s",
                            column.getName(), existingDef, column));
                }
            }

            tableDefinition = TableDefinition.of(columns.values());
        }

        @Override
        protected @NotNull TableLocation makeTableLocation(@NotNull final TableLocationKey locationKey) {
            if (!(locationKey instanceof TableLocationKeyImpl)) {
                throw new UnsupportedOperationException(String.format(
                        "%s: Unsupported TableLocationKey %s", this, locationKey));
            }
            return new TableLocationImpl((TableKeyImpl) getKey(), (TableLocationKeyImpl) locationKey);
        }

        @Override
        public void refresh() {
            TableKeyImpl key = (TableKeyImpl) getKey();
            backend.getTableLocations(tableDefinition, key, this::handleTableLocationKey);
        }

        @Override
        protected void activateUnderlyingDataSource() {
            TableKeyImpl key = (TableKeyImpl) getKey();
            final Subscription localSubscription = subscription = new Subscription();
            localSubscription.cancellationCallback = backend.subscribeToTableLocations(
                    tableDefinition, key, this::handleTableLocationKey);
            activationSuccessful(localSubscription);
        }

        @Override
        protected void deactivateUnderlyingDataSource() {
            final Subscription localSubscription = subscription;
            subscription = null;
            if (localSubscription != null) {
                localSubscription.cancellationCallback.close();
            }
        }

        @Override
        protected <T> boolean matchSubscriptionToken(final T token) {
            return token == subscription;
        }

        @Override
        public String getImplementationName() {
            return "PythonTableDataService.TableLocationProvider";
        }
    }

    /**
     * {@link TableLocationKey} implementation for TableService.
     */
    public static class TableLocationKeyImpl extends PartitionedTableLocationKey {

        private final PyObject locationKey;
        private int cachedHashCode;

        /**
         * Construct a TableLocationKeyImpl. Used by the Python adapter.
         *
         * @param locationKey the location key
         */
        @ScriptApi
        public TableLocationKeyImpl(@NotNull final PyObject locationKey) {
            this(locationKey, Map.of());
        }

        private TableLocationKeyImpl(
                @NotNull final PyObject locationKey,
                @NotNull final Map<String, Comparable<?>> partitionValues) {
            super(partitionValues);
            this.locationKey = locationKey;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TableLocationKeyImpl)) {
                return false;
            }
            final TableLocationKeyImpl otherTyped = (TableLocationKeyImpl) other;
            return partitions.equals((otherTyped).partitions) && locationKey.equals(otherTyped.locationKey);
        }

        @Override
        public int hashCode() {
            if (cachedHashCode == 0) {
                final int computedHashCode =
                        31 * partitions.hashCode() + Long.hashCode(locationKey.call("__hash__").getLongValue());
                // Don't use 0; that's used by StandaloneTableLocationKey, and also our sentinel for the need to compute
                if (computedHashCode == 0) {
                    final int fallbackHashCode = TableLocationKeyImpl.class.hashCode();
                    cachedHashCode = fallbackHashCode == 0 ? 1 : fallbackHashCode;
                } else {
                    cachedHashCode = computedHashCode;
                }
            }
            return cachedHashCode;
        }

        @Override
        public int compareTo(@NotNull final TableLocationKey other) {
            if (getClass() != other.getClass()) {
                throw new ClassCastException(String.format("Cannot compare %s to %s", getClass(), other.getClass()));
            }
            final TableLocationKeyImpl otherTableLocationKey = (TableLocationKeyImpl) other;
            return PartitionsComparator.INSTANCE.compare(partitions, otherTableLocationKey.partitions);
        }

        @Override
        public LogOutput append(@NotNull final LogOutput logOutput) {
            return logOutput.append(getImplementationName())
                    .append(":[key=").append(locationKey.toString())
                    .append(", partitions=").append(PartitionsFormatter.INSTANCE, partitions)
                    .append(']');
        }

        @Override
        public String toString() {
            return new LogOutputStringImpl().append(this).toString();
        }

        @Override
        public String getImplementationName() {
            return "PythonTableDataService.TableLocationKeyImpl";
        }
    }

    /**
     * {@link TableLocation} implementation for TableService.
     */
    public class TableLocationImpl extends AbstractTableLocation {

        volatile Subscription subscription = null;

        private long size;

        private TableLocationImpl(
                @NotNull final TableKeyImpl tableKey,
                @NotNull final TableLocationKeyImpl locationKey) {
            super(tableKey, locationKey, true);
        }

        private synchronized void onSizeChanged(final long newSize) {
            if (size == newSize) {
                return;
            }
            size = newSize;
            handleUpdate(RowSetFactory.flat(size), System.currentTimeMillis());
        }

        @Override
        protected @NotNull ColumnLocation makeColumnLocation(@NotNull final String name) {
            return new ColumnLocationImpl(this, name);
        }

        @Override
        public void refresh() {
            final TableKeyImpl key = (TableKeyImpl) getTableKey();
            final TableLocationKeyImpl location = (TableLocationKeyImpl) getKey();
            backend.getTableLocationSize(key, location, this::onSizeChanged);
        }

        @Override
        public @NotNull List<SortColumn> getSortedColumns() {
            return List.of();
        }

        @Override
        public @NotNull List<String[]> getDataIndexColumns() {
            return List.of();
        }

        @Override
        public boolean hasDataIndex(@NotNull final String... columns) {
            return false;
        }

        @Override
        public @Nullable BasicDataIndex loadDataIndex(@NotNull final String... columns) {
            return null;
        }

        @Override
        protected void activateUnderlyingDataSource() {
            final TableKeyImpl key = (TableKeyImpl) getTableKey();
            final TableLocationKeyImpl location = (TableLocationKeyImpl) getKey();

            final Subscription localSubscription = subscription = new Subscription();
            localSubscription.cancellationCallback = backend.subscribeToTableLocationSize(key, location, newSize -> {
                if (localSubscription != subscription) {
                    // we've been cancelled and/or replaced
                    return;
                }

                onSizeChanged(newSize);
            });
            activationSuccessful(localSubscription);
        }

        @Override
        protected void deactivateUnderlyingDataSource() {
            final Subscription localSubscription = subscription;
            subscription = null;
            if (localSubscription != null) {
                localSubscription.cancellationCallback.close();
            }
        }

        @Override
        protected <T> boolean matchSubscriptionToken(final T token) {
            return token == subscription;
        }

        @Override
        public String getImplementationName() {
            return "PythonTableDataService.TableLocationImpl";
        }
    }

    /**
     * {@link ColumnLocation} implementation for TableService.
     */
    public class ColumnLocationImpl extends AbstractColumnLocation {

        protected ColumnLocationImpl(
                @NotNull final PythonTableDataService.TableLocationImpl tableLocation,
                @NotNull final String name) {
            super(tableLocation, name);
        }

        @Override
        public boolean exists() {
            // Schema is consistent across all column locations with the same segment ID. This implementation should be
            // changed when/if we add support for rich schema evolution.
            return true;
        }

        @Override
        public ColumnRegionChar<Values> makeColumnRegionChar(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionChar<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));
        }

        @Override
        public ColumnRegionByte<Values> makeColumnRegionByte(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionByte<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));
        }

        @Override
        public ColumnRegionShort<Values> makeColumnRegionShort(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionShort<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));
        }

        @Override
        public ColumnRegionInt<Values> makeColumnRegionInt(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionInt<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));

        }

        @Override
        public ColumnRegionLong<Values> makeColumnRegionLong(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionLong<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));

        }

        @Override
        public ColumnRegionFloat<Values> makeColumnRegionFloat(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionFloat<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));
        }

        @Override
        public ColumnRegionDouble<Values> makeColumnRegionDouble(
                @NotNull final ColumnDefinition<?> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionDouble<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));
        }

        @Override
        public <TYPE> ColumnRegionObject<TYPE, Values> makeColumnRegionObject(
                @NotNull final ColumnDefinition<TYPE> columnDefinition) {
            return new AppendOnlyFixedSizePageRegionObject<>(REGION_MASK, pageSize,
                    new TableServiceGetRangeAdapter(columnDefinition));
        }

        private class TableServiceGetRangeAdapter implements AppendOnlyRegionAccessor<Values> {
            private final @NotNull ColumnDefinition<?> columnDefinition;

            public TableServiceGetRangeAdapter(@NotNull ColumnDefinition<?> columnDefinition) {
                this.columnDefinition = columnDefinition;
            }

            @Override
            public void readChunkPage(
                    final long firstRowPosition,
                    final int minimumSize,
                    @NotNull final WritableChunk<Values> destination) {
                final TableLocationImpl location = (TableLocationImpl) getTableLocation();
                final TableKeyImpl key = (TableKeyImpl) location.getTableKey();

                final List<WritableChunk<Values>> values = backend.getColumnValues(
                        key, (TableLocationKeyImpl) location.getKey(), columnDefinition,
                        firstRowPosition, minimumSize, destination.capacity());

                final int numRows = values.stream().mapToInt(WritableChunk::size).sum();

                if (numRows < minimumSize) {
                    throw new TableDataException(String.format("Not enough data returned. Read %d rows but minimum "
                            + "expected was %d. Result from get_column_values(%s, %s, %s, %d, %d).",
                            numRows, minimumSize, key.key, ((TableLocationKeyImpl) location.getKey()).locationKey,
                            columnDefinition.getName(), firstRowPosition, minimumSize));
                }
                if (numRows > destination.capacity()) {
                    throw new TableDataException(String.format("Too much data returned. Read %d rows but maximum "
                            + "expected was %d. Result from get_column_values(%s, %s, %s, %d, %d).",
                            numRows, destination.capacity(), key.key,
                            ((TableLocationKeyImpl) location.getKey()).locationKey, columnDefinition.getName(),
                            firstRowPosition, minimumSize));
                }

                int offset = 0;
                for (final Chunk<Values> rbChunk : values) {
                    final int length = Math.min(destination.capacity() - offset, rbChunk.size());
                    destination.copyFromChunk(rbChunk, 0, offset, length);
                    offset += length;
                }
                destination.setSize(offset);
            }

            @Override
            public long size() {
                return getTableLocation().getSize();
            }
        }
    }

    private static class Subscription {
        SafeCloseable cancellationCallback;
    }
}
