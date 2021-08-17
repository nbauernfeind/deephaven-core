package io.deephaven.db.appmode;

import io.deephaven.db.appmode.ApplicationState.Factory;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import java.util.Properties;

@Immutable
public abstract class DynamicApplication<T extends Factory> implements ApplicationConfig {

    public static final String TYPE = "dynamic";


    public static <T extends Factory> DynamicApplication.Builder<T> builder() {
        return ImmutableDynamicApplication.builder();
    }

    public static DynamicApplication<Factory> parse(Properties properties) throws ClassNotFoundException {
        //noinspection unchecked
        Class<Factory> clazz = (Class<Factory>) Class.forName(properties.getProperty("class"));
        return builder()
                .clazz(clazz)
                .name(properties.getProperty("name"))
                .build();
    }

    @Parameter
    public abstract String id();

    @Parameter
    public abstract String name();

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

    public interface Builder<T extends Factory> {

        Builder<T> id(String id);

        Builder<T> name(String name);

        Builder<T> clazz(Class<T> id);

        DynamicApplication<T> build();
    }
}
