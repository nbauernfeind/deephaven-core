package io.deephaven.appmode;

import io.deephaven.db.appmode.ApplicationConfig;
import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.db.appmode.DynamicApplication;
import io.deephaven.db.appmode.GroovyScriptApplication;
import io.deephaven.db.appmode.PythonScriptApplication;
import io.deephaven.db.appmode.QSTApplication;
import io.deephaven.db.appmode.StaticClassApplication;
import io.deephaven.grpc_api.app_mode.ApplicationTicketResolver;
import io.deephaven.grpc_api.console.GlobalSessionProvider;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ApplicationInjector {

    private static final Logger log = LoggerFactory.getLogger(ApplicationInjector.class);

    private final GlobalSessionProvider globalSessionProvider;
    private final ApplicationTicketResolver ticketResolver;

    @Inject
    public ApplicationInjector(final GlobalSessionProvider globalSessionProvider,
                               final ApplicationTicketResolver ticketResolver) {
        this.globalSessionProvider = Objects.requireNonNull(globalSessionProvider);
        this.ticketResolver = ticketResolver;
    }

    public void run() throws IOException, ClassNotFoundException {
        if (!ApplicationConfig.isEnabled()) {
            return;
        }

        log.info().append("Finding application(s)...").endl();
        final List<ApplicationConfig> configs = ApplicationConfig.find();

        if (configs.isEmpty()) {
            log.warn().append("No application(s) found...").endl();
            return;
        }

        configs.forEach(this::loadApplication);
    }

    private void loadApplication(final ApplicationConfig config) {
        log.info().append("Found application config: ").append(config.toString()).endl();

        final NameVisitor nameVisitor = new NameVisitor();
        config.walk(nameVisitor);
        log.info().append("Starting application '").append(nameVisitor.name).append('\'').endl();
        final ApplicationState app = ApplicationFactory.create(config, globalSessionProvider.getGlobalSession());

        int numExports = app.listFields().size();
        log.info().append("\tfound ").append(numExports).append(" exports").endl();

        ticketResolver.onApplicationLoad(app);
    }

    private static class NameVisitor implements ApplicationConfig.Visitor {
        private String name;

        @Override
        public void visit(GroovyScriptApplication script) {
            name = script.name();
        }

        @Override
        public void visit(PythonScriptApplication script) {
            name = script.name();
        }

        @Override
        public void visit(StaticClassApplication<?> clazz) {
            name = clazz.name();
        }

        @Override
        public void visit(QSTApplication qst) {
            name = qst.name();
        }

        @Override
        public void visit(DynamicApplication<?> advanced) {
            name = advanced.name();
        }
    }
}
