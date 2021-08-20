package io.deephaven.appmode;

import io.deephaven.db.appmode.ApplicationConfig;
import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.grpc_api.app_mode.ApplicationTicketResolver;
import io.deephaven.grpc_api.console.GlobalSessionProvider;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ApplicationInjector {

    private static final Logger log = LoggerFactory.getLogger(ApplicationInjector.class);

    private final GlobalSessionProvider globalSessionProvider;
    private final ApplicationTicketResolver ticketResolver;
    private final ApplicationState.Listener applicationListener;

    @Inject
    public ApplicationInjector(final GlobalSessionProvider globalSessionProvider,
                               final ApplicationTicketResolver ticketResolver,
                               final ApplicationState.Listener applicationListener) {
        this.globalSessionProvider = Objects.requireNonNull(globalSessionProvider);
        this.ticketResolver = ticketResolver;
        this.applicationListener = applicationListener;
    }

    public void run() throws IOException, ClassNotFoundException {
        if (!ApplicationConfig.isEnabled()) {
            return;
        }

        final Path applicationDir = ApplicationConfig.applicationDir();
        log.info().append("Finding application(s) in '").append(applicationDir.toString()).append("'...").endl();

        final List<ApplicationConfig> configs = ApplicationConfig.find();
        if (configs.isEmpty()) {
            log.warn().append("No application(s) found...").endl();
            return;
        }

        for (ApplicationConfig config : configs) {
            loadApplication(applicationDir, config);
        }
    }

    private void loadApplication(final Path applicationDir, final ApplicationConfig config) {
        // Note: if we need to be more specific about which application we are starting, we can print out the path of the application.
        log.info().append("Starting application '").append(config.toString()).append('\'').endl();
        final ApplicationState app = ApplicationFactory.create(applicationDir, config,
                globalSessionProvider.getGlobalSession(), applicationListener);

        int numExports = app.listFields().size();
        log.info().append("\tfound ").append(numExports).append(" exports").endl();

        ticketResolver.onApplicationLoad(app);
    }
}
