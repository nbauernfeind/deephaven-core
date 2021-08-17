package io.deephaven.db.appmode;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@Immutable
public abstract class QSTApplication implements ApplicationConfig {

    public static final String TYPE = "qst";

    public static Builder builder() {
        return ImmutableQSTApplication.builder();
    }

    public static QSTApplication parse(Properties properties) {
        return builder()
                .id(properties.getProperty("id"))
                .name(properties.getProperty("name"))
                .addFiles(ApplicationConfig.splitFilePropertyIntoPaths(properties.getProperty("file")))
                .build();
    }

    @Parameter
    public abstract String id();

    @Parameter
    public abstract String name();

    @Parameter
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

        QSTApplication build();
    }
}
