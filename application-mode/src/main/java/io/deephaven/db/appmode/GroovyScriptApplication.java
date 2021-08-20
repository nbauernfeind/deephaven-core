package io.deephaven.db.appmode;

import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@Immutable
public abstract class GroovyScriptApplication implements ApplicationConfig {

    public static final String TYPE = "groovy";

    public static Builder builder() {
        return ImmutableGroovyScriptApplication.builder();
    }

    public static GroovyScriptApplication parse(Properties properties) {
        return builder()
                .id(properties.getProperty("id"))
                .name(properties.getProperty("name"))
                .addFiles(ApplicationUtil.findFilesFrom(properties))
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

        GroovyScriptApplication build();
    }
}
