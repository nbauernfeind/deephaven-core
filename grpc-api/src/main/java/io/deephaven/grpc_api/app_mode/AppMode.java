package io.deephaven.grpc_api.app_mode;

public enum AppMode {
    RESTRICTED, HYBRID, CONSOLE_ONLY;

    public boolean hasVisibilityToAppExports() {
        return this != CONSOLE_ONLY;
    }
    public boolean hasVisibilityToConsoleExports() {
        return this != RESTRICTED;
    }
}
