package io.deephaven.appmode;

import io.deephaven.db.appmode.ApplicationState;

/**
 * This application context can be used to get access to the application state from
 * within script applications.
 */
public class ApplicationContext {

    private static final ThreadLocal<ApplicationState> states = new ThreadLocal<>();

    public static ApplicationState get() {
        final ApplicationState state = states.get();
        if (state == null) {
            throw new IllegalStateException("Should not be getting application state outside runUnderContext");
        }
        return state;
    }

    static void runUnderContext(final ApplicationState context, final Runnable runner) {
        ApplicationContext.states.set(context);
        try {
            runner.run();
        } finally {
            ApplicationContext.states.remove();
        }
    }
}
