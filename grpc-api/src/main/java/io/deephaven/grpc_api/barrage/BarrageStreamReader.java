/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.grpc_api.barrage;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.protobuf.CodedInputStream;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TLongArrayList;
import io.deephaven.barrage.flatbuf.BarrageMessageType;
import io.deephaven.barrage.flatbuf.BarrageMessageWrapper;
import io.deephaven.barrage.flatbuf.BarrageModColumnMetadata;
import io.deephaven.barrage.flatbuf.BarrageUpdateMetadata;
import io.deephaven.db.util.LongSizedDataStructure;
import io.deephaven.db.v2.sources.chunk.ChunkType;
import io.deephaven.db.v2.utils.BarrageMessage;
import io.deephaven.db.v2.utils.ExternalizableIndexUtils;
import io.deephaven.db.v2.utils.Index;
import io.deephaven.db.v2.utils.IndexShiftData;
import io.deephaven.grpc_api.arrow.FlightServiceGrpcImpl;
import io.deephaven.grpc_api_client.barrage.chunk.ChunkInputStreamGenerator;
import io.deephaven.grpc_api_client.util.BarrageProtoUtil;
import io.deephaven.grpc_api_client.util.FlatBufferIteratorAdapter;
import io.deephaven.grpc_api_client.util.GrpcMarshallingException;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import org.apache.arrow.flatbuf.FieldNode;
import org.apache.arrow.flatbuf.Message;
import org.apache.arrow.flatbuf.MessageHeader;
import org.apache.arrow.flatbuf.RecordBatch;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Iterator;

public class BarrageStreamReader implements BarrageMessageConsumer.StreamReader<ChunkInputStreamGenerator.Options> {

    private static final Logger log = LoggerFactory.getLogger(BarrageStreamReader.class);

