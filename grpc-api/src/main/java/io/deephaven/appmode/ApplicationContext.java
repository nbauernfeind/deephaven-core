package io.deephaven.appmode;

import io.deephaven.db.appmode.ApplicationState;

import java.util.concurrent.Callable;

/**
 * This application context can be used to get access to the application state from
 * within script applications.
 */
public class ApplicationContext {
    private static ApplicationState currentContext = null;

    public static ApplicationState get() {
        return currentContext;
    }

    public static void runUnderContext(final ApplicationState context, final Runnable runner) {
        try {
            currentContext = context;
            runner.run();
        } finally {
            currentContext = null;
        }
    }
}
