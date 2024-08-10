//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl.select.analyzers;

import io.deephaven.base.log.LogOutput;
import io.deephaven.base.verify.Assert;
import io.deephaven.configuration.Configuration;
import io.deephaven.engine.liveness.LivenessNode;
import io.deephaven.engine.liveness.LivenessReferent;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.ModifiedColumnSet;
import io.deephaven.engine.table.TableUpdate;
import io.deephaven.engine.table.impl.select.SelectColumn;
import io.deephaven.engine.table.impl.util.JobScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final public class ViewColumnLayer extends SelectOrViewColumnLayer {

    private static final boolean ALLOW_LIVENESS_REFERENT_RESULTS = Configuration.getInstance()
            .getBooleanForClassWithDefault(ViewColumnLayer.class, "allowLivenessReferentResults", false);

    ViewColumnLayer(SelectAndViewAnalyzer inner, String name, SelectColumn sc, ColumnSource<?> cs, String[] deps,
            ModifiedColumnSet mcsBuilder) {
        super(inner, name, sc, checkResultType(cs), null, deps, mcsBuilder);
    }

    @Override
    public CompletionHandler createUpdateHandler(
            final TableUpdate upstream,
            final RowSet toClear,
            final SelectAndViewAnalyzer.UpdateHelper helper,
            final JobScheduler jobScheduler,
            @Nullable final LivenessNode liveResultOwner,
            final CompletionHandler completionHandler) {
        // There should be nothing to do here.
        Assert.eqNull(completionHandler, "completionHandler");

        return null;
    }

    @Override
    public LogOutput append(LogOutput logOutput) {
        return logOutput.append("{ViewColumnLayer: ").append(selectColumn.toString()).append(", layerIndex=")
                .append(getLayerIndex()).append("}");
    }

    private static ColumnSource<?> checkResultType(@NotNull final ColumnSource<?> cs) {
        final Class<?> resultType = cs.getType();
        if (!ALLOW_LIVENESS_REFERENT_RESULTS && LivenessReferent.class.isAssignableFrom(resultType)) {
            throw new UnsupportedOperationException(String.format(
                    "Cannot use view or updateView to produce results of type %s; use select or update instead",
                    resultType));
        }
        return cs;
    }

    @Override
    public boolean allowCrossColumnParallelization() {
        // this should not actually matter; but false seems like the safe answer for any formula
        return false;
    }
}
