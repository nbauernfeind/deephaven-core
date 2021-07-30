package io.deephaven.grpc_api.appmode;

import io.deephaven.annotations.SimpleStyle;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Immutable
@SimpleStyle
public abstract class ApplicationQST implements ApplicationConfig {

    public static final String TYPE = "qst";

    public static ApplicationQST of(List<Path> files) {
        return ImmutableApplicationQST.of(files);
    }

    public static ApplicationQST parse(Properties properties) {
        return of(Arrays.stream(properties.getProperty("file").split(";")).map(Paths::get).collect(Collectors.toList()));
    }

    @Parameter
    public abstract List<Path> files();

    @Override
    public final <V extends Visitor> V walk(V visitor) {
        visitor.visit(this);
        return visitor;
    }
}
