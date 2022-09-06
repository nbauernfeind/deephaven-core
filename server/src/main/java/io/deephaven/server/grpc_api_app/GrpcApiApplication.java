package io.deephaven.server.grpc_api_app;

import com.google.auto.service.AutoService;
import io.deephaven.appmode.ApplicationState;
import io.deephaven.configuration.Configuration;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.impl.sources.ring.RingTableTools;
import io.deephaven.engine.updategraph.UpdateGraphProcessor;
import io.deephaven.engine.util.TableTools;
import io.deephaven.proto.backplane.grpc.ExportNotification;
import io.deephaven.server.session.SessionState;
import io.deephaven.stream.StreamToTableAdapter;
import io.grpc.stub.StreamObserver;

/**
 * The {@value APP_NAME}, application id {@value APP_ID}, produces stream {@link io.deephaven.engine.table.Table tables}
 * {@value NOTIFICATION_INFO}; and derived table {@value NOTIFICATION_INFO_RING}. This data is modeled after the
 * {@link ExportNotification} event information from {@link SessionState#addExportListener(StreamObserver)}.
 *
 * @see #ENABLED
 * @see #RING_SIZE
 */
@AutoService(ApplicationState.Factory.class)
public final class GrpcApiApplication implements ApplicationState.Factory {
    private static final String APP_ID = "GRPC_API_APP";
    private static final String APP_NAME = "GRPC API Application";
    private static final String NOTIFICATION_INFO = "session_export_notification_info";
    private static final String NOTIFICATION_INFO_RING = "session_export_notification_info_ring";

    private static final String ENABLED = "enabled";
    private static final String RING_SIZE = "ringSize";

    private static Table updateStreamTable = TableTools.emptyTable(0);

    public static Table getExportObjectUpdateStreamTable() {
        return updateStreamTable;
    }

    // TODO: This is a hack as Configuration is not loaded until after the ApplicationState.Factory is loaded.
    private static boolean enabled() {
        return "true".equals(System.getProperty(GrpcApiApplication.class + "." + ENABLED, "true"));
    }
    private static int ringSize() {
        return Integer.getInteger(GrpcApiApplication.class + "." + RING_SIZE, 1024);
    }

    @Override
    public ApplicationState create(ApplicationState.Listener listener) {
        final ApplicationState state = new ApplicationState(listener, APP_ID, APP_NAME);
        if (!enabled()) {
            return state;
        }
        final SessionStateExportObjectUpdateStream updateStream = new SessionStateExportObjectUpdateStream();
        SessionState.LISTENER = updateStream::onExportObjectStateUpdate;
        final StreamToTableAdapter adapter = new StreamToTableAdapter(
                SessionStateExportObjectUpdateStream.definition(), updateStream,
                UpdateGraphProcessor.DEFAULT, NOTIFICATION_INFO);
        updateStreamTable = adapter.table();
        state.setField(NOTIFICATION_INFO, updateStreamTable);
        final int ringSize = ringSize();
        if (ringSize > 0) {
            state.setField(NOTIFICATION_INFO_RING, RingTableTools.of(updateStreamTable, ringSize));
        }
        return state;
    }
}
