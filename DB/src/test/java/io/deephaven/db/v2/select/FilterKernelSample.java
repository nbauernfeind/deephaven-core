package io.deephaven.db.v2.select;
// QueryLibrary internal version number: DEFAULT
import java.lang.*;

import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.select.Param;

import static io.deephaven.db.v2.select.ConditionFilter.FilterKernel;

import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.DoubleChunk;
import io.deephaven.db.v2.sources.chunk.LongChunk;
import io.deephaven.db.v2.sources.chunk.ShortChunk;
import io.deephaven.db.v2.utils.Index;

import static io.deephaven.db.tables.lang.DBLanguageFunctionUtil.*;
import static io.deephaven.db.v2.sources.chunk.Attributes.*;

public class FilterKernelSample implements io.deephaven.db.v2.select.ConditionFilter.FilterKernel<FilterKernel.Context>{


    private final int p1;
    private final float p2;
    private final java.lang.String p3;

    public FilterKernelSample(Table table, Index fullSet, Param... params) {
        this.p1 = (java.lang.Integer) params[0].getValue();
        this.p2 = (java.lang.Float) params[1].getValue();
        this.p3 = (java.lang.String) params[2].getValue();
    }
    @Override
    public Context getContext(int maxChunkSize) {
        return new Context(maxChunkSize);
    }

    @Override
    public LongChunk<OrderedKeyIndices> filter(Context context, LongChunk<OrderedKeyIndices> indices, Chunk... inputChunks) {
        final ShortChunk __columnChunk0 = inputChunks[0].asShortChunk();
        final DoubleChunk __columnChunk1 = inputChunks[1].asDoubleChunk();
        final int size = indices.size();
        context.resultChunk.setSize(0);
        for (int __my_i__ = 0; __my_i__ < size; __my_i__++) {
            final short v1 =  (short)__columnChunk0.get(__my_i__);
            final double v2 =  (double)__columnChunk1.get(__my_i__);
            if ("foo".equals((PLUS(PLUS(PLUS(p1, p2), v1), v2))+p3)) {
                context.resultChunk.add(indices.get(__my_i__));
            }
        }
        return context.resultChunk;
    }
}
