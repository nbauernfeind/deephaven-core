package io.deephaven.server.console;

import io.deephaven.appmode.ApplicationState;
import io.deephaven.engine.liveness.LivenessScope;
import io.deephaven.engine.liveness.LivenessScopeStack;
import io.deephaven.engine.rowset.impl.RefCountedCow;
import io.deephaven.util.SafeCloseable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class ConsoleTableApplication implements ApplicationState.Factory {

    private static final String APP_NAME = "Console Table Application";
    private static final String APP_ID = "console_app";
    private static final String CONSOLE_TABLE = "console_table";


    private final Provider<ConsoleTableImpl> consoleTableImpl;
    @SuppressWarnings("FieldCanBeLocal")
    private LivenessScope scope;

    @Inject
    public ConsoleTableApplication(final Provider<ConsoleTableImpl> consoleTableImpl) {
        this.consoleTableImpl = consoleTableImpl;
        // force load the following classes that cause deadlocks:
        // forceInit(RefCountedCow.class);
    }

    @Override
    public ApplicationState create(final ApplicationState.Listener listener) {
        final ApplicationState state = new ApplicationState(listener, APP_ID, APP_NAME);
        scope = new LivenessScope();
        ConsoleTableImpl console = consoleTableImpl.get();
        scope.manage(console);
        state.setField(CONSOLE_TABLE, console);
        return state;
    }

    private static <T> Class<T> forceInit(final Class<T> klass) {
        try {
            Class.forName(klass.getName(), true, klass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e); // Can't happen
        }
        return klass;
    }
}
