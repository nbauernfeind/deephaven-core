package io.deephaven.db.appmode;

import io.deephaven.db.tables.Table;
import io.deephaven.db.tables.utils.TableTools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApplicationConfigs {
    static StaticClassApplication<App003> app00() {
        return StaticClassApplication.<App003>builder()
                .id(ApplicationConfigs.class.getName() + ".app00")
                .name("Static Class Application")
                .clazz(App003.class)
                .build();
    }

    static GroovyScriptApplication app01() {
        return GroovyScriptApplication.builder()
                .id(ApplicationConfigs.class.getName() + ".app01")
                .name("My Groovy Application")
                .addFiles(resolve("01-groovy.groovy"))
                .build();
    }

    static PythonScriptApplication app02() {
        return PythonScriptApplication.builder()
                .id(ApplicationConfigs.class.getName() + ".app02")
                .name("My Python Application")
                .addFiles(resolve("02-python.py"))
                .build();
    }

    static QSTApplication app03() {
        return QSTApplication.builder()
                .id(ApplicationConfigs.class.getName() + ".app02")
                .name("My QST Application")
                .addFiles(resolve("03-qst.qst"))
                .build();
    }

    static DynamicApplication<App004> app04() {
        return DynamicApplication.<App004>builder()
                .id(ApplicationConfigs.class.getName() + ".app02")
                .name("My Dynamic Application")
                .clazz(App004.class)
                .build();
    }

    static Path resolve(String path) {
        return testAppDir().resolve(path);
    }

    static Path testAppDir() {
        // TODO: figure out why we do this in our top level build.gradle
        // b/c referring to the checked out directory name "deephaven-core" is very fragile
        //workingDir = "$rootDir/.."
        return Paths.get("deephaven-core/grpc-api/src/test/app.d");
    }

    public static class App003 implements Application.Factory {

        @Override
        public final Application create() {
            Field<Table> hello = Field.of("hello", TableTools.emptyTable(42).view("I=i"), "A table with one column 'I' and 42 rows, 0-41.");
            Field<Table> world = Field.of("world", TableTools.timeTable("00:00:01"));
            return Application.builder()
                    .id(App003.class.getName())
                    .name("My Class Application")
                    .fields(Fields.of(hello, world))
                    .build();
        }
    }

    public static class App004 implements ApplicationState.Factory {

        @Override
        public ApplicationState create() {
            final CountDownLatch latch = new CountDownLatch(1);
            final ApplicationState state = new ApplicationState("", "My Dynamic Application") {
                @Override
                public void shutdown() {
                    latch.countDown();
                }
            };
            state.setField(Field.of("initial_field", TableTools.timeTable("00:00:01")));
            final Thread thread = new Thread(() -> {
                for (int i = 0; ; ++i) {
                    state.setField(Field.of(String.format("field_%d", i), TableTools.emptyTable(i).view("I=i").tail(1)));
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
