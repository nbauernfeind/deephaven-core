package io.deephaven.appmode;

import io.deephaven.db.appmode.DynamicApplication;
import io.deephaven.db.appmode.QSTApplication;
import io.deephaven.db.appmode.StaticClassApplication;
import io.deephaven.db.appmode.ApplicationConfig;
import io.deephaven.db.appmode.GroovyScriptApplication;
import io.deephaven.db.appmode.PythonScriptApplication;
import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.db.util.GroovyDeephavenSession;
import io.deephaven.db.util.PythonDeephavenSession;
import io.deephaven.db.util.ScriptSession;

import java.util.Objects;

public class ApplicationFactory implements ApplicationConfig.Visitor {

    public static ApplicationState create(ApplicationConfig config, ScriptSession scriptSession) {
        return config.walk(new ApplicationFactory(scriptSession)).out();
    }

    private final ScriptSession scriptSession;
    private ApplicationState out;

    private ApplicationFactory(final ScriptSession scriptSession) {
        this.scriptSession = scriptSession;
    }

    public ApplicationState out() {
        return Objects.requireNonNull(out);
    }

    @Override
    public void visit(GroovyScriptApplication script) {
        if (!(scriptSession instanceof GroovyDeephavenSession)) {
            throw new IllegalArgumentException(String.format(
                    "Cannot instantiate Groovy application on a %s script session", scriptSession.scriptType()));
        }

        out = new ApplicationState(script.id(), script.name());
        ApplicationContext.runUnderContext(out, () -> {
            script.files().forEach(scriptSession::evaluateScript);
        });
    }

    @Override
    public void visit(PythonScriptApplication script) {
        if (!(scriptSession instanceof PythonDeephavenSession)) {
            throw new IllegalArgumentException(String.format(
                    "Cannot instantiate Python application on a %s script session", scriptSession.scriptType()));
        }

        out = new ApplicationState(script.id(), script.name());
        ApplicationContext.runUnderContext(out, () -> {
            script.files().forEach(scriptSession::evaluateScript);
        });
    }

    @Override
    public void visit(DynamicApplication<?> advanced) {
        try {
            out = advanced.create();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(QSTApplication qst) {
        throw new UnsupportedOperationException("TODO, QST");
    }

    @Override
    public void visit(StaticClassApplication<?> clazz) {
        try {
            out = clazz.create().toState();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
