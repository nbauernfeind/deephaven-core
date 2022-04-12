/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.table.impl;

import io.deephaven.chunk.LongChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.WritableLongChunk;
import io.deephaven.chunk.WritableObjectChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.rowset.RowSetBuilderRandom;
import io.deephaven.engine.rowset.RowSetFactory;
import io.deephaven.engine.rowset.WritableRowSet;
import io.deephaven.engine.rowset.chunkattributes.RowKeys;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.impl.perf.PerformanceEntry;
import io.deephaven.engine.table.impl.perf.UpdatePerformanceTracker;
import io.deephaven.engine.table.impl.replay.Replayer;
import io.deephaven.engine.table.impl.sources.FillUnordered;
import io.deephaven.engine.updategraph.UpdateGraphProcessor;
import io.deephaven.engine.updategraph.UpdateSourceRegistrar;
import io.deephaven.engine.util.TableTools;
import io.deephaven.function.LongNumericPrimitives;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import io.deephaven.time.DateTime;
import io.deephaven.time.DateTimeUtils;
import io.deephaven.time.TimeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.deephaven.util.type.TypeUtils.box;

/**
 * A TimeTable adds rows at a fixed interval with a single column named "Timestamp".
 *
 * To create a TimeTable, you should use the {@link TableTools#timeTable} family of methods.
 */
