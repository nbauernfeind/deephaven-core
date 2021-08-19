package io.deephaven.grpc_api.app_mode;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.db.util.ScriptSession;
import io.deephaven.grpc_api.session.TicketResolver;
import io.grpc.BindableService;

@Module
public interface AppModeModule {
    @Binds
    @IntoSet
    BindableService bindApplicationServiceImpl(ApplicationServiceGrpcImpl applicationService);

    @Binds @IntoSet
    TicketResolver bindApplicationTicketResolver(ApplicationTicketResolver resolver);

    @Binds
    ScriptSession.Listener bindScriptSessionListener(ApplicationServiceGrpcImpl applicationService);

    @Binds
    ApplicationState.Listener bindApplicationStateListener(ApplicationServiceGrpcImpl applicationService);
}
