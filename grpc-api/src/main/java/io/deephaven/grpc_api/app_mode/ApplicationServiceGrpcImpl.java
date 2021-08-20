package io.deephaven.grpc_api.app_mode;

import com.google.rpc.Code;
import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.db.appmode.CustomField;
import io.deephaven.db.appmode.Field;
import io.deephaven.db.plot.Figure;
import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.utils.DBTimeUtils;
import io.deephaven.db.util.ScriptSession;
import io.deephaven.grpc_api.barrage.util.BarrageSchemaUtil;
import io.deephaven.grpc_api.session.SessionService;
import io.deephaven.grpc_api.session.SessionState;
import io.deephaven.grpc_api.util.GrpcUtil;
import io.deephaven.grpc_api.util.Scheduler;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import io.deephaven.proto.backplane.grpc.CustomInfo;
import io.deephaven.proto.backplane.grpc.FieldInfo;
import io.deephaven.proto.backplane.grpc.ApplicationServiceGrpc;
import io.deephaven.proto.backplane.grpc.FieldsChangeUpdate;
import io.deephaven.proto.backplane.grpc.FigureInfo;
import io.deephaven.proto.backplane.grpc.ListFieldsRequest;
import io.deephaven.proto.backplane.grpc.RemovedField;
import io.deephaven.proto.backplane.grpc.TableInfo;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class ApplicationServiceGrpcImpl extends ApplicationServiceGrpc.ApplicationServiceImplBase
        implements ScriptSession.Listener, ApplicationState.Listener {
    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceGrpcImpl.class);

    private final AppMode mode;
    private final Scheduler scheduler;
    private final SessionService sessionService;

    /** The list of Field listeners */
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    /** A schedulable job that flushes pending field changes to all listeners. */
    private final FieldUpdatePropagationJob propagationJob = new FieldUpdatePropagationJob();

    /** Which fields have been removed since we last propagated? */
    private final Set<AppFieldId> removedFields = new HashSet<>();
    /** Which fields have been created/updated since we last propagated? */
    private final Set<AppFieldId> updatedFields = new HashSet<>();
    /** Which [remaining] fields have we seen? */
    private final Map<AppFieldId, Field<?>> knownFieldMap = new HashMap<>();

    @Inject
    public ApplicationServiceGrpcImpl(final AppMode mode,
                                      final Scheduler scheduler,
                                      final SessionService sessionService) {
        this.mode = mode;
        this.scheduler = scheduler;
        this.sessionService = sessionService;
    }

    @Override
    public synchronized void onScopeChanges(final ScriptSession scriptSession, final ScriptSession.Changes changes) {
        if (!mode.hasVisibilityToConsoleExports()) {
            return;
        }

        changes.removed.keySet().stream().map(AppFieldId::fromScopeName).forEach(id -> {
            updatedFields.remove(id);
            removedFields.add(id);
            knownFieldMap.remove(id);
        });

        for (final String name : changes.updated.keySet()) {
            final AppFieldId id = AppFieldId.fromScopeName(name);
            final ScopeField field = (ScopeField) knownFieldMap.get(id);
            field.value = scriptSession.getVariable(name);
            updatedFields.add(id);
        }

        for (final String name : changes.created.keySet()) {
            final AppFieldId id = AppFieldId.fromScopeName(name);
            final ScopeField field = new ScopeField(name, scriptSession.getVariable(name));
            final FieldInfo fieldInfo = getFieldInfo(id, field);
            if (fieldInfo == null) {
                // The script session should not have told us about this variable...
                throw new IllegalStateException(String.format("Field information could not be generated for scope variable '%s'", name));
            }
            updatedFields.add(id);
            knownFieldMap.put(id, field);
        }

        if (!subscriptions.isEmpty()) {
            propagationJob.schedulePropagation();
        }
    }

    @Override
    public synchronized void onRemoveField(ApplicationState app, String name) {
        if (!mode.hasVisibilityToAppExports()) {
            return;
        }

        final AppFieldId id = AppFieldId.from(app, name);
        if (knownFieldMap.remove(id) != null) {
            removedFields.add(id);
        }
    }

    @Override
    public synchronized void onNewField(final ApplicationState app, final Field<?>field) {
        if (!mode.hasVisibilityToAppExports()) {
            return;
        }

        final AppFieldId id = AppFieldId.from(app, field.name());
        final FieldInfo fieldInfo = getFieldInfo(id, field);
        if (fieldInfo == null) {
            throw new IllegalStateException(String.format("Field information could not be generated for field '%s/%s'", app.id(), field.name()));
        }

        if (fieldInfo.getFieldType().getFieldCase() == FieldInfo.FieldType.FieldCase.REMOVED) {
            updatedFields.remove(id);
            removedFields.add(id);
            knownFieldMap.remove(id);
        } else {
            updatedFields.add(id);
            knownFieldMap.put(id, field);
        }

        if (!subscriptions.isEmpty()) {
            propagationJob.schedulePropagation();
        }
    }

    private synchronized void propagateUpdates() {
        final FieldsChangeUpdate.Builder builder = FieldsChangeUpdate.newBuilder();
        removedFields.forEach(id -> builder.addFields(getRemovedFieldInfo(id)));
        removedFields.clear();
        updatedFields.forEach(id -> builder.addFields(getFieldInfo(id, knownFieldMap.get(id))));
        updatedFields.clear();
        final FieldsChangeUpdate update = builder.build();
        subscriptions.forEach(sub -> sub.send(update));
    }

    @Override
    public synchronized void listFields(ListFieldsRequest request, StreamObserver<FieldsChangeUpdate> responseObserver) {
        GrpcUtil.rpcWrapper(log, responseObserver, () -> {
            final SessionState session = sessionService.getCurrentSession();
            final FieldsChangeUpdate.Builder responseBuilder = FieldsChangeUpdate.newBuilder();

            knownFieldMap.forEach((appFieldId, field) -> responseBuilder.addFields(getFieldInfo(appFieldId, field)));

            responseObserver.onNext(responseBuilder.build());
            subscriptions.add(new Subscription(session, responseObserver));
        });
    }

    private static FieldInfo getRemovedFieldInfo(final AppFieldId id) {
        return FieldInfo.newBuilder()
                .setTicket(id.getTicket())
                .setFieldName(id.fieldName)
                .setFieldType(FieldInfo.FieldType.newBuilder().setRemoved(RemovedField.getDefaultInstance()).build())
                .build();
    }
    private static FieldInfo getFieldInfo(final AppFieldId id, final Field<?> field) {
        if (field instanceof CustomField) {
            return getCustomFieldInfo(id, (CustomField<?>) field);
        }
        return getStandardFieldInfo(id, field);
    }

    private static FieldInfo getCustomFieldInfo(final AppFieldId id, final CustomField<?> field) {
        return FieldInfo.newBuilder()
                .setTicket(id.getTicket())
                .setFieldName(id.fieldName)
                .setFieldType(FieldInfo.FieldType.newBuilder()
                        .setCustom(CustomInfo.newBuilder()
                                .setType(field.type())
                                .build())
                        .build())
                .setFieldDescription(field.description().orElse(""))
                .setApplicationId(id.applicationId())
                .setApplicationName(id.applicationName())
                .build();
    }

    private static FieldInfo getStandardFieldInfo(final AppFieldId id, final Field<?> field) {
        // Note that this method accepts any Field and not just StandardField
        final FieldInfo.FieldType fieldType = fetchFieldType(field.value());

        if (fieldType == null) {
            throw new IllegalArgumentException("Application Field is not of standard type; use CustomField instead");
        }

        return FieldInfo.newBuilder()
                .setTicket(id.getTicket())
                .setFieldName(id.fieldName)
                .setFieldType(fieldType)
                .setFieldDescription(field.description().orElse(""))
                .setApplicationId(id.applicationId())
                .setApplicationName(id.applicationName())
                .build();
    }

    private static FieldInfo.FieldType fetchFieldType(final Object obj) {
        if (obj == null) {
            return FieldInfo.FieldType.newBuilder().setRemoved(RemovedField.getDefaultInstance()).build();
        }
        if (obj instanceof Table) {
            final Table table = (Table) obj;
            return FieldInfo.FieldType.newBuilder().setTable(TableInfo.newBuilder()
                    .setSchemaHeader(BarrageSchemaUtil.schemaBytesFromTable(table))
                    .setIsStatic(!table.isLive())
                    .setSize(table.size())
                    .build()).build();
        }
        if (obj instanceof Figure) {
            return FieldInfo.FieldType.newBuilder().setFigure(FigureInfo.getDefaultInstance()).build();
        }

        return null;
    }

    private static class ScopeField implements Field<Object> {
        final String name;
        Object value;

        ScopeField(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public Optional<String> description() {
            return Optional.of("query scope variable");
        }
    }

    private class FieldUpdatePropagationJob implements Runnable {
        /** This interval is used as a debounce to prevent spamming field changes from a broken application. */
        private static final long UPDATE_INTERVAL_MS = 250;
        /** The last time the update propagation job ran. */
        private volatile long lastUpdateTime = 0;

        private final ReentrantLock runLock = new ReentrantLock();
        private final AtomicBoolean needsRun = new AtomicBoolean();

        @Override
        public void run() {
            needsRun.set(true);
            while (true) {
                if (!runLock.tryLock()) {
                    // if we can't get a lock, the thread that lets it go will check before exiting the method
                    return;
                }

                try {
                    if (needsRun.compareAndSet(true, false)) {
                        lastUpdateTime = scheduler.currentTime().getMillis();
                        propagateUpdates();
                    }
                } catch (final Exception exception) {
                    log.error().append("failed to propagate field changes: ").append(exception).endl();
                } finally {
                    runLock.unlock();
                }

                if (!needsRun.get()) {
                    return;
                }
            }
        }

        public void scheduleImmediately() {
            if (needsRun.compareAndSet(false, true) && !runLock.isLocked()) {
                scheduler.runImmediately(this);
            }
        }

        public void scheduleAt(final long nextRunTime) {
            scheduler.runAtTime(DBTimeUtils.millisToTime(nextRunTime), this);
        }

        public void schedulePropagation() {
            final long now = scheduler.currentTime().getMillis();
            final long msSinceLastUpdate = now - lastUpdateTime;
            if (msSinceLastUpdate < UPDATE_INTERVAL_MS) {
                // we have updated within the period, so wait until a sufficient gap
                scheduleAt(lastUpdateTime + UPDATE_INTERVAL_MS);
            } else {
                // we have not updated recently, so go for it right away
                scheduleImmediately();
            }
        }
    }

    /**
     * Subscription is a small helper class that kills the listener's subscription when its session expires.
     *
     * @implNote gRPC observers are not thread safe; we must synchronize around observer communication
     */
    private class Subscription implements Closeable {
        private final SessionState session;
        private final StreamObserver<FieldsChangeUpdate> observer;

        public Subscription(final SessionState session, final StreamObserver<FieldsChangeUpdate> observer) {
            this.session = session;
            this.observer = observer;
            if (observer instanceof ServerCallStreamObserver) {
                final ServerCallStreamObserver<FieldsChangeUpdate> serverCall = (ServerCallStreamObserver<FieldsChangeUpdate>) observer;
                serverCall.setOnCancelHandler(this::onCancel);
            }
            session.addOnCloseCallback(this);
        }

        synchronized void send(FieldsChangeUpdate changes) {
            try {
                observer.onNext(changes);
            } catch (RuntimeException ignored) {
                onCancel();
            }
        }

        void onCancel() {
            if (session.removeOnCloseCallback(this)) {
                close();
            }
        }

        @Override
        public synchronized void close() {
            synchronized (ApplicationServiceGrpcImpl.this) {
                subscriptions.remove(this);
            }
            GrpcUtil.safelyExecute(() -> observer.onError(GrpcUtil.statusRuntimeException(Code.ABORTED, "subscription cancelled")));
        }
    }
}
