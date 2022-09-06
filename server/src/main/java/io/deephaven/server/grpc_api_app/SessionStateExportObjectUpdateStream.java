package io.deephaven.server.grpc_api_app;

import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.ColumnDefinition;
import io.deephaven.engine.table.TableDefinition;
import io.deephaven.engine.table.impl.sources.ArrayBackedColumnSource;
import io.deephaven.proto.backplane.grpc.ExportNotification;
import io.deephaven.qst.type.Type;
import io.deephaven.stream.StreamConsumer;
import io.deephaven.stream.StreamPublisher;
import io.deephaven.stream.StreamToTableAdapter;
import io.deephaven.time.DateTimeUtils;
import io.deephaven.util.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

final class SessionStateExportObjectUpdateStream implements StreamPublisher {

    private static final TableDefinition DEFINITION = TableDefinition.of(
            ColumnDefinition.ofTime("Time"),
            ColumnDefinition.ofString("SessionId"),
            ColumnDefinition.ofString("ExportId"),
            ColumnDefinition.ofBoolean("IsNonExport"),
            ColumnDefinition.ofString("State"),
            ColumnDefinition.ofBoolean("IsLive"),
            ColumnDefinition.ofString("Description"),
            ColumnDefinition.of("Dependencies", Type.find(String[].class)));
    private static final int CHUNK_SIZE = ArrayBackedColumnSource.BLOCK_SIZE;

    public static TableDefinition definition() {
        return DEFINITION;
    }

    private WritableChunk<Values>[] chunks;
    private StreamConsumer consumer;

    SessionStateExportObjectUpdateStream() {
        // noinspection unchecked
        chunks = StreamToTableAdapter.makeChunksForDefinition(DEFINITION, CHUNK_SIZE);
    }

    @Override
    public void register(@NotNull StreamConsumer consumer) {
        if (this.consumer != null) {
            throw new IllegalStateException("Can not register multiple StreamConsumers.");
        }
        this.consumer = Objects.requireNonNull(consumer);
    }

    public synchronized void onExportObjectStateUpdate(
            String sessionId, String exportId, String description, ExportNotification.State state, boolean isLive,
            boolean isNonExport, List<String> remainingParents) {
        chunks[0].asWritableLongChunk().add(DateTimeUtils.currentTime().getNanos());
        chunks[1].asWritableObjectChunk().add(sessionId);
        chunks[2].asWritableObjectChunk().add(exportId);
        chunks[3].asWritableByteChunk().add(BooleanUtils.booleanAsByte(isNonExport));
        chunks[4].asWritableObjectChunk().add(state.name());
        chunks[5].asWritableByteChunk().add(BooleanUtils.booleanAsByte(isLive));
        chunks[6].asWritableObjectChunk().add(description);
        chunks[7].asWritableObjectChunk().add(remainingParents.toArray(String[]::new));

        if (chunks[0].size() == CHUNK_SIZE) {
            flushInternal();
        }
    }

    @Override
    public synchronized void flush() {
        if (chunks[0].size() == 0) {
            return;
        }
        flushInternal();
    }

    private void flushInternal() {
        consumer.accept(chunks);
        // noinspection unchecked
        chunks = StreamToTableAdapter.makeChunksForDefinition(DEFINITION, CHUNK_SIZE);
    }
}
