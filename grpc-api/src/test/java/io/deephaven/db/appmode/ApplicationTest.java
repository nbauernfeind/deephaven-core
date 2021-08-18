package io.deephaven.db.appmode;

import io.deephaven.appmode.ApplicationFactory;
import io.deephaven.db.tables.Table;
import io.deephaven.db.util.AbstractScriptSession;
import io.deephaven.db.util.GroovyDeephavenSession;
import io.deephaven.db.util.PythonDeephavenSession;
import io.deephaven.db.v2.JUnit4QueryTableTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static io.deephaven.db.appmode.ApplicationConfigs.testAppDir;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {

    private final JUnit4QueryTableTestBase base = new JUnit4QueryTableTestBase();

    private AbstractScriptSession session = null;

    @Before
    public void setUp() throws Exception {
        base.setUp();
    }

    @After
    public void tearDown() throws Exception {
        base.tearDown();
        if (session != null) {
            session.release();
            session = null;
        }
    }

    @Test
    public void app00() {
        ApplicationState app = ApplicationFactory.create(testAppDir(), ApplicationConfigs.app00(), session);
        assertThat(app.name()).isEqualTo("My Class Application");
        assertThat(app.numFieldsExported()).isEqualTo(2);
        assertThat(app.getField("hello").value()).isInstanceOf(Table.class);
        assertThat(app.getField("world").value()).isInstanceOf(Table.class);
        app.shutdown();
    }

    @Test
    @Ignore("the working directory is not useful; cannot create path to app files")
    public void app01() throws IOException {
        session = new GroovyDeephavenSession(GroovyDeephavenSession.RunScripts.none(), false);
        ApplicationState app = ApplicationFactory.create(testAppDir(), ApplicationConfigs.app01(), session);
        assertThat(app.name()).isEqualTo("My Groovy Application");
        assertThat(app.numFieldsExported()).isEqualTo(2);
        assertThat(app.getField("hello").value()).isInstanceOf(Table.class);
        assertThat(app.getField("world").value()).isInstanceOf(Table.class);
        app.shutdown();
    }

    @Test
    @Ignore("python test needs to run in a container; also working directory is not useful")
    public void app02() throws IOException {
        session = new PythonDeephavenSession(false, false);
        ApplicationState app = ApplicationFactory.create(testAppDir(), ApplicationConfigs.app02(), session);
        assertThat(app.name()).isEqualTo("My Python Application");
        assertThat(app.numFieldsExported()).isEqualTo(2);
        assertThat(app.getField("hello").value()).isInstanceOf(Table.class);
        assertThat(app.getField("world").value()).isInstanceOf(Table.class);
        app.shutdown();
    }

    @Test
    @Ignore("TODO: deephaven-core#1080 support QST application")
    public void app03() {
        ApplicationState app = ApplicationFactory.create(testAppDir(), ApplicationConfigs.app03(), session);
        assertThat(app.name()).isEqualTo("My QST Application");
        assertThat(app.numFieldsExported()).isEqualTo(2);
        assertThat(app.getField("hello").value()).isInstanceOf(Table.class);
        assertThat(app.getField("world").value()).isInstanceOf(Table.class);
        app.shutdown();
    }

    @Test
    public void app04() {
        ApplicationState app = ApplicationFactory.create(testAppDir(), ApplicationConfigs.app04(), session);
        assertThat(app.name()).isEqualTo("My Dynamic Application");
        app.shutdown();
    }
}
