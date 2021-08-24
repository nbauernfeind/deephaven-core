package io.deephaven.grpc_api.console.scala;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.deephaven.db.util.ScriptSession;
import io.deephaven.scala.ScalaDeephavenSession;

@Module
public class ScalaConsoleSessionModule {
    @Provides
    @IntoMap
    @StringKey("scala")
    ScriptSession bindScriptSession(ScalaDeephavenSession scalaSession) {
        return scalaSession;
    }

    @Provides
    ScalaDeephavenSession bindGroovySession(final ScriptSession.Listener listener) {
        return new ScalaDeephavenSession(listener, true);
    }
}
