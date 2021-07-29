package io.deephaven.appmode;

import io.deephaven.annotations.BuildableStyle;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Immutable
@BuildableStyle
public abstract class ApplicationGroovyScript implements ApplicationConfig {

    public static final String TYPE = "groovy";

    public static Builder builder() {
        return ImmutableApplicationGroovyScript.builder();
    }

    public static ApplicationGroovyScript parse(Properties properties) {
        return builder()
                .id(properties.getProperty("id"))
                .name(properties.getProperty("name"))
                .addFiles(Arrays.stream(properties.getProperty("file").split(";")).map(Paths::get).toArray(Path[]::new))
                .build();
    }

    public abstract String id();

    public abstract String name();

    public abstract List<Path> files();

    @Override
    public final <V extends Visitor> V walk(V visitor) {
        visitor.visit(this);
        return visitor;
    }

    public interface Builder {

        Builder id(String id);

        Builder name(String name);

        Builder addFiles(Path... files);

        ApplicationGroovyScript build();
    }
}
