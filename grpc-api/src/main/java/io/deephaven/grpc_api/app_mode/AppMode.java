package io.deephaven.grpc_api.app_mode;

import io.deephaven.db.appmode.ApplicationConfig;

public enum AppMode {
    RESTRICTED, HYBRID, CONSOLE_ONLY, API_ONLY;

    public static AppMode currentMode() {
        boolean appEnabled = ApplicationConfig.isEnabled();
        boolean consoleEnabled = Boolean.getBoolean("console.enabled");
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
