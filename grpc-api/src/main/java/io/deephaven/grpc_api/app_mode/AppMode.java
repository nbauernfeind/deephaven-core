package io.deephaven.grpc_api.app_mode;

import io.deephaven.db.appmode.ApplicationConfig;
import io.deephaven.grpc_api.console.ConsoleServiceGrpcImpl;

public enum AppMode {
    RESTRICTED, HYBRID, CONSOLE_ONLY, API_ONLY;

    public static AppMode currentMode() {
        boolean appEnabled = ApplicationConfig.isEnabled();
        boolean consoleEnabled = !ConsoleServiceGrpcImpl.REMOTE_CONSOLE_DISABLED;
        if (appEnabled && consoleEnabled) {
            return HYBRID;
        }
        if (appEnabled) {
            return RESTRICTED;
        }
        if (consoleEnabled) {
            return CONSOLE_ONLY;
        }
        return API_ONLY;
    }

    public boolean hasVisibilityToAppExports() {
        return this != CONSOLE_ONLY;
    }
    public boolean hasVisibilityToConsoleExports() {
        return this != RESTRICTED;
    }
}
