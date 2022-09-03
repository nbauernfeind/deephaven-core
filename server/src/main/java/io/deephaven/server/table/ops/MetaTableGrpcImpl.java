package io.deephaven.server.table.ops;

import io.deephaven.base.verify.Assert;
import io.deephaven.engine.table.Table;
import io.deephaven.proto.backplane.grpc.BatchTableRequest;
import io.deephaven.proto.backplane.grpc.MetaTableRequest;
import io.deephaven.server.session.SessionState;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MetaTableGrpcImpl extends GrpcTableOperation<MetaTableRequest> {

    @Inject
    public MetaTableGrpcImpl() {
        super(BatchTableRequest.Operation::getMetaTable, MetaTableRequest::getResultId, MetaTableRequest::getSourceId);
    }

    @Override
    public Table create(MetaTableRequest request, List<SessionState.ExportObject<Table>> sourceTables) {
        Assert.eq(sourceTables.size(), "sourceTables.size()", 1);
        return sourceTables.get(0).get().getMeta();
    }
}
