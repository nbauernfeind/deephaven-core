package io.deephaven.db.appmode;

import io.deephaven.db.appmode.ApplicationState.Factory;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import java.util.Properties;

@Immutable
public abstract class ApplicationAdvanced<T extends Factory> implements ApplicationConfig {

    public static final String TYPE = "advanced";

    public static <T extends Factory> ApplicationAdvanced<T> of(Class<T> clazz) {
        return ImmutableApplicationAdvanced.of(clazz);
    }

    public static ApplicationAdvanced<?> parse(Properties properties)
        throws ClassNotFoundException {
        Class<?> clazz = Class.forName(properties.getProperty("class"));
        // noinspection unchecked
        return of((Class<Factory>) clazz);
    }

    @Parameter
    public abstract Class<T> clazz();

    public final ApplicationState create() throws InstantiationException, IllegalAccessException {
        return clazz().newInstance().create();
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