    @Override
    public BarrageMessage safelyParseFrom(final ChunkInputStreamGenerator.Options options,
                                          final ChunkType[] columnChunkTypes,
                                          final Class<?>[] columnTypes,
                                          final Class<?>[] componentTypes,
                                          final InputStream stream) {
        Message header = null;
        BarrageUpdateMetadata metadata = null;
        final BarrageMessage msg = new BarrageMessage();

        try {
            boolean bodyParsed = false;
            final CodedInputStream decoder = CodedInputStream.newInstance(stream);

            for (int tag = decoder.readTag(); tag != 0; tag = decoder.readTag()) {
                if (tag == FlightServiceGrpcImpl.DATA_HEADER_TAG) {
                    final int size = decoder.readRawVarint32();
                    header = Message.getRootAsMessage(ByteBuffer.wrap(decoder.readRawBytes(size)));
                    continue;
                } else if (tag == FlightServiceGrpcImpl.APP_METADATA_TAG) {
                    final int size = decoder.readRawVarint32();
                    final BarrageMessageWrapper wrapper = BarrageMessageWrapper.getRootAsBarrageMessageWrapper(ByteBuffer.wrap(decoder.readRawBytes(size)));
                    if (wrapper.magic() == BarrageStreamGenerator.FLATBUFFER_MAGIC && wrapper.msgType() == BarrageMessageType.BarrageUpdateMetadata) {
                        metadata = BarrageUpdateMetadata.getRootAsBarrageUpdateMetadata(wrapper.msgPayloadAsByteBuffer());
                    }
                    continue;
                } else if (tag != FlightServiceGrpcImpl.BODY_TAG) {
                    decoder.skipField(tag);
                    continue;
                }

                if (bodyParsed) {
                    // although not an error for protobuf, we consider it one because:
                    // 1) we control all writers
                    // 2) it's plain ol' inefficient!
                    throw new IllegalStateException("Unexpected duplicate body tag");
                }

                if (header == null) {
                    throw new IllegalStateException("Missing metadata header; cannot decode body");
                }

                if (header.headerType() != org.apache.arrow.flatbuf.MessageHeader.RecordBatch) {
                    throw new IllegalStateException("Only know how to decode Schema/BarrageRecordBatch messages");
                }

                final RecordBatch batch = (RecordBatch) header.header(new RecordBatch());
                msg.isSnapshot = metadata.isSnapshot();

                bodyParsed = true;
                final int size = decoder.readRawVarint32();
                //noinspection UnstableApiUsage
                try (final LittleEndianDataInputStream ois = new LittleEndianDataInputStream(new BarrageProtoUtil.ObjectInputStreamAdapter(decoder, size))) {
                    final MutableInt bufferOffset = new MutableInt();
                    final Iterator<ChunkInputStreamGenerator.FieldNodeInfo> fieldNodeIter =
                            new FlatBufferIteratorAdapter<>(batch.nodesLength(), i -> new ChunkInputStreamGenerator.FieldNodeInfo(batch.nodes(i)));

                    final TLongArrayList bufferInfo = new TLongArrayList(batch.buffersLength());
                    for (int bi = 0; bi < batch.buffersLength(); ++bi) {
                        int offset = LongSizedDataStructure.intSize("BufferInfo", batch.buffers(bi).offset());
                        int length = LongSizedDataStructure.intSize("BufferInfo", batch.buffers(bi).length());
                        if (bi < batch.buffersLength() - 1) {
                            final int nextOffset = LongSizedDataStructure.intSize("BufferInfo", batch.buffers(bi + 1).offset());
                            // our parsers handle overhanging buffers
                            length += Math.max(0, nextOffset - offset - length);
                        }
                        bufferOffset.setValue(offset + length);
                        bufferInfo.add(length);
                    }
                    final TLongIterator bufferInfoIter = bufferInfo.iterator();

                    if (msg.isSnapshot) {
                        final ByteBuffer effectiveViewport = metadata.effectiveViewportAsByteBuffer();
                        if (effectiveViewport != null) {
                            msg.snapshotIndex = extractIndex(effectiveViewport);
                        }
                        msg.snapshotColumns = extractBitSet(metadata.effectiveColumnSetAsByteBuffer());
                    }

                    msg.firstSeq = metadata.firstSeq();
                    msg.lastSeq = metadata.lastSeq();
                    msg.rowsAdded = extractIndex(metadata.addedRowsAsByteBuffer());
                    msg.rowsRemoved = extractIndex(metadata.removedRowsAsByteBuffer());
                    msg.shifted = extractIndexShiftData(metadata.shiftDataAsByteBuffer());

                    final ByteBuffer rowsIncluded = metadata.addedRowsIncludedAsByteBuffer();
                    msg.rowsIncluded = rowsIncluded != null ? extractIndex(rowsIncluded) : msg.rowsAdded.clone();

                    int numRowsAdded = msg.rowsIncluded.intSize();
                    if (numRowsAdded == 0 && msg.isSnapshot) {
                        // We only send the full table index in the initial snapshot. After that it is empty.
                        numRowsAdded = -1;
                    }

                    msg.addColumnData = new BarrageMessage.AddColumnData[columnTypes.length];
                    for (int ci = 0; ci < msg.addColumnData.length; ++ci) {
                        final BarrageMessage.AddColumnData acd = new BarrageMessage.AddColumnData();
                        msg.addColumnData[ci] = acd;

                        acd.data = ChunkInputStreamGenerator.extractChunkFromInputStream(options, columnChunkTypes[ci], columnTypes[ci], fieldNodeIter, bufferInfoIter, ois);
                        if (numRowsAdded == -1 && acd.data.size() != 0) {
                            numRowsAdded = acd.data.size();
                        } else if (acd.data.size() != 0 && acd.data.size() != numRowsAdded) {
                            throw new IllegalStateException("Add column data does not have the expected number of rows.");
                        }
                        acd.type = columnTypes[ci];
                        acd.componentType = componentTypes[ci];
                    }

                    msg.modColumnData = new BarrageMessage.ModColumnData[columnTypes.length];
                    for (int i = 0; i < msg.modColumnData.length; ++i) {
                        final BarrageMessage.ModColumnData mcd = new BarrageMessage.ModColumnData();
                        msg.modColumnData[i] = mcd;

                        final BarrageModColumnMetadata md = metadata.nodes(i);
                        final ByteBuffer bb = md.modifiedRowsAsByteBuffer();
                        mcd.rowsModified = extractIndex(bb);

                        final int numModded = mcd.rowsModified.intSize();
                        mcd.data = ChunkInputStreamGenerator.extractChunkFromInputStream(options, columnChunkTypes[i], columnTypes[i], fieldNodeIter, bufferInfoIter, ois);
                        if (mcd.data.size() != numModded) {
                            throw new IllegalStateException("Mod column data does not have the expected number of rows.");
                        }
                        mcd.type = columnTypes[i];
                        mcd.componentType = componentTypes[i];
                    }
                }
            }

            if (header != null && header.headerType() == MessageHeader.Schema) {
                // there is no body and our clients do not want to see schema messages
                return null;
            }

            if (!bodyParsed) {
                throw new IllegalStateException("Missing body tag");
            }

            return msg;
        } catch (final Exception e) {
            log.error().append("Unable to parse a received BarrageMessage: ").append(e).endl();
            throw new GrpcMarshallingException("Unable to parse BarrageMessage object", e);
        }
    }

    private static Index extractIndex(final ByteBuffer bb) throws IOException {
        if (bb == null) {
            return Index.FACTORY.getEmptyIndex();
        }
        //noinspection UnstableApiUsage
        try (final LittleEndianDataInputStream is = new LittleEndianDataInputStream(new ByteBufferBackedInputStream(bb))) {
            return ExternalizableIndexUtils.readExternalCompressedDelta(is);
        }
    }

    private static BitSet extractBitSet(final ByteBuffer bb) {
        return BitSet.valueOf(bb);
    }

    private static IndexShiftData extractIndexShiftData(final ByteBuffer bb) throws IOException {
        final IndexShiftData.Builder builder = new IndexShiftData.Builder();

        final Index sIndex, eIndex, dIndex;
        //noinspection UnstableApiUsage
        try (final LittleEndianDataInputStream is = new LittleEndianDataInputStream(new ByteBufferBackedInputStream(bb))) {
            sIndex = ExternalizableIndexUtils.readExternalCompressedDelta(is);
            eIndex = ExternalizableIndexUtils.readExternalCompressedDelta(is);
            dIndex = ExternalizableIndexUtils.readExternalCompressedDelta(is);
        }

        try (final Index.Iterator sit = sIndex.iterator();
             final Index.Iterator eit = eIndex.iterator();
             final Index.Iterator dit = dIndex.iterator()) {
            while (sit.hasNext()) {
                if (!eit.hasNext() || !dit.hasNext()) {
                    throw new IllegalStateException("IndexShiftData is inconsistent");
                }
                final long next = sit.nextLong();
                builder.shiftRange(next, eit.nextLong(), dit.nextLong() - next);
            }
        }

        return builder.build();
    }
}
