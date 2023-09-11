/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit IntTransfer and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.FloatChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.util.QueryConstants;
import org.apache.parquet.column.statistics.FloatStatistics;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;

public class FloatTransfer implements TransferObject<FloatBuffer> {

    private final ColumnSource<?> columnSource;
    private final ChunkSource.GetContext context;
    private FloatChunk<? extends Values> chunk;
    private FloatBuffer buffer;
    private float minValue = QueryConstants.NULL_FLOAT;
    private float maxValue = QueryConstants.NULL_FLOAT;

    public FloatTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = FloatBuffer.allocate(targetSize);
        this.context = columnSource.makeGetContext(targetSize);
    }

    @Override
    public FloatBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int rowCount() {
        return chunk.size();
    }

    @Override
    public void fetchData(@NotNull final RowSequence rs) {
        chunk = columnSource.getChunk(context, rs).asFloatChunk();
        if(buffer.capacity() < chunk.size()) {
            buffer = FloatBuffer.allocate(chunk.size());
        }

        buffer.clear();
        for (int ii = 0; ii < chunk.size(); ii++) {
            final float val = chunk.get(ii);
            if(val != QueryConstants.NULL_FLOAT) {
                if (minValue == QueryConstants.NULL_FLOAT) {
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
        if(minValue != QueryConstants.NULL_FLOAT) {
            ((FloatStatistics) stats).setMinMax(minValue, maxValue);
        }
    }
}