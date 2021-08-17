package io.deephaven.db.appmode;

import java.util.Objects;

class ApplicationStateExec implements ApplicationConfig.Visitor {

    public static ApplicationState of(ApplicationConfig config) {
        return config.walk(new ApplicationStateExec()).out();
    }

    private ApplicationState out;

    private ApplicationStateExec() {}

    public ApplicationState out() {
        return Objects.requireNonNull(out);
    }

    @Override
    public void visit(ApplicationGroovyScript script) {
        out = ApplicationExec.of(script).toState();
    }

    @Override
    public void visit(ApplicationPythonScript script) {
        out = ApplicationExec.of(script).toState();
    }

    @Override
    public void visit(ApplicationQST qst) {
        out = ApplicationExec.of(qst).toState();
    }

    @Override
    public void visit(ApplicationClass<?> clazz) {
        out = ApplicationExec.of(clazz).toState();
    }

    @Override
    public void visit(ApplicationAdvanced<?> advanced) {
        try {
            out = advanced.create();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
