/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.qst.table;

import io.deephaven.annotations.NodeStyle;
import io.deephaven.api.TableOperations;
import org.immutables.value.Value.Immutable;

import java.util.Collection;

/**
 * @see TableOperations#view(Collection)
 */
@Immutable
@NodeStyle
public abstract class ViewTable extends TableBase implements SelectableTable {

    public static Builder builder() {
        return ImmutableViewTable.builder();
    }

    @Override
    public final <T> T walk(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public interface Builder extends SelectableTable.Builder<ViewTable, Builder> {

    }
}
