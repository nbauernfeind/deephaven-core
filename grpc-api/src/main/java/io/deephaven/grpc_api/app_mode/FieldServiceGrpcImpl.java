package io.deephaven.grpc_api.app_mode;

import io.deephaven.db.tables.Table;
import io.deephaven.grpc_api.barrage.util.BarrageSchemaUtil;
import io.deephaven.grpc_api.console.GlobalSessionProvider;
import io.deephaven.grpc_api.console.ScopeTicketResolver;
import io.deephaven.grpc_api.util.GrpcUtil;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import io.deephaven.proto.backplane.grpc.FieldInfo;
import io.deephaven.proto.backplane.grpc.FieldServiceGrpc;
import io.deephaven.proto.backplane.grpc.ListFieldsRequest;
import io.deephaven.proto.backplane.grpc.TableInfo;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FieldServiceGrpcImpl extends FieldServiceGrpc.FieldServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(FieldServiceGrpcImpl.class);

    private final AppMode mode;
    private final GlobalSessionProvider sessionProvider;

    @Inject
    public FieldServiceGrpcImpl(final AppMode mode,
                                final GlobalSessionProvider globalSessionProvider) {
        this.mode = mode;
        this.sessionProvider = globalSessionProvider;
    }

    @Override
    public void listFields(ListFieldsRequest request, StreamObserver<FieldInfo> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            if (mode.hasVisibilityToAppExports()) {
                // TODO NOCOMMIT(NATE): Show App Exports
                log.warn().append("Skipping request to list application mode exports.").endl();
            }
            if (mode.hasVisibilityToConsoleExports()) {
                sessionProvider.getGlobalSession().getVariables().forEach((var, obj) ->{
                    final FieldInfo.FieldType fieldType = fetchFieldType(obj);
                    if (fieldType != null) {
                        responseObserver.onNext(FieldInfo.newBuilder()
                                .setTicket(ScopeTicketResolver.ticketForName(var))
                                .setFieldName(var)
                                .setFieldType(fieldType)
                                .setFieldDescription("scope variable")
                                .build());
                    }
                });
            }
            responseObserver.onCompleted();
        });
    }

    private static FieldInfo.FieldType fetchFieldType(final Object obj) {
        if (obj instanceof Table) {
            final Table table = (Table) obj;
            return FieldInfo.FieldType.newBuilder().setTable(TableInfo.newBuilder()
                    .setSchemaHeader(BarrageSchemaUtil.schemaBytesFromTable(table))
                    .setIsStatic(!table.isLive())
                    .setSize(table.size())
                    .build()).build();
        }
        return null;
    }
}
