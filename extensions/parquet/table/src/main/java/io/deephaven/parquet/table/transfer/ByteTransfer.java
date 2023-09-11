/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit IntTransfer and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.ByteChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.util.QueryConstants;
import org.apache.parquet.column.statistics.IntStatistics;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.nio.IntBuffer;

public class ByteTransfer implements TransferObject<IntBuffer> {

    private final ColumnSource<?> columnSource;
    private final ChunkSource.GetContext context;
    private ByteChunk<? extends Values> chunk;
    private IntBuffer buffer;
    private byte minValue = QueryConstants.NULL_BYTE;
    private byte maxValue = QueryConstants.NULL_BYTE;

    public ByteTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
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
        chunk = columnSource.getChunk(context, rs).asByteChunk();
        if(buffer.capacity() < chunk.size()) {
            buffer = IntBuffer.allocate(chunk.size());
        }

        buffer.clear();
        for (int ii = 0; ii < chunk.size(); ii++) {
            final byte val = chunk.get(ii);
            if(val != QueryConstants.NULL_BYTE) {
                if (minValue == QueryConstants.NULL_BYTE) {
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
        if(minValue != QueryConstants.NULL_BYTE) {
            ((IntStatistics) stats).setMinMax(minValue, maxValue);
        }
    }
}