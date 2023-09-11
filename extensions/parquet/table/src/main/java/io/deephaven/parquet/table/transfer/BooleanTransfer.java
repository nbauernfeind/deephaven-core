/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.ByteChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BooleanTransfer implements TransferObject<ByteBuffer> {

    private final ColumnSource<?> columnSource;
    private final ChunkSource.GetContext context;
    private ByteChunk<? extends Values> chunk;
    private ByteBuffer buffer;

    public BooleanTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = ByteBuffer.allocate(targetSize);
        this.context = columnSource.makeGetContext(targetSize);
    }

    @Override
    public ByteBuffer getBuffer() {
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
            buffer = ByteBuffer.allocate(chunk.size());
        }

        buffer.clear();
        for (int ii = 0; ii < chunk.size(); ii++) {
            final byte val = chunk.get(ii);
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
    }
}