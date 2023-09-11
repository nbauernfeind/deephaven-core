/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.chunk.ObjectChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import org.apache.parquet.column.statistics.Statistics;
import org.apache.parquet.io.api.Binary;
import org.jetbrains.annotations.NotNull;

public class StringTransfer implements TransferObject<Binary[]> {

    private final ChunkSource.GetContext context;
    private ObjectChunk<String, Values> chunk;
    private final Binary[] buffer;
    private final ColumnSource<?> columnSource;


    public StringTransfer(@NotNull final ColumnSource<?> columnSource, final int targetSize) {
        this.columnSource = columnSource;
        this.buffer = new Binary[targetSize];
        context = this.columnSource.makeGetContext(targetSize);
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
        chunk = (ObjectChunk<String, Values>) columnSource.getChunk(context, rs);
        for (int i = 0; i < chunk.size(); i++) {
            final String value = chunk.get(i);
            buffer[i] = value == null ? null : Binary.fromString(value);
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
