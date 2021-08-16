package io.deephaven.grpc_api.app_mode;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.deephaven.grpc_api.session.TicketResolver;
import io.grpc.BindableService;

@Module
public interface AppModeModule {
    @Binds
    @IntoSet
    BindableService bindFieldServiceImpl(FieldServiceGrpcImpl fieldService);

    @Binds @IntoSet
    TicketResolver bindFieldTicketResolver(FieldTicketResolver resolver);
}
