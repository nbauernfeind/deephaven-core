/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit IntTransfer and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.DoubleChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.util.QueryConstants;
import org.apache.parquet.column.statistics.DoubleStatistics;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.nio.DoubleBuffer;

public class DoubleTransfer implements TransferObject<DoubleBuffer> {

    private final ColumnSource<?> columnSource;
    private final ChunkSource.GetContext context;
    private DoubleChunk<? extends Values> chunk;
    private DoubleBuffer buffer;
    private double minValue = QueryConstants.NULL_DOUBLE;
    private double maxValue = QueryConstants.NULL_DOUBLE;

    public DoubleTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = DoubleBuffer.allocate(targetSize);
        this.context = columnSource.makeGetContext(targetSize);
    }

    @Override
    public DoubleBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int rowCount() {
        return chunk.size();
    }

    @Override
    public void fetchData(@NotNull final RowSequence rs) {
        chunk = columnSource.getChunk(context, rs).asDoubleChunk();
        if(buffer.capacity() < chunk.size()) {
            buffer = DoubleBuffer.allocate(chunk.size());
        }

        buffer.clear();
        for (int ii = 0; ii < chunk.size(); ii++) {
            final double val = chunk.get(ii);
            if(val != QueryConstants.NULL_DOUBLE) {
                if (minValue == QueryConstants.NULL_DOUBLE) {
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
        if(minValue != QueryConstants.NULL_DOUBLE) {
            ((DoubleStatistics) stats).setMinMax(minValue, maxValue);
        }
    }
}