/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.IntChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.util.QueryConstants;
import org.apache.parquet.column.statistics.IntStatistics;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.nio.IntBuffer;

public class IntTransfer implements TransferObject<IntBuffer> {

    private final ColumnSource<?> columnSource;
    private final ChunkSource.GetContext context;
    private IntChunk<? extends Values> chunk;
    private IntBuffer buffer;
    private int minValue = QueryConstants.NULL_INT;
    private int maxValue = QueryConstants.NULL_INT;

    public IntTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = IntBuffer.allocate(targetSize);
        this.context = columnSource.makeGetContext(targetSize);
    }

    @Override
    public IntBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int rowCount() {
        return chunk.size();
    }

    @Override
    public void fetchData(@NotNull final RowSequence rs) {
        chunk = columnSource.getChunk(context, rs).asIntChunk();
        if(buffer.capacity() < chunk.size()) {
            buffer = IntBuffer.allocate(chunk.size());
        }

        buffer.clear();
        for (int ii = 0; ii < chunk.size(); ii++) {
            final int val = chunk.get(ii);
            if(val != QueryConstants.NULL_INT) {
                if (minValue == QueryConstants.NULL_INT) {
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
        if(minValue != QueryConstants.NULL_INT) {
            ((IntStatistics) stats).setMinMax(minValue, maxValue);
        }
    }
}