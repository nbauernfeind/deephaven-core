/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit IntTransfer and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.LongChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.util.QueryConstants;
import org.apache.parquet.column.statistics.LongStatistics;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.nio.LongBuffer;

public class LongTransfer implements TransferObject<LongBuffer> {

    private final ColumnSource<?> columnSource;
    private final ChunkSource.GetContext context;
    private LongChunk<? extends Values> chunk;
    private LongBuffer buffer;
    private long minValue = QueryConstants.NULL_LONG;
    private long maxValue = QueryConstants.NULL_LONG;

    public LongTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = LongBuffer.allocate(targetSize);
        this.context = columnSource.makeGetContext(targetSize);
    }

    @Override
    public LongBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int rowCount() {
        return chunk.size();
    }

    @Override
    public void fetchData(@NotNull final RowSequence rs) {
        chunk = columnSource.getChunk(context, rs).asLongChunk();
        if(buffer.capacity() < chunk.size()) {
            buffer = LongBuffer.allocate(chunk.size());
        }

        buffer.clear();
        for (int ii = 0; ii < chunk.size(); ii++) {
            final long val = chunk.get(ii);
            if(val != QueryConstants.NULL_LONG) {
                if (minValue == QueryConstants.NULL_LONG) {
                    minValue = maxValue = val;
                } else if (val < minValue) {
                    minValue = val;
                } else if (val > maxValue) {
                    maxValue = val;
                }
            }

            buffer.put(val);
        }
        buffer.flip();
    }

    @Override
    public void close() {
        context.close();
    }

    @Override
    public <T extends Comparable<T>> void updateStatistics(@NotNull final Statistics<T> stats) {
        if(minValue != QueryConstants.NULL_LONG) {
            ((LongStatistics) stats).setMinMax(minValue, maxValue);
        }
    }
}