/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharVectorExpansionKernel and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.extensions.barrage.chunk.vector;

import io.deephaven.chunk.FloatChunk;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.IntChunk;
import io.deephaven.chunk.ObjectChunk;
import io.deephaven.chunk.WritableFloatChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.WritableIntChunk;
import io.deephaven.chunk.WritableObjectChunk;
import io.deephaven.chunk.attributes.Any;
import io.deephaven.chunk.attributes.ChunkPositions;
import io.deephaven.util.datastructures.LongSizedDataStructure;
import io.deephaven.vector.FloatVector;
import io.deephaven.vector.FloatVectorDirect;
import io.deephaven.vector.Vector;

import static io.deephaven.vector.FloatVectorDirect.ZERO_LENGTH_VECTOR;

public class FloatVectorExpansionKernel implements VectorExpansionKernel {
    public final static FloatVectorExpansionKernel INSTANCE = new FloatVectorExpansionKernel();

    @Override
    public <A extends Any> WritableChunk<A> expand(
            final ObjectChunk<Vector<?>, A> source, final WritableIntChunk<ChunkPositions> perElementLengthDest) {
        if (source.size() == 0) {
            perElementLengthDest.setSize(0);
            return WritableFloatChunk.makeWritableChunk(0);
        }

        final ObjectChunk<FloatVector, A> typedSource = source.asObjectChunk();

        long totalSize = 0;
        for (int i = 0; i < typedSource.size(); ++i) {
            final FloatVector row = typedSource.get(i);
            totalSize += row == null ? 0 : row.size();
        }
        final WritableFloatChunk<A> result = WritableFloatChunk.makeWritableChunk(
                LongSizedDataStructure.intSize("ExpansionKernel", totalSize));

        int lenWritten = 0;
        perElementLengthDest.setSize(source.size() + 1);
        for (int i = 0; i < typedSource.size(); ++i) {
            final FloatVector row = typedSource.get(i);
            final int len = row == null ? 0 : row.intSize("FloatVectorExpansionKernel");
            perElementLengthDest.set(i, lenWritten);
            for (int j = 0; j < len; ++j) {
                result.set(lenWritten + j, row.get(j));
            }
            lenWritten += len;
            result.setSize(lenWritten);
        }
        perElementLengthDest.set(typedSource.size(), lenWritten);

        return result;
    }

    @Override
    public <A extends Any> WritableObjectChunk<Vector<?>, A> contract(
            final Chunk<A> source, final IntChunk<ChunkPositions> perElementLengthDest,
            final WritableChunk<A> outChunk, final int outOffset, final int totalRows) {
        if (perElementLengthDest.size() == 0) {
            if (outChunk != null) {
                return outChunk.asWritableObjectChunk();
            }
            return WritableObjectChunk.makeWritableChunk(totalRows);
        }

        final int itemsInBatch = perElementLengthDest.size() - 1;
        final FloatChunk<A> typedSource = source.asFloatChunk();
        final WritableObjectChunk<Vector<?>, A> result;
        if (outChunk != null) {
            result = outChunk.asWritableObjectChunk();
        } else {
            final int numRows = Math.max(itemsInBatch, totalRows);
            result = WritableObjectChunk.makeWritableChunk(numRows);
            result.setSize(numRows);
        }

        int lenRead = 0;
        for (int i = 0; i < itemsInBatch; ++i) {
            final int rowLen = perElementLengthDest.get(i + 1) - perElementLengthDest.get(i);
            if (rowLen == 0) {
                result.set(outOffset + i, ZERO_LENGTH_VECTOR);
            } else {
                final float[] row = new float[rowLen];
                typedSource.copyToArray(lenRead, row, 0, rowLen);
                lenRead += rowLen;
                result.set(outOffset + i, new FloatVectorDirect(row));
            }
        }

        return result;
    }
}
