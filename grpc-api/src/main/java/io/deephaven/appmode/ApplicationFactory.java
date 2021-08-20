package io.deephaven.appmode;

import com.google.rpc.Code;
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
import io.deephaven.grpc_api.util.GrpcUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ApplicationFactory implements ApplicationConfig.Visitor {

    public static ApplicationState create(Path applicationDir, ApplicationConfig config, ScriptSession scriptSession, ApplicationState.Listener appStateListener) {
        return config.walk(new ApplicationFactory(applicationDir, scriptSession, appStateListener)).out();
    }

    private final Path applicationDir;
    private final ScriptSession scriptSession;
    private final ApplicationState.Listener appStateListener;

    private ApplicationState out;

    private ApplicationFactory(final Path applicationDir,
                               final ScriptSession scriptSession,
                               final ApplicationState.Listener appStateListener) {
        this.applicationDir = Objects.requireNonNull(applicationDir);
        this.scriptSession = scriptSession;
        this.appStateListener = appStateListener;
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

        out = new ApplicationState(appStateListener, script.id(), script.name());
        ApplicationContext.runUnderContext(out, () -> evaluateScripts(script.files()));
    }

    @Override
    public void visit(PythonScriptApplication script) {
        if (!(scriptSession instanceof PythonDeephavenSession)) {
            throw new IllegalArgumentException(String.format(
                    "Cannot instantiate Python application on a %s script session", scriptSession.scriptType()));
        }

        out = new ApplicationState(appStateListener, script.id(), script.name());
        ApplicationContext.runUnderContext(out, () -> evaluateScripts(script.files()));
    }

    @Override
    public void visit(DynamicApplication<?> advanced) {
        try {
            out = advanced.create(appStateListener);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(QSTApplication qst) {
        throw GrpcUtil.statusRuntimeException(Code.UNIMPLEMENTED, "See deephaven-core#1080; support qst application");
    }

    @Override
    public void visit(StaticClassApplication<?> clazz) {
        try {
            out = clazz.create().toState(appStateListener);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Path absolutePath(Path path) {
        return path.isAbsolute() ? path : applicationDir.resolve(path);
    }

    private void evaluateScripts(List<Path> files) {
        for (Path file : files) {
            scriptSession.evaluateScript(absolutePath(file));
        }
    }
}
