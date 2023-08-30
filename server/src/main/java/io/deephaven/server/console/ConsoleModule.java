/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.server.console;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import io.deephaven.appmode.ApplicationState;
import io.deephaven.configuration.Configuration;
import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.updategraph.UpdateGraph;
import io.deephaven.engine.updategraph.impl.PeriodicUpdateGraph;
import io.deephaven.io.logger.LogBuffer;
import io.deephaven.lang.completion.CustomCompletion;
import io.deephaven.server.session.TicketResolver;
import io.deephaven.server.util.Scheduler;
import io.deephaven.util.SafeCloseable;
import io.grpc.BindableService;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;

@Module
public interface ConsoleModule {
    int CONSOLE_MIN_UPDATE_INTERVAL =
            Configuration.getInstance().getIntegerWithDefault("console.minUpdateInterval", 50);

    @Binds
    @IntoSet
    BindableService bindConsoleServiceImpl(ConsoleServiceGrpcBinding consoleService);

    @Binds
    @IntoSet
    TicketResolver bindConsoleTicketResolver(ScopeTicketResolver resolver);

    @Provides
    @ElementsIntoSet
    static Set<CustomCompletion.Factory> primeCustomCompletions() {
        return Collections.emptySet();
    }

    @Provides
    @Singleton
    static ConsoleTableImpl provideConsoleTable(
            final LogBuffer logBuffer,
            final Scheduler scheduler) {
        final UpdateGraph updateGraph = PeriodicUpdateGraph.newBuilder("ConsoleUpdateGraph")
                .numUpdateThreads(1)
                .targetCycleDurationMillis(CONSOLE_MIN_UPDATE_INTERVAL)
                .existingOrBuild();
        try (final SafeCloseable ignored = ExecutionContext.getContext().withUpdateGraph(updateGraph).open()) {
            return new ConsoleTableImpl(
                    new ConsoleTableImpl.TableAccess(), logBuffer, scheduler, 1000, 200);
        }
    }

    @Binds
    @IntoSet
    ApplicationState.Factory bindConsoleTableApplication(ConsoleTableApplication consoleTableApplication);
}
