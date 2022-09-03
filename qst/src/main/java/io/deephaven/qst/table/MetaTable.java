/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.qst.table;

import io.deephaven.annotations.NodeStyle;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * @see io.deephaven.api.TableOperations#getMeta()
 */
@Immutable
@NodeStyle
public abstract class MetaTable extends TableBase implements SingleParentTable {

    public static MetaTable of(TableSpec parent) {
        return ImmutableMetaTable.of(parent);
    }

    @Parameter
    public abstract TableSpec parent();

    @Override
    public final <V extends Visitor> V walk(V visitor) {
        visitor.visit(this);
        return visitor;
    }
}
