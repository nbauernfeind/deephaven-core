package io.deephaven.grpc_api.arrow;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;
import com.google.rpc.Code;
import io.deephaven.UncheckedDeephavenException;
import io.deephaven.barrage.flatbuf.BarragePutMetadata;
import io.deephaven.barrage.flatbuf.Buffer;
import io.deephaven.barrage.flatbuf.FieldNode;
import io.deephaven.barrage.flatbuf.Message;
import io.deephaven.barrage.flatbuf.MessageHeader;
import io.deephaven.barrage.flatbuf.RecordBatch;
import io.deephaven.barrage.flatbuf.Schema;
import io.deephaven.datastructures.util.CollectionUtil;
import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.TableDefinition;
import io.deephaven.db.util.LongSizedDataStructure;
import io.deephaven.db.util.ScriptSession;
import io.deephaven.db.util.liveness.LivenessArtifact;
import io.deephaven.db.v2.BaseTable;
import io.deephaven.db.v2.remote.ConstructSnapshot;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.db.v2.sources.chunk.ChunkType;
import io.deephaven.db.v2.utils.BarrageMessage;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.IndexShiftData;
import io.deephaven.grpc_api.barrage.BarrageStreamGenerator;
import io.deephaven.grpc_api.barrage.BarrageStreamReader;
import io.deephaven.grpc_api.barrage.util.BarrageSchemaUtil;
import io.deephaven.grpc_api.console.ConsoleServiceGrpcImpl;
import io.deephaven.grpc_api.session.SessionService;
import io.deephaven.grpc_api.session.SessionState;
import io.deephaven.grpc_api.util.GrpcUtil;
import io.deephaven.grpc_api_client.barrage.chunk.ChunkInputStreamGenerator;
import io.deephaven.grpc_api_client.table.BarrageSourcedTable;
import io.deephaven.grpc_api_client.util.BarrageProtoUtil;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import io.deephaven.proto.backplane.grpc.Barrage;
import io.deephaven.proto.backplane.grpc.BarrageData;
import io.grpc.stub.StreamObserver;
import org.apache.arrow.flight.impl.Flight;
import org.apache.arrow.flight.impl.FlightServiceGrpc;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class FlightServiceGrpcImpl extends FlightServiceGrpc.FlightServiceImplBase {
    // TODO (core#54): use app_metadata to communicate serialization options
    private static final ChunkInputStreamGenerator.Options DEFAULT_DESER_OPTIONS = new ChunkInputStreamGenerator.Options.Builder()
            .setUseDeephavenNulls(true)
            .build();

    private static final Logger log = LoggerFactory.getLogger(FlightServiceGrpcImpl.class);

    private final SessionService sessionService;
    private final ConsoleServiceGrpcImpl consoleService;

    @Inject()
    public FlightServiceGrpcImpl(final SessionService sessionService,
                                 final ConsoleServiceGrpcImpl consoleService) {
        this.sessionService = sessionService;
        this.consoleService = consoleService;
    }

    @Override
    public void listFlights(final Flight.Criteria request, final StreamObserver<Flight.FlightInfo> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final ScriptSession script = consoleService.getGlobalSession();

            if (script != null) {
                script.getVariables().forEach((name, value) -> {
                    if (value instanceof Table) {
                        responseObserver.onNext(getFlightInfo(name, (Table) value));
                    }
                });
            }

            responseObserver.onCompleted();
        });
    }

    @Override
    public void getFlightInfo(final Flight.FlightDescriptor request, final StreamObserver<Flight.FlightInfo> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();
            final SessionState.ExportObject<Table> export = getExportFromDescriptor(session, request);
            session.nonExport()
                    .require(export)
                    .onError(responseObserver::onError)
                    .submit(() -> {
//                        responseObserver.onNext(getFlightInfo(export.get()));
                        responseObserver.onCompleted();
                    });
        });
    }

    @Override
    public void getSchema(final Flight.FlightDescriptor request, final StreamObserver<Flight.SchemaResult> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();
            final SessionState.ExportObject<Table> export = getExportFromDescriptor(session, request);
            session.nonExport()
                    .require(export)
                    .onError(responseObserver::onError)
                    .submit(() -> {
                        responseObserver.onNext(Flight.SchemaResult.newBuilder()
                                .setSchema(schemaBytesFromTable(export.get()))
                                .build());
                        responseObserver.onCompleted();
                    });
        });
    }

    public void doGetCustom(final Flight.Ticket request, final StreamObserver<InputStream> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();
            final SessionState.ExportObject<BaseTable> export = session.getExport(request);
            session.nonExport()
                    .require(export)
                    .submit(() -> {
                        final BaseTable table = export.get();

                        // Send Schema wrapped in Message
                        final String[] columnNames = table.getDefinition().getColumnNamesArray();
                        final ColumnSource<?>[] columnSources = table.getColumnSources().toArray(ColumnSource.ZERO_LENGTH_COLUMN_SOURCE_ARRAY);
                        final FlatBufferBuilder builder = new FlatBufferBuilder();
                        final int schemaOffset = BarrageSchemaUtil.makeSchemaPayload(builder, columnNames, columnSources, table.getAttributes());
                        builder.finish(BarrageStreamGenerator.wrapInMessage(builder, schemaOffset, BarrageStreamGenerator.SCHEMA_TYPE_ID));
                        final ByteBuffer serializedMessage = builder.dataBuffer();

                        final byte[] msgBytes = BarrageData.newBuilder()
                                .setDataHeader(ByteString.copyFrom(serializedMessage.array(), serializedMessage.position(), serializedMessage.remaining()))
                                .build()
                                .toByteArray();
                        responseObserver.onNext(new BarrageStreamGenerator.DrainableByteArrayInputStream(msgBytes, 0, msgBytes.length));

                        // get ourselves some data!
                        final BitSet colSet = new BitSet(columnNames.length);
                        for (int i = 0; i < columnNames.length; ++i) {
                            colSet.set(i);
                        }
                        final BarrageMessage msg = ConstructSnapshot.constructBackplaneSnapshotInPositionSpace(this, table, colSet, Index.FACTORY.getFlatIndex(Long.MAX_VALUE));

                        final BarrageStreamGenerator bsg = new BarrageStreamGenerator(msg);
                        try {
                            responseObserver.onNext(bsg.getDoGetInputStream(bsg.getSubView(DEFAULT_DESER_OPTIONS, false, null, null, colSet)));
                        } catch (final IOException e) {
                            throw new UncheckedDeephavenException(e); // unexpected
                        }

                        responseObserver.onCompleted();
                    });
        });
    }

    public StreamObserver<InputStream> doPutCustom(final StreamObserver<Flight.PutResult> responseObserver) {
        return GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();

            return new StreamObserver<InputStream>() {
                private final PutMarshaller marshaller = new PutMarshaller(session, responseObserver);
                {
                    session.manage(marshaller);
                }

                @Override
                public void onNext(final InputStream request) {
                    GrpcUtil.rpcWrapper(log, responseObserver, () -> {
                        try {
                            marshaller.parseNext(parseProtoMessage(request));
                        } catch (final IOException unexpected) {
                            throw GrpcUtil.securelyWrapError(log, unexpected);
                        }
                    });
                }

                @Override
                public void onError(final Throwable t) {
                    // ok; we're done then
                    marshaller.resultExport.submit(() -> { throw new UncheckedDeephavenException(t); });
                }

                @Override
                public void onCompleted() {
                    session.unmanageNonExport(marshaller);
                }
            };
        });
    }

    // client side is out-of-band
    public void doPutCustom(final InputStream request, final StreamObserver<Flight.PutResult> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();

            final MessageInfo mi;
            try {
                mi = parseProtoMessage(request);
            } catch (final IOException unexpected) {
                throw GrpcUtil.securelyWrapError(log, unexpected);
            }
            final BarragePutMetadata app_metadata = mi.app_metadata;
            if (app_metadata == null) {
                throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "no app_metadata provided");
            }
            final ByteBuffer ticketBuf = app_metadata.rpcTicketAsByteBuffer();
            if (ticketBuf == null) {
                throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "no rpc ticket provided");
            }

            final SessionState.ExportBuilder<PutMarshaller> putExport = session.newExport(GrpcUtil.byteStringToLong(ticketBuf));

            putExport.submit(() -> {
                final PutMarshaller put = new PutMarshaller(session, responseObserver);
                put.parseNext(mi);
                return put;
            });
        });
    }

    public void doPutUpdateCustom(final InputStream request, final StreamObserver<Flight.OOBPutResult> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();

            final MessageInfo mi;
            try {
                mi = parseProtoMessage(request);
            } catch (final IOException unexpected) {
                throw GrpcUtil.securelyWrapError(log, unexpected);
            }

            final BarragePutMetadata app_metadata = mi.app_metadata;
            if (app_metadata == null) {
                throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "no app_metadata provided");
            }
            final ByteBuffer ticketBuf = app_metadata.rpcTicketAsByteBuffer();
            if (ticketBuf == null) {
                throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "no rpc ticket provided");
            }

            final SessionState.ExportObject<PutMarshaller> putExport = session.getExport(GrpcUtil.byteStringToLong(ticketBuf));

            session.nonExport()
                    .require(putExport)
                    .onError(responseObserver::onError)
                    .submit(() -> {
                        putExport.get().parseNext(mi);
                        responseObserver.onNext(Flight.OOBPutResult.getDefaultInstance()); // nothing to report
                        responseObserver.onCompleted();
                    });
        });
    }

    private static SessionState.ExportObject<Table> getExportFromDescriptor(final SessionState session, final Flight.FlightDescriptor request) {
        if (request.getType() != Flight.FlightDescriptor.DescriptorType.PATH
                || request.getPathCount() != 2
                || !request.getPath(0).equals("ticket")) {
            throw GrpcUtil.statusRuntimeException(Code.NOT_FOUND, "no flight found");
        }

        final long exportId;
        try {
            exportId = Long.parseLong(request.getPath(1));
        } catch (final NumberFormatException error) {
            throw GrpcUtil.statusRuntimeException(Code.NOT_FOUND, "no flight found");
        }

        return session.getExport(exportId);
    }

    private Flight.FlightInfo getFlightInfo(final String name, final Table table) {
        return Flight.FlightInfo.newBuilder()
                .setSchema(schemaBytesFromTable(table))
                .setFlightDescriptor(Flight.FlightDescriptor.newBuilder()
                        .setType(Flight.FlightDescriptor.DescriptorType.PATH)
                        .addPath("scope")
                        .addPath(name)
                        .build())
                .addEndpoint(Flight.FlightEndpoint.newBuilder()
                        .setTicket(Flight.Ticket.newBuilder().setTicket(ByteString.copyFromUtf8("s" + name)).build())
                        .build())
                .setTotalRecords(table.isLive() ? -1 : table.size())
                .setTotalBytes(-1)
                .build();
    }

    private static ByteString schemaBytesFromTable(final Table table) {
        final String[] columnNames = table.getDefinition().getColumnNamesArray();
        final ColumnSource<?>[] columnSources = table.getColumnSources().toArray(ColumnSource.ZERO_LENGTH_COLUMN_SOURCE_ARRAY);
        final FlatBufferBuilder builder = new FlatBufferBuilder();
        builder.finish(BarrageSchemaUtil.makeSchemaPayload(builder, columnNames, columnSources, table.getAttributes()));
        return ByteString.copyFrom(builder.dataBuffer());
    }

    private static final int BODY_TAG =
            BarrageStreamReader.makeTag(Flight.FlightData.DATA_BODY_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    private static final int DATA_HEADER_TAG =
            BarrageStreamReader.makeTag(Flight.FlightData.DATA_HEADER_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    private static final int APP_METADATA_TAG =
            BarrageStreamReader.makeTag(Flight.FlightData.APP_METADATA_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    private static final int FLIGHT_DESCRIPTOR_TAG =
            BarrageStreamReader.makeTag(Flight.FlightData.FLIGHT_DESCRIPTOR_FIELD_NUMBER, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    private static final BarrageMessage.ModColumnData[] ZERO_MOD_COLUMNS = new BarrageMessage.ModColumnData[0];

    private static MessageInfo parseProtoMessage(final InputStream stream) throws IOException {
        final MessageInfo mi = new MessageInfo();

        final CodedInputStream decoder = CodedInputStream.newInstance(stream);

        for (int tag = decoder.readTag(); tag != 0; tag = decoder.readTag()) {
            if (tag == DATA_HEADER_TAG) {
                final int size = decoder.readRawVarint32();
                mi.header = Message.getRootAsMessage(ByteBuffer.wrap(decoder.readRawBytes(size)));
                continue;
            } else if (tag == APP_METADATA_TAG) {
                final int size = decoder.readRawVarint32();
                mi.app_metadata = BarragePutMetadata.getRootAsBarragePutMetadata(ByteBuffer.wrap(decoder.readRawBytes(size)));
                continue;
            } else if (tag == FLIGHT_DESCRIPTOR_TAG) {
                final int size = decoder.readRawVarint32();
                final byte[] bytes = decoder.readRawBytes(size);
                mi.descriptor = Flight.FlightDescriptor.parseFrom(bytes);
                continue;
            } else if (tag != BODY_TAG) {
                log.info().append("Skipping tag: ").append(tag).endl();
                decoder.skipField(tag);
                continue;
            }

            if (mi.inputStream != null) {
                // latest input stream wins
                mi.inputStream.close();
                mi.inputStream = null;
            }

            final int size = decoder.readRawVarint32();

            //noinspection UnstableApiUsage
            mi.inputStream = new LittleEndianDataInputStream(new BarrageProtoUtil.ObjectInputStreamAdapter(decoder, size));
        }

        log.info().append("rcvd do put:")
                .append(" descriptor=").append(mi.descriptor == null ? "none" : mi.descriptor.toString())
                .append(" header=").append(mi.header == null ? "none" : MessageHeader.name(mi.header.headerType()))
                .append(" app_md=").append(mi.app_metadata == null ? "none" : "exists")
                .append(" body=").append(mi.inputStream == null ? "none" : Integer.toString(mi.inputStream.available()))
                .endl();

        if (mi.header != null && mi.header.headerType() == MessageHeader.RecordBatch && mi.inputStream == null) {
            //noinspection UnstableApiUsage
            mi.inputStream = new LittleEndianDataInputStream(new ByteArrayInputStream(CollectionUtil.ZERO_LENGTH_BYTE_ARRAY));
        }

        return mi;
    }

    private static final class MessageInfo {
        Message header = null;
        BarragePutMetadata app_metadata = null;
        Flight.FlightDescriptor descriptor = null;
        @SuppressWarnings("UnstableApiUsage")
        LittleEndianDataInputStream inputStream = null;
    }

    /**
     * This is a stateful marshaller; a PUT stream begins with its schema.
     */
    private static class PutMarshaller extends LivenessArtifact {
        private long nextSeq = 0;
        private BarrageSourcedTable resultTable;
        private SessionState.ExportBuilder<Table> resultExport;
        private final SessionState session;
        private final StreamObserver<Flight.PutResult> observer;
        private final TreeMap<Long, MessageInfo> pendingSeq = new TreeMap<>();

        private PutMarshaller(
                final SessionState session,
                final StreamObserver<Flight.PutResult> observer) {
            this.session = session;
            this.observer = observer;
        }

        private ChunkType[] columnChunkTypes;
        private Class<?>[] columnTypes;

        public void parseNext(final MessageInfo mi) {
            GrpcUtil.rpcWrapper(log, observer, () -> {
                synchronized (this) {
                    if (nextSeq == -1) {
                        throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "already received final app_metadata; cannot apply update");
                    }
                    if (mi.app_metadata != null) {
                        final long sequence = mi.app_metadata.sequence();
                        if (sequence != nextSeq) {
                            pendingSeq.put(sequence, mi);
                            return;
                        }
                    }
                }

                if (mi.descriptor != null) {
                    if (resultExport != null) {
                        throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "only one descriptor definition allowed");
                    }
                    if (mi.descriptor.getType() != Flight.FlightDescriptor.DescriptorType.PATH
                            || mi.descriptor.getPathCount() != 2
                            || !mi.descriptor.getPath(0).equals("ticket")) {
                        throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "flight path must be 'ticket/EXPORT_ID'");
                    }

                    final long exportId;
                    try {
                        exportId = Long.parseLong(mi.descriptor.getPath(1));
                    } catch (final NumberFormatException error) {
                        throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "flight path EXPORT_ID non-numeric");
                    }

                    resultExport = session.newExport(exportId);
                }

                if (mi.header == null) {
                    return; // nothing to do!
                }

                if (mi.header.headerType() == MessageHeader.Schema) {
                    parseSchema((Schema) mi.header.header(new Schema()));
                    return;
                }

                if (mi.header.headerType() != MessageHeader.RecordBatch) {
                    throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "only schema/record-batch messages supported");
                }

                final BarrageMessage msg = new BarrageMessage();
                final RecordBatch batch = (RecordBatch) mi.header.header(new RecordBatch());
                final FlatBufferFieldNodeIter fieldNodeIter = new FlatBufferFieldNodeIter(batch);
                final FlatBufferBufferInfoIter bufferInfoIter = new FlatBufferBufferInfoIter(batch);

                msg.step = -1;
                msg.firstSeq = -1;
                msg.lastSeq = -1;

                msg.rowsRemoved = Index.FACTORY.getEmptyIndex();
                msg.shifted = IndexShiftData.EMPTY;

                int numAdded = -1;

                // all bits are always set
                msg.addColumns = new BitSet(resultTable.getColumns().length);
                for (int i = 0; i < resultTable.getColumns().length; ++i) {
                    msg.addColumns.set(i);
                }
                msg.addColumnData = new BarrageMessage.AddColumnData[msg.addColumns.cardinality()];

                for (int ii = msg.addColumns.nextSetBit(0), jj = 0; ii != -1; ii = msg.addColumns.nextSetBit(ii + 1), ++jj) {
                    final BarrageMessage.AddColumnData acd = new BarrageMessage.AddColumnData();
                    msg.addColumnData[jj] = acd;

                    try {
                        acd.data = ChunkInputStreamGenerator.extractChunkFromInputStream(DEFAULT_DESER_OPTIONS, columnChunkTypes[ii], columnTypes[ii], fieldNodeIter, bufferInfoIter, mi.inputStream);
                    } catch (final IOException unexpected) {
                        throw new UncheckedDeephavenException(unexpected);
                    }

                    if (numAdded == -1) {
                        numAdded = acd.data.size();
                    }
                    if (acd.data.size() != numAdded) {
                        throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "inconsistent num records per column: " + numAdded + " != " + acd.data.size());
                    }
                    acd.type = columnTypes[ii];
                }

                msg.rowsAdded = Index.FACTORY.getIndexByRange(resultTable.size(), resultTable.size() + numAdded - 1);
                msg.rowsIncluded = msg.rowsAdded.clone();
                msg.modColumns = new BitSet();
                msg.modColumnData = ZERO_MOD_COLUMNS;

                resultTable.handleBarrageMessage(msg);

                // no app_metadata to report; but ack the processing
                observer.onNext(Flight.PutResult.newBuilder().build());

                final MessageInfo nextMi;
                synchronized (this) {
                    nextMi = pendingSeq.remove(++nextSeq);
                }
                if (nextMi != null) {
                    parseNext(mi); // tail recursive
                }
            });
        }

        @Override
        protected void destroy() {
            // the rpc ticket has been released
            finishPut();
        }

        private synchronized void finishPut() {
            GrpcUtil.rpcWrapper(log, observer, () -> {
                if (nextSeq == -1) {
                    return; // allow onComplete after isFinal app_metadata
                }

                if (resultExport == null) {
                    throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "result flight descriptor never provided");
                }
                if (resultTable == null) {
                    throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "result flight schema never provided");
                }

                resultTable.setRefreshing(false);
                resultExport.submit(() -> resultTable);

                nextSeq = -1;
                if (!pendingSeq.isEmpty()) {
                    throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "pending sequences to apply but received final app_metadata");
                }
                observer.onCompleted();
            });
        }

        private void parseSchema(final Schema header) {
            if (resultTable != null) {
                throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "Schema evolution not supported");
            }
            final TableDefinition definition = BarrageSchemaUtil.schemaToTableDefinition(header);

            final BitSet subCols = new BitSet(definition.getColumns().length);
            for (int i = 0; i < definition.getColumns().length; ++i) {
                subCols.set(i);
            }
            resultTable = BarrageSourcedTable.make(definition, subCols, false);

            columnChunkTypes = resultTable.getWireChunkTypes();
            columnTypes = resultTable.getWireTypes();
        }
    }

    private static class FlatBufferFieldNodeIter implements Iterator<ChunkInputStreamGenerator.FieldNodeInfo> {
        private static final String DEBUG_NAME = "FlatBufferFieldNodeIter";

        private final RecordBatch batch;
        private int fieldNodeOffset = 0;

        public FlatBufferFieldNodeIter(final RecordBatch batch) {
            this.batch = batch;
        }

        @Override
        public boolean hasNext() {
            return fieldNodeOffset < batch.nodesLength();
        }

        @Override
        public ChunkInputStreamGenerator.FieldNodeInfo next() {
            final FieldNode node = batch.nodes(fieldNodeOffset++);
            return new ChunkInputStreamGenerator.FieldNodeInfo(
                    LongSizedDataStructure.intSize(DEBUG_NAME, node.length()),
                    LongSizedDataStructure.intSize(DEBUG_NAME, node.nullCount()));
        }
    }

    private static class FlatBufferBufferInfoIter implements Iterator<ChunkInputStreamGenerator.BufferInfo> {
        private final RecordBatch batch;
        private int bufferOffset = 0;

        public FlatBufferBufferInfoIter(final RecordBatch batch) {
            this.batch = batch;
        }

        @Override
        public boolean hasNext() {
            return bufferOffset < batch.buffersLength();
        }

        @Override
        public ChunkInputStreamGenerator.BufferInfo next() {
            final Buffer node = batch.buffers(bufferOffset++);
            return new ChunkInputStreamGenerator.BufferInfo(node.offset(), node.length());
        }
    }
}