public class TimeTable extends QueryTable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TimeTable.class);

    public static class Builder {
        private UpdateSourceRegistrar registrar = UpdateGraphProcessor.DEFAULT;
        private TimeProvider timeProvider;
        private DateTime startTime;
        private long period;
        private boolean isStreamTable;

        public Builder withRegistrar(UpdateSourceRegistrar registrar) {
            this.registrar = registrar;
            return this;
        }

        public Builder withTimeProvider(TimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        public Builder withStartTime(DateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder withStartTime(String startTime) {
            this.startTime = DateTimeUtils.convertDateTime(startTime);
            return this;
        }

        public Builder withPeriod(long period) {
            this.period = period;
            return this;
        }

        public Builder withPeriod(String period) {
            this.period = DateTimeUtils.expressionToNanos(period);
            return this;
        }

        public Builder asStreamTable() {
            this.isStreamTable = true;
            return this;
        }

        public QueryTable build() {
            return new TimeTable(registrar,
                    timeProvider == null ? Replayer.getTimeProvider(null) : timeProvider,
                    startTime, period, isStreamTable);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private static final String TIMESTAMP = "Timestamp";
    private long lastIndex = -1;
    private final SyntheticDateTimeSource columnSource;
    private final TimeProvider timeProvider;
    private final PerformanceEntry entry;
    private final boolean isStreamTable;

    public TimeTable(UpdateSourceRegistrar registrar, TimeProvider timeProvider,
            @Nullable DateTime startTime, long period, boolean isStreamTable) {
        super(RowSetFactory.empty().toTracking(), initColumn(startTime, period));
        this.isStreamTable = isStreamTable;
        final String name = isStreamTable ? "TimeTableStream" : "TimeTable";
        this.entry = UpdatePerformanceTracker.getInstance().getEntry(name + "(" + startTime + "," + period + ")");
        columnSource = (SyntheticDateTimeSource) getColumnSourceMap().get(TIMESTAMP);
        this.timeProvider = timeProvider;
        if (isStreamTable) {
            setAttribute(Table.STREAM_TABLE_ATTRIBUTE, Boolean.TRUE);
        } else {
            setAttribute(Table.ADD_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);
            setFlat();
        }
        if (startTime != null) {
            refresh(false);
        }
        registrar.addSource(this);
    }

    private static Map<String, ColumnSource<?>> initColumn(DateTime firstTime, long period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Invalid time period: " + period + " nanoseconds");
        }
        return Collections.singletonMap(TIMESTAMP, new SyntheticDateTimeSource(firstTime, period));
    }

    @Override
    public void run() {
        refresh(true);
    }

    private void refresh(final boolean notifyListeners) {
        entry.onUpdateStart();
        try {
            final DateTime dateTime = timeProvider.currentTime();
            long rangeStart = lastIndex + 1;
            if (columnSource.startTime == null) {
                lastIndex = 0;
                columnSource.startTime = new DateTime(
                        LongNumericPrimitives.lowerBin(dateTime.getNanos(), columnSource.period));
            } else {
                lastIndex = Math.max(lastIndex,
                        DateTimeUtils.minus(dateTime, columnSource.startTime) / columnSource.period);
            }

            if (rangeStart <= lastIndex) {
                final RowSet addedRange = RowSetFactory.fromRange(rangeStart, lastIndex);
                final RowSet removedRange = isStreamTable && rangeStart > 0
                        ? RowSetFactory.fromRange(getRowSet().firstRowKey(), rangeStart - 1) : RowSetFactory.empty();
                if (isStreamTable && rangeStart > 0) {
                    getRowSet().writableCast().removeRange(0, rangeStart - 1);
                }
                getRowSet().writableCast().insertRange(rangeStart, lastIndex);
                if (notifyListeners) {
                    notifyListeners(addedRange, removedRange, RowSetFactory.empty());
                }
            }
        } finally {
            entry.onUpdateEnd();
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        UpdateGraphProcessor.DEFAULT.removeSource(this);
    }

    private static class SyntheticDateTimeSource extends AbstractColumnSource<DateTime> implements
            ImmutableColumnSourceGetDefaults.LongBacked<DateTime>,
            FillUnordered {

        private DateTime startTime;
        private final long period;

        private SyntheticDateTimeSource(DateTime startTime, long period) {
            super(DateTime.class);
            this.startTime = startTime;
            this.period = period;
        }

        @Override
        public DateTime get(long rowKey) {
            return DateTimeUtils.plus(startTime, period * rowKey);
        }

        @Override
        public long getLong(long rowKey) {
            return startTime.getNanos() + period * rowKey;
        }

        @Override
        public WritableRowSet match(boolean invertMatch, boolean usePrev, boolean caseInsensitive, RowSet selection,
                Object... keys) {
            if (startTime == null) {
                // there are no valid rows for this column source yet
                return RowSetFactory.empty();
            }

            final RowSetBuilderRandom matchingSet = RowSetFactory.builderRandom();

            for (Object o : keys) {
                if (!(o instanceof DateTime)) {
                    continue;
                }
                final DateTime key = (DateTime) o;

                if (key.getNanos() % period != startTime.getNanos() % period || DateTimeUtils.isBefore(key, startTime)) {
                    continue;
                }

                matchingSet.addKey(DateTimeUtils.minus(key, startTime) / period);
            }

            if (invertMatch) {
                try (final WritableRowSet matching = matchingSet.build()) {
                    return selection.minus(matching);
                }
            }

            final WritableRowSet matching = matchingSet.build();
            matching.retain(selection);
            return matching;
        }

        @Override
        public Map<DateTime, RowSet> getValuesMapping(RowSet subRange) {
            final Map<DateTime, RowSet> result = new LinkedHashMap<>();
            subRange.forAllRowKeys(
                    ii -> result.put(get(ii), RowSetFactory.fromKeys(ii)));
            return result;
        }

        @Override
        public <ALTERNATE_DATA_TYPE> boolean allowsReinterpret(
                @NotNull final Class<ALTERNATE_DATA_TYPE> alternateDataType) {
            return alternateDataType == long.class;
        }

        @Override
        public <ALTERNATE_DATA_TYPE> ColumnSource<ALTERNATE_DATA_TYPE> doReinterpret(
                @NotNull Class<ALTERNATE_DATA_TYPE> alternateDataType) {
            // noinspection unchecked
            return (ColumnSource<ALTERNATE_DATA_TYPE>) new SyntheticDateTimeAsLongSource();
        }

        @Override
        public void fillChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
                @NotNull LongChunk<? extends RowKeys> keys) {
            final WritableObjectChunk<DateTime, ? super Values> objectDest = dest.asWritableObjectChunk();
            objectDest.setSize(keys.size());

            for (int ii = 0; ii < keys.size(); ++ii) {
                objectDest.set(ii, get(keys.get(ii)));
            }
        }

        @Override
        public void fillPrevChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
                @NotNull LongChunk<? extends RowKeys> keys) {
            fillChunkUnordered(context, dest, keys);
        }

        @Override
        public boolean providesFillUnordered() {
            return true;
        }

        private class SyntheticDateTimeAsLongSource extends AbstractColumnSource<Long> implements
                ImmutableColumnSourceGetDefaults.LongBacked<Long>,
                FillUnordered {

            SyntheticDateTimeAsLongSource() {
                super(Long.class);
            }

            @Override
            public Long get(long rowKey) {
                return box(getLong(rowKey));
            }

            @Override
            public long getLong(long rowKey) {
                return startTime.getNanos() + period * rowKey;
            }

            @Override
            public WritableRowSet match(boolean invertMatch, boolean usePrev, boolean caseInsensitive, RowSet selection,
                    Object... keys) {
                if (startTime == null) {
                    // there are no valid rows for this column source yet
                    return RowSetFactory.empty();
                }

                final RowSetBuilderRandom matchingSet = RowSetFactory.builderRandom();

                for (Object o : keys) {
                    if (!(o instanceof Long)) {
                        continue;
                    }
                    final long key = (Long) o;

                    if (key % period != startTime.getNanos() % period || key < startTime.getNanos()) {
                        continue;
                    }

                    matchingSet.addKey((key - startTime.getNanos()) / period);
                }

                if (invertMatch) {
                    try (final WritableRowSet matching = matchingSet.build()) {
                        return selection.minus(matching);
                    }
                }

                final WritableRowSet matching = matchingSet.build();
                matching.retain(selection);
                return matching;
            }

            @Override
            public Map<Long, RowSet> getValuesMapping(RowSet subRange) {
                final Map<Long, RowSet> result = new LinkedHashMap<>();
                subRange.forAllRowKeys(
                        ii -> result.put(box(startTime.getNanos() + period * ii), RowSetFactory.fromKeys(ii)));
                return result;
            }

            @Override
            public <ALTERNATE_DATA_TYPE> boolean allowsReinterpret(
                    @NotNull final Class<ALTERNATE_DATA_TYPE> alternateDataType) {
                return alternateDataType == DateTime.class;
            }

            @Override
            public <ALTERNATE_DATA_TYPE> ColumnSource<ALTERNATE_DATA_TYPE> doReinterpret(
                    @NotNull Class<ALTERNATE_DATA_TYPE> alternateDataType) {
                // noinspection unchecked
                return (ColumnSource<ALTERNATE_DATA_TYPE>) SyntheticDateTimeSource.this;
            }

            @Override
            public void fillChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
                    @NotNull LongChunk<? extends RowKeys> keys) {
                final WritableLongChunk<? super Values> longDest = dest.asWritableLongChunk();
                longDest.setSize(keys.size());

                for (int ii = 0; ii < keys.size(); ++ii) {
                    longDest.set(ii, get(keys.get(ii)));
                }
            }

            @Override
            public void fillPrevChunkUnordered(@NotNull FillContext context,
                    @NotNull WritableChunk<? super Values> dest, @NotNull LongChunk<? extends RowKeys> keys) {
                fillChunkUnordered(context, dest, keys);
            }

            @Override
            public boolean providesFillUnordered() {
                return true;
            }
        }
    }
}
