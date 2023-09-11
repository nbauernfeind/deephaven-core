/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.ObjectChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.util.codec.ObjectCodec;
import org.apache.parquet.column.statistics.Statistics;
import org.apache.parquet.io.api.Binary;
import org.jetbrains.annotations.NotNull;

public class CodecTransfer<T> implements TransferObject<Binary[]> {

    private final ChunkSource.GetContext context;
    private final ObjectCodec<? super T> codec;
    private ObjectChunk<T, Values> chunk;
    private final Binary[] buffer;
    private final ColumnSource<T> columnSource;


    public CodecTransfer(
            @NotNull final ColumnSource<T> columnSource,
            @NotNull final ObjectCodec<? super T> codec,
            final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = new Binary[targetSize];
        context = this.columnSource.makeGetContext(targetSize);
        this.codec = codec;
    }

    @Override
    public Binary[] getBuffer() {
        return buffer;
    }

    @Override
    public int rowCount() {
        return chunk.size();
    }

    @Override
    public void fetchData(@NotNull final RowSequence rs) {
        // noinspection unchecked
        chunk = (ObjectChunk<T, Values>) columnSource.getChunk(context, rs);
        for (int i = 0; i < chunk.size(); i++) {
            T value = chunk.get(i);
            buffer[i] = value == null ? null : Binary.fromConstantByteArray(codec.encode(value));
        }
    }

    @Override
    public void close() {
        context.close();
    }

    @Override
    public <T extends Comparable<T>> void updateStatistics(@NotNull final Statistics<T> stats) {

    }
}
