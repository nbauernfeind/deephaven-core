package io.deephaven.grpc_api.app_mode;

import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.db.appmode.CustomField;
import io.deephaven.db.appmode.Field;
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
import io.deephaven.proto.backplane.grpc.ApplicationServiceGrpc;
import io.deephaven.proto.backplane.grpc.FieldsChangeUpdate;
import io.deephaven.proto.backplane.grpc.ListFieldsRequest;
import io.deephaven.proto.backplane.grpc.TableInfo;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApplicationServiceGrpcImpl extends ApplicationServiceGrpc.ApplicationServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceGrpcImpl.class);

    private final AppMode mode;
    private final GlobalSessionProvider sessionProvider;
    private final SessionService sessionService;
    private final ApplicationTicketResolver ticketResolver;

    @Inject
    public ApplicationServiceGrpcImpl(final AppMode mode,
                                      final GlobalSessionProvider globalSessionProvider,
                                      final SessionService sessionService,
                                      final ApplicationTicketResolver ticketResolver) {
        this.mode = mode;
        this.sessionProvider = globalSessionProvider;
        this.sessionService = sessionService;
        this.ticketResolver = ticketResolver;
    }

    public void validateFields() {
        // Let's ensure that we can create field info for every exposed field.
        ticketResolver.visitAllApplications(app -> {
            app.listFields().forEach(field -> getFieldInfo(app, field));
        });
    }

    @Override
    public void listFields(ListFieldsRequest request, StreamObserver<FieldsChangeUpdate> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getOptionalSession();
            final FieldsChangeUpdate.Builder responseBuilder = FieldsChangeUpdate.newBuilder();

            if (mode.hasVisibilityToAppExports()) {
                ticketResolver.visitAllApplications(app -> {
                    app.listFields().forEach(field -> responseBuilder.addFields(getFieldInfo(app, field)));
                });
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

    private static FieldInfo getFieldInfo(final ApplicationState app, final Field<?> field) {
        if (field instanceof CustomField) {
            return getCustomFieldInfo(app, (CustomField<?>) field);
        }
        return getStandardFieldInfo(app, field);
    }

    private static FieldInfo getCustomFieldInfo(final ApplicationState app, final CustomField<?> field) {
        return FieldInfo.newBuilder()
                .setTicket(app.ticketForField(field))
                .setFieldName(field.name())
                .setFieldType(fetchFieldType(field.value()))
                .setFieldDescription(field.description().orElse(""))
                .build();
    }

    private static FieldInfo getStandardFieldInfo(final ApplicationState app, final Field<?> field) {
        // Note that this method accepts any Field and not just StandardField
        final FieldInfo.FieldType fieldType = fetchFieldType(field.value());

        if (fieldType == null) {
            throw new IllegalArgumentException("Application Field is not of standard type; use CustomField instead");
        }

        return FieldInfo.newBuilder()
                .setTicket(app.ticketForField(field))
                .setFieldName(field.name())
                .setFieldType(fieldType)
                .setFieldDescription(field.description().orElse(""))
                .build();
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
