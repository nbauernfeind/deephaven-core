package io.deephaven.grpc_api.app_mode;

import io.deephaven.db.tables.Table;
import io.deephaven.grpc_api.barrage.util.BarrageSchemaUtil;
import io.deephaven.grpc_api.console.GlobalSessionProvider;
import io.deephaven.grpc_api.console.ScopeTicketResolver;
import io.deephaven.grpc_api.session.SessionService;
import io.deephaven.grpc_api.session.SessionState;
import io.deephaven.grpc_api.util.GrpcUtil;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import io.deephaven.proto.backplane.grpc.FieldInfo;
import io.deephaven.proto.backplane.grpc.FieldServiceGrpc;
import io.deephaven.proto.backplane.grpc.FieldsChangeUpdate;
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
    private final SessionService sessionService;

    @Inject
    public FieldServiceGrpcImpl(final AppMode mode,
                                final GlobalSessionProvider globalSessionProvider,
                                final SessionService sessionService) {
        this.mode = mode;
        this.sessionProvider = globalSessionProvider;
        this.sessionService = sessionService;
    }

    @Override
    public void listFields(ListFieldsRequest request, StreamObserver<FieldsChangeUpdate> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getOptionalSession();
            final FieldsChangeUpdate.Builder responseBuilder = FieldsChangeUpdate.newBuilder();

            if (mode.hasVisibilityToAppExports()) {
                log.warn().append("Skipping request to list application mode exports.").endl();
            }
            if (mode.hasVisibilityToConsoleExports() && session != null) {
                sessionProvider.getGlobalSession().getVariables().forEach((var, obj) ->{
                    final FieldInfo.FieldType fieldType = fetchFieldType(obj);
                    if (fieldType != null) {
                        responseBuilder.addFields(FieldInfo.newBuilder()
                                .setTicket(ScopeTicketResolver.ticketForName(var))
                                .setFieldName(var)
                                .setFieldType(fieldType)
                                .setFieldDescription("scope variable")
                                .build());
                    }
                });
            }
            responseObserver.onNext(responseBuilder.build());
            // TODO: note we are not closing, because we want to eventually send updates to the client
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
