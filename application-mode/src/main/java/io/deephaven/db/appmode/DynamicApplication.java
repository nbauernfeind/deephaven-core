package io.deephaven.db.appmode;

import io.deephaven.db.appmode.ApplicationState.Factory;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import java.util.Properties;

@Immutable(builder = false)
public abstract class DynamicApplication<T extends Factory> implements ApplicationConfig {

    public static final String TYPE = "dynamic";

    public static DynamicApplication<Factory> parse(Properties properties)
        throws ClassNotFoundException {
        // noinspection unchecked
        Class<Factory> clazz = (Class<Factory>) Class.forName(properties.getProperty("class"));
        return of(clazz);
    }

    public static <T extends Factory> DynamicApplication<T> of(Class<T> clazz) {
        return ImmutableDynamicApplication.of(clazz);
    }

    @Parameter
    public abstract Class<T> clazz();

    public final ApplicationState create(ApplicationState.Listener appStateListener) throws InstantiationException, IllegalAccessException {
        return clazz().newInstance().create(appStateListener);
    }

    @Override
    public final <V extends Visitor> V walk(V visitor) {
        visitor.visit(this);
        return visitor;
    }

    @Check
    final void checkClazz() {
        if (!Factory.class.isAssignableFrom(clazz())) {
            throw new IllegalArgumentException(
                String.format("clazz should extend '%s'", Factory.class));
        }
    }
}
