package io.deephaven.db.appmode;

import org.immutables.value.Value.Immutable;

import java.util.Properties;

@Immutable(builder = false)
public abstract class QSTApplication implements ApplicationConfig {

    public static final String TYPE = "qst";

    public static QSTApplication parse(Properties properties) {
        return of();
    }

    public static ImmutableQSTApplication of() {
        return ImmutableQSTApplication.of();
    }

    // Note: QST structure undecided

    @Override
    public final <V extends Visitor> V walk(V visitor) {
        visitor.visit(this);
        return visitor;
    }
}
