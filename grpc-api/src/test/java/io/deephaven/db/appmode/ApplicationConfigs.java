package io.deephaven.db.appmode;

import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.utils.TableTools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApplicationConfigs {
    static StaticClassApplication<App00> app00() {
        return StaticClassApplication.of(App00.class);
    }

    static GroovyScriptApplication app01() {
        return GroovyScriptApplication.builder()
                .id(ApplicationConfigs.class.getName() + ".app01")
                .name("My Groovy Application")
                .addFiles(Paths.get("01-groovy.groovy"))
                .build();
    }

    static PythonScriptApplication app02() {
        return PythonScriptApplication.builder()
                .id(ApplicationConfigs.class.getName() + ".app02")
                .name("My Python Application")
                .addFiles(Paths.get("02-python.py"))
                .build();
    }

    static QSTApplication app03() {
        return QSTApplication.of();
    }

    static DynamicApplication<App04> app04() {
        return DynamicApplication.of(App04.class);
    }

    static Path resolve(String path) {
        return testAppDir().resolve(path);
    }

    static Path testAppDir() {
        // TODO: figure out why we do this in our top level build.gradle
        // b/c referring to the checked out directory name "deephaven-core" is very fragile
        //workingDir = "$rootDir/.."
        return Paths.get("core/grpc-api/src/test/app.d");
    }

    public static class App00 implements Application.Factory {

        @Override
        public final Application create() {
            Field<Table> hello = StandardField.of("hello", TableTools.emptyTable(42).view("I=i"), "A table with one column 'I' and 42 rows, 0-41.");
            Field<Table> world = StandardField.of("world", TableTools.timeTable("00:00:01"));
            return Application.builder()
                    .id(App00.class.getName())
                    .name("My Class Application")
                    .fields(Fields.of(hello, world))
                    .build();
        }
    }

    public static class App04 implements ApplicationState.Factory {

        @Override
        public ApplicationState create() {
            final CountDownLatch latch = new CountDownLatch(1);
            final ApplicationState state = new ApplicationState("", "My Dynamic Application") {
                @Override
                public void shutdown() {
                    latch.countDown();
                }
            };
            state.setField("initial_field", TableTools.timeTable("00:00:01"));
            final Thread thread = new Thread(() -> {
                for (int i = 0; ; ++i) {
                    state.setField(String.format("field_%d", i), TableTools.emptyTable(i).view("I=i").tail(1));
                    try {
                        if (latch.await(1, TimeUnit.SECONDS)) {
                            return;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            return state;
        }
    }
}
