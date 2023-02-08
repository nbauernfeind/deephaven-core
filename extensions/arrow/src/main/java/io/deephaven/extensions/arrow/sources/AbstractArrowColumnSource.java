package io.deephaven.extensions.arrow.sources;

import io.deephaven.base.verify.Assert;
import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.table.SharedContext;
import io.deephaven.engine.table.impl.AbstractColumnSource;
import io.deephaven.extensions.arrow.ArrowWrapperTools;
import io.deephaven.util.QueryConstants;
import io.deephaven.util.datastructures.LongSizedDataStructure;
import org.apache.arrow.vector.types.pojo.Field;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongConsumer;

/**
 * Base class for arrow column sources
 * 
 * @param <T>
 */
public abstract class AbstractArrowColumnSource<T> extends AbstractColumnSource<T> {

    protected final int highBit;
    protected final int bitCount;
    protected final ArrowWrapperTools.Helper arrowHelper;
    protected final Field field;

    protected AbstractArrowColumnSource(
            @NotNull final Class<T> type,
            final int highBit,
            @NotNull final Field field,
            @NotNull final ArrowWrapperTools.Helper arrowHelper) {
        super(type);
        this.highBit = highBit;
        Assert.eq(Integer.bitCount(highBit), "Integer.bitCount(highBit)", 1, "1");
        this.bitCount = Integer.numberOfTrailingZeros(highBit);
        this.field = field;
        this.arrowHelper = arrowHelper;
    }

    @Override
    public final ArrowWrapperTools.FillContext makeFillContext(final int chunkCapacity,
            final SharedContext sharedContext) {
        return new ArrowWrapperTools.FillContext(arrowHelper, sharedContext);
    }

    protected final void fillChunk(
            @NotNull final ArrowWrapperTools.FillContext context,
            @NotNull final RowSequence rowSequence,
            @NotNull final LongConsumer rowKeyConsumer) {
        long lastKeyInBlockCache = QueryConstants.NULL_LONG;
        RowSequence recordBatchKeys = null;
        try (RowSequence.Iterator okIt = rowSequence.getRowSequenceIterator()) {
            while (okIt.hasMore()) {
                long nextKey = okIt.peekNextKey();
                int blockNumber = getBlockNo(nextKey);
                long lastKeyInBlock = nextKey | (highBit - 1);
                if (recordBatchKeys == null || lastKeyInBlock != lastKeyInBlockCache) {
                    recordBatchKeys = okIt.getNextRowSequenceThrough(lastKeyInBlock);
                    lastKeyInBlockCache = lastKeyInBlock;
                }
                context.ensureLoadingBlock(blockNumber);
                recordBatchKeys.forAllRowKeys(rowKeyConsumer);
            }
        }
    }

    protected final int getBlockNo(long rowKey) {
        return LongSizedDataStructure.intSize("ArrowIntColumnSource#getInt", (rowKey >> bitCount));
    }

    protected final int getPositionInBlock(long rowKey) {
        return LongSizedDataStructure.intSize("ArrowIntColumnSource#getInt", rowKey & (highBit - 1));
    }
}
