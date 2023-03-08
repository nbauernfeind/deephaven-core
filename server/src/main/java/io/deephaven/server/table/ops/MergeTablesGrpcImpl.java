/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.server.table.ops;

import com.google.rpc.Code;
import io.deephaven.auth.codegen.impl.TableServiceContextualAuthWiring;
import io.deephaven.base.verify.Assert;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.updategraph.UpdateContext;
import io.deephaven.engine.updategraph.UpdateGraphProcessor;
import io.deephaven.engine.util.TableTools;
import io.deephaven.extensions.barrage.util.GrpcUtil;
import io.deephaven.proto.backplane.grpc.BatchTableRequest;
import io.deephaven.proto.backplane.grpc.MergeTablesRequest;
import io.deephaven.server.session.SessionState;
import io.deephaven.util.SafeCloseable;
import io.grpc.StatusRuntimeException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MergeTablesGrpcImpl extends GrpcTableOperation<MergeTablesRequest> {

    private final UpdateGraphProcessor updateGraphProcessor;

    @Inject
    public MergeTablesGrpcImpl(
            final TableServiceContextualAuthWiring authWiring,
            final UpdateGraphProcessor updateGraphProcessor) {
        super(authWiring::checkPermissionMergeTables, BatchTableRequest.Operation::getMerge,
                MergeTablesRequest::getResultId, MergeTablesRequest::getSourceIdsList);
        this.updateGraphProcessor = updateGraphProcessor;
    }

    @Override
    public void validateRequest(final MergeTablesRequest request) throws StatusRuntimeException {
        if (request.getSourceIdsList().isEmpty()) {
            throw GrpcUtil.statusRuntimeException(Code.INVALID_ARGUMENT, "Cannot merge zero source tables.");
        }
    }

    @Override
    public Table create(final MergeTablesRequest request,
            final List<SessionState.ExportObject<Table>> sourceTables) {
        Assert.gt(sourceTables.size(), "sourceTables.size()", 0);

        final String keyColumn = request.getKeyColumn();
        final List<Table> tables = sourceTables.stream()
                .map(SessionState.ExportObject::get)
                .collect(Collectors.toList());

        if (tables.stream().noneMatch(Table::isRefreshing)) {
            return keyColumn.isEmpty() ? TableTools.merge(tables) : TableTools.mergeSorted(keyColumn, tables);
        } else {
            final UpdateContext updateContext = getUpdateContext(tables);
            return updateContext.apply(() -> {
                Table result;
                try (final SafeCloseable ignored = updateContext.getSharedLock().lockCloseable()) {
                    result = TableTools.merge(tables);
                }
                if (!keyColumn.isEmpty()) {
                    result = result.sort(keyColumn);
                }
                return result;
            });
        }
    }
}
