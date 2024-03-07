//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.api;

import io.deephaven.annotations.BuildableStyle;
import io.deephaven.api.expression.Expression;
import io.deephaven.api.filter.Filter;
import io.deephaven.api.filter.FilterNot;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import java.util.Optional;
import java.util.Set;


/**
 * An un-parsed string; used for cases where the server has string-parsing that hasn't been structurally represented at
 * the api layer yet.
 */
@Immutable
@BuildableStyle
public abstract class RawString implements Expression, Filter {

    public static RawString of(String x) {
        return ImmutableRawString.of(x);
    }

    public static Builder builder() {
        return ImmutableRawString.builder();
    }

    public interface Builder extends BuilderBase<Builder> {
        RawString build();
    }

    @Parameter
    public abstract String value();

    @Default
    public boolean serial() {
        return false;
    }

    public abstract Set<String> synchronizeOn();

    public abstract Set<String> respectBarrier();

    public abstract Optional<String> barrier();

    @Override
    public final FilterNot<RawString> invert() {
        return Filter.not(this);
    }

    @Override
    public final <T> T walk(Expression.Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public final <T> T walk(Filter.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
