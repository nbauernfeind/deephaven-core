package io.deephaven.db.v2.select;
// QueryLibrary internal version number: DEFAULT
import java.lang.*;

import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.IntChunk;
import io.deephaven.db.v2.sources.chunk.LongChunk;
import io.deephaven.db.v2.sources.chunk.WritableChunk;
import io.deephaven.db.v2.sources.chunk.WritableLongChunk;

import static io.deephaven.db.tables.lang.DBLanguageFunctionUtil.*;

public class FormulaKernelSample implements io.deephaven.db.v2.select.formula.FormulaKernel {
    public static final io.deephaven.db.v2.select.formula.FormulaKernelFactory __FORMULA_KERNEL_FACTORY = FormulaKernelSample::new;

    private final io.deephaven.db.tables.dbarrays.DbLongArray II_;
    private final java.lang.Integer q;

    public FormulaKernelSample(io.deephaven.db.tables.dbarrays.DbArrayBase[] __dbArrays,
            io.deephaven.db.tables.select.Param[] __params) {
        II_ = (io.deephaven.db.tables.dbarrays.DbLongArray)__dbArrays[0];
        q = (java.lang.Integer)__params[0].getValue();
    }

    @Override
    public FormulaFillContext makeFillContext(final int __chunkCapacity) {
        return new FormulaFillContext(__chunkCapacity);
    }

    @Override
    public void applyFormulaChunk(io.deephaven.db.v2.select.Formula.FillContext __context,
            final WritableChunk<? super Attributes.Values> __destination,
            Chunk<? extends Attributes.Values>[] __sources) {
        final WritableLongChunk<? super Attributes.Values> __typedDestination = __destination.asWritableLongChunk();
        final LongChunk<? extends Attributes.Values> __chunk__col__II = __sources[0].asLongChunk();
        final LongChunk<? extends Attributes.Values> __chunk__col__ii = __sources[1].asLongChunk();
        final IntChunk<? extends Attributes.Values> __chunk__col__I = __sources[2].asIntChunk();
        final IntChunk<? extends Attributes.Values> __chunk__col__i = __sources[3].asIntChunk();
        final int __size = __typedDestination.size();
        for (int __chunkPos = 0; __chunkPos < __size; ++__chunkPos) {
            __typedDestination.set(__chunkPos, applyFormulaPerItem(__chunk__col__II.get(__chunkPos), __chunk__col__ii.get(__chunkPos), __chunk__col__I.get(__chunkPos), __chunk__col__i.get(__chunkPos)));
        }
    }

    private long applyFormulaPerItem(long II, long ii, int I, int i) {
        try {
            return PLUS(PLUS(MULTIPLY(I, II), MULTIPLY(q.intValue(), ii)), II_.get(MINUS(i, 1)));
        } catch (java.lang.Exception __e) {
            throw new io.deephaven.db.v2.select.FormulaEvaluationException("In formula: " + "plus(plus(times(I, II), times(q.intValue(), ii)), II_.get(minus(i, 1)))", __e);
        }
    }

    private class FormulaFillContext implements io.deephaven.db.v2.select.Formula.FillContext {
        FormulaFillContext(int __chunkCapacity) {
        }
    }

}
