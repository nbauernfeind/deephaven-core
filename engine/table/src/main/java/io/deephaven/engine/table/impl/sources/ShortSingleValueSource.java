/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharacterSingleValueSource and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.engine.table.impl.sources;

import io.deephaven.chunk.WritableShortChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.impl.MutableColumnSourceGetDefaults;
import io.deephaven.engine.updategraph.LogicalClock;
import io.deephaven.engine.rowset.chunkattributes.RowKeys;
import io.deephaven.chunk.ShortChunk;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.LongChunk;
import io.deephaven.engine.rowset.RowSequence;
import org.jetbrains.annotations.NotNull;

import static io.deephaven.util.QueryConstants.NULL_SHORT;
import static io.deephaven.util.type.TypeUtils.unbox;

/**
 * Single value source for Short.
 * <p>
 * The C-haracterSingleValueSource is replicated to all other types with
 * io.deephaven.engine.table.impl.sources.Replicate.
 *
 * (C-haracter is deliberately spelled that way in order to prevent Replicate from altering this very comment).
 */
public class ShortSingleValueSource extends SingleValueColumnSource<Short> implements MutableColumnSourceGetDefaults.ForShort {

    private short current;
    private transient short prev;

    // region Constructor
    public ShortSingleValueSource() {
        super(short.class);
        current = NULL_SHORT;
        prev = NULL_SHORT;
    }
    // endregion Constructor

    @Override
    public final void set(Short value) {
        if (isTrackingPrevValues) {
            final long currentStep = LogicalClock.DEFAULT.currentStep();
            if (changeTime < currentStep) {
                prev = current;
                changeTime = currentStep;
            }
        }
        current = unbox(value);
    }

    // region UnboxedSetter
    @Override
    public final void set(short value) {
        if (isTrackingPrevValues) {
            final long currentStep = LogicalClock.DEFAULT.currentStep();
            if (changeTime < currentStep) {
                prev = current;
                changeTime = currentStep;
            }
        }
        current = value;
    }
    // endregion UnboxedSetter

    @Override
    public final void setNull() {
        set(NULL_SHORT);
    }

    @Override
    public final void set(long key, short value) {
        set(value);
    }

    @Override
    public final void setNull(long key) {
        // region null set
        set(NULL_SHORT);
        // endregion null set
    }

    @Override
    public final short getShort(long rowKey) {
        if (rowKey == RowSequence.NULL_ROW_KEY) {
            return NULL_SHORT;
        }
        return current;
    }

    @Override
    public final short getPrevShort(long rowKey) {
        if (rowKey == RowSequence.NULL_ROW_KEY) {
            return NULL_SHORT;
        }
        if (!isTrackingPrevValues || changeTime < LogicalClock.DEFAULT.currentStep()) {
            return current;
        }
        return prev;
    }

    @Override
    public final void fillFromChunk(@NotNull FillFromContext context, @NotNull Chunk<? extends Values> src, @NotNull RowSequence rowSequence) {
        if (rowSequence.size() == 0) {
            return;
        }
        // We can only hold one value anyway, so arbitrarily take the first value in the chunk and ignore the rest.
        final ShortChunk<? extends Values> chunk = src.asShortChunk();
        set(chunk.get(0));
    }

    @Override
    public void fillFromChunkUnordered(@NotNull FillFromContext context, @NotNull Chunk<? extends Values> src, @NotNull LongChunk<RowKeys> keys) {
        if (keys.size() == 0) {
            return;
        }
        // We can only hold one value anyway, so arbitrarily take the first value in the chunk and ignore the rest.
        final ShortChunk<? extends Values> chunk = src.asShortChunk();
        set(chunk.get(0));
    }

    @Override
    public void fillChunk(@NotNull FillContext context, @NotNull WritableChunk<? super Values> destination,
            @NotNull RowSequence rowSequence) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        destination.setSize(rowSequence.intSize());
        destination.asWritableShortChunk().fillWithValue(0, rowSequence.intSize(), getShort(0));
    }

    @Override
    public void fillPrevChunk(@NotNull FillContext context,
            @NotNull WritableChunk<? super Values> destination, @NotNull RowSequence rowSequence) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        destination.setSize(rowSequence.intSize());
        destination.asWritableShortChunk().fillWithValue(0, rowSequence.intSize(), getPrevShort(0));
    }

    @Override
    public void fillChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
            @NotNull LongChunk<? extends RowKeys> keys) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        short value = getShort(0);
        final WritableShortChunk<? super Values> destChunk = dest.asWritableShortChunk();
        for (int ii = 0; ii < keys.size(); ++ii) {
            destChunk.set(ii, keys.get(ii) == RowSequence.NULL_ROW_KEY ? NULL_SHORT : value);
        }
        destChunk.setSize(keys.size());
    }

    @Override
    public void fillPrevChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
            @NotNull LongChunk<? extends RowKeys> keys) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        short value = getPrevShort(0);
        final WritableShortChunk<? super Values> destChunk = dest.asWritableShortChunk();
        for (int ii = 0; ii < keys.size(); ++ii) {
            destChunk.set(ii, keys.get(ii) == RowSequence.NULL_ROW_KEY ? NULL_SHORT : value);
        }
        destChunk.setSize(keys.size());
    }

    @Override
    public boolean providesFillUnordered() {
        return true;
    }
}
