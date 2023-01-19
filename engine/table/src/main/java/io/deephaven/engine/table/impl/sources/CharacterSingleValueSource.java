/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.engine.table.impl.sources;

import io.deephaven.chunk.WritableCharChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.impl.MutableColumnSourceGetDefaults;
import io.deephaven.engine.updategraph.LogicalClock;
import io.deephaven.engine.rowset.chunkattributes.RowKeys;
import io.deephaven.chunk.CharChunk;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.LongChunk;
import io.deephaven.engine.rowset.RowSequence;
import org.jetbrains.annotations.NotNull;

import static io.deephaven.util.QueryConstants.NULL_CHAR;
import static io.deephaven.util.type.TypeUtils.unbox;

/**
 * Single value source for Character.
 * <p>
 * The C-haracterSingleValueSource is replicated to all other types with
 * io.deephaven.engine.table.impl.sources.Replicate.
 *
 * (C-haracter is deliberately spelled that way in order to prevent Replicate from altering this very comment).
 */
public class CharacterSingleValueSource extends SingleValueColumnSource<Character> implements MutableColumnSourceGetDefaults.ForChar {

    private char current;
    private transient char prev;

    // region Constructor
    public CharacterSingleValueSource() {
        super(char.class);
        current = NULL_CHAR;
        prev = NULL_CHAR;
    }
    // endregion Constructor

    @Override
    public final void set(Character value) {
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
    public final void set(char value) {
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
        set(NULL_CHAR);
    }

    @Override
    public final void set(long key, char value) {
        set(value);
    }

    @Override
    public final void setNull(long key) {
        // region null set
        set(NULL_CHAR);
        // endregion null set
    }

    @Override
    public final char getChar(long rowKey) {
        if (rowKey == RowSequence.NULL_ROW_KEY) {
            return NULL_CHAR;
        }
        return current;
    }

    @Override
    public final char getPrevChar(long rowKey) {
        if (rowKey == RowSequence.NULL_ROW_KEY) {
            return NULL_CHAR;
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
        final CharChunk<? extends Values> chunk = src.asCharChunk();
        set(chunk.get(0));
    }

    @Override
    public void fillFromChunkUnordered(@NotNull FillFromContext context, @NotNull Chunk<? extends Values> src, @NotNull LongChunk<RowKeys> keys) {
        if (keys.size() == 0) {
            return;
        }
        // We can only hold one value anyway, so arbitrarily take the first value in the chunk and ignore the rest.
        final CharChunk<? extends Values> chunk = src.asCharChunk();
        set(chunk.get(0));
    }

    @Override
    public void fillChunk(@NotNull FillContext context, @NotNull WritableChunk<? super Values> destination,
            @NotNull RowSequence rowSequence) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        destination.setSize(rowSequence.intSize());
        destination.asWritableCharChunk().fillWithValue(0, rowSequence.intSize(), getChar(0));
    }

    @Override
    public void fillPrevChunk(@NotNull FillContext context,
            @NotNull WritableChunk<? super Values> destination, @NotNull RowSequence rowSequence) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        destination.setSize(rowSequence.intSize());
        destination.asWritableCharChunk().fillWithValue(0, rowSequence.intSize(), getPrevChar(0));
    }

    @Override
    public void fillChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
            @NotNull LongChunk<? extends RowKeys> keys) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        char value = getChar(0);
        final WritableCharChunk<? super Values> destChunk = dest.asWritableCharChunk();
        for (int ii = 0; ii < keys.size(); ++ii) {
            destChunk.set(ii, keys.get(ii) == RowSequence.NULL_ROW_KEY ? NULL_CHAR : value);
        }
        destChunk.setSize(keys.size());
    }

    @Override
    public void fillPrevChunkUnordered(@NotNull FillContext context, @NotNull WritableChunk<? super Values> dest,
            @NotNull LongChunk<? extends RowKeys> keys) {
        // We can only hold one value, fill the chunk with the value obtained from an arbitrarily valid rowKey
        char value = getPrevChar(0);
        final WritableCharChunk<? super Values> destChunk = dest.asWritableCharChunk();
        for (int ii = 0; ii < keys.size(); ++ii) {
            destChunk.set(ii, keys.get(ii) == RowSequence.NULL_ROW_KEY ? NULL_CHAR : value);
        }
        destChunk.setSize(keys.size());
    }

    @Override
    public boolean providesFillUnordered() {
        return true;
    }
}
