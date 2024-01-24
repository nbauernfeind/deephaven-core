/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.engine.table.impl.select;

import io.deephaven.api.ColumnName;
import io.deephaven.api.RawString;
import io.deephaven.api.Selectable;
import io.deephaven.api.Strings;
import io.deephaven.api.expression.Expression;
import io.deephaven.api.expression.Function;
import io.deephaven.api.expression.Method;
import io.deephaven.api.filter.Filter;
import io.deephaven.api.literal.Literal;
import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.context.QueryCompiler;
import io.deephaven.engine.rowset.TrackingRowSet;
import io.deephaven.engine.table.ColumnDefinition;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.impl.MatchPair;
import io.deephaven.engine.table.WritableColumnSource;
import io.deephaven.engine.table.impl.BaseTable;
import io.deephaven.util.annotations.FinalDefault;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The interface for a query table to perform retrieve values from a column for select like operations.
 */
public interface SelectColumn extends Selectable {

    static SelectColumn of(Selectable selectable) {
        return (selectable instanceof SelectColumn)
                ? (SelectColumn) selectable
                : selectable.expression().walk(new ExpressionAdapter(selectable.newColumn()));
    }

    static SelectColumn[] from(Selectable... selectables) {
        return from(Arrays.asList(selectables));
    }

    static SelectColumn[] from(Collection<? extends Selectable> selectables) {
        return selectables.stream().map(SelectColumn::of).toArray(SelectColumn[]::new);
    }

    static SelectColumn[] copyFrom(SelectColumn[] selectColumns) {
        return Arrays.stream(selectColumns).map(SelectColumn::copy).toArray(SelectColumn[]::new);
    }

    static Collection<SelectColumn> copyFrom(Collection<SelectColumn> selectColumns) {
        return selectColumns.stream().map(SelectColumn::copy).collect(Collectors.toList());
    }

    /**
     * Convenient static final instance of a zero length Array of SelectColumns for use in toArray calls.
     */
    SelectColumn[] ZERO_LENGTH_SELECT_COLUMN_ARRAY = new SelectColumn[0];

    /**
     * Initialize the column from the provided set of underlying columns and row set.
     *
     * @param rowSet the base row set
     * @param columnsOfInterest the input columns
     *
     * @return a list of columns on which the result of this is dependent
     */
    List<String> initInputs(TrackingRowSet rowSet, Map<String, ? extends ColumnSource<?>> columnsOfInterest);

    /**
     * Initialize any internal column definitions from the provided initial. Any formulae will be compiled immediately
     * using the {@link QueryCompiler} in the current {@link ExecutionContext}.
     *
     * @param columnDefinitionMap the starting set of column definitions; valid for this call only
     *
     * @return a list of columns on which the result of this is dependent
     * @apiNote Any {@link io.deephaven.engine.context.QueryLibrary}, {@link io.deephaven.engine.context.QueryScope}, or
     *          {@link QueryCompiler} usage needs to be resolved within initDef. Implementations must be idempotent.
     *          Implementations that want to hold on to the {@code columnDefinitionMap} must make a defensive copy.
     */
    @FinalDefault
    default List<String> initDef(@NotNull Map<String, ColumnDefinition<?>> columnDefinitionMap) {
        return initDef(columnDefinitionMap, request -> {
            ExecutionContext.getContext().getQueryCompiler().compile(request);
        });
    }

    /**
     * Initialize any internal column definitions from the provided initial. A compilation request consumer is provided
     * to allow for deferred compilation of expressions that belong to the same query.
     *
     * @param columnDefinitionMap the starting set of column definitions; valid for this call only
     * @param compilationRequestProcessor a consumer to submit compilation requests; valid for this call only
     *
     * @return a list of columns on which the result of this is dependent
     * @apiNote Any {@link io.deephaven.engine.context.QueryLibrary}, {@link io.deephaven.engine.context.QueryScope}, or
     *          {@link QueryCompiler} usage needs to be resolved within initDef. Implementations must be idempotent.
     *          Implementations that want to hold on to the {@code columnDefinitionMap} must make a defensive copy.
     */
    List<String> initDef(
            @NotNull Map<String, ColumnDefinition<?>> columnDefinitionMap,
            @NotNull QueryCompiler.RequestProcessor compilationRequestProcessor);

    /**
     * Get the data type stored in the resultant column.
     *
     * @return the type
     */
    Class<?> getReturnedType();

    /**
     * Get a list of the names of columns used in this SelectColumn. Behavior is undefined if none of the init* methods
     * have been called yet.
     *
     * @return the columns used in this SelectColumn
     */
    List<String> getColumns();

    /**
     * Get a list of the names of column arrays used in this SelectColumn. Behavior is undefined if none of the init*
     * methods have been called yet.
     *
     * @return the list of column arrays used
     */
    List<String> getColumnArrays();

    /**
     * Get a {@link ColumnSource} that can be used to access the data on demand.
     *
     * @return a {@link ColumnSource}
     */
    @NotNull
    ColumnSource<?> getDataView();

    /**
     * Returns a lazily computed view of this column.
     *
     * @return a lazily computed column source
     */
    @NotNull
    ColumnSource<?> getLazyView();

    /**
     * Get the name of the resultant column.
     *
     * @return the name of the column
     */
    String getName();

    /**
     * Get a MatchPair for this column, if applicable.
     *
     * @return
     */
    MatchPair getMatchPair();

    /**
     * Create a new {@link WritableColumnSource}.
     *
     * The returned column source must be capable of handling updates.
     *
     * @param size A hint as to the number of rows that will be used
     *
     * @return a new {@link WritableColumnSource}
     */
    WritableColumnSource<?> newDestInstance(long size);

    /**
     * Create a new {@link io.deephaven.engine.table.ColumnSource#isImmutable immutable} {@link WritableColumnSource}.
     *
     * The returned column source should be flat, and need not handle updates.
     *
     * @param size A hint as to the number of rows that will be used
     *
     * @return a new {@link WritableColumnSource}
     */
    WritableColumnSource<?> newFlatDestInstance(long size);

    /**
     *
     * @return
     */
    boolean isRetain();

    /**
     * Validate that this {@code SelectColumn} is safe to use in the context of the provided sourceTable.
     *
     * @param sourceTable the source table
     */
    default void validateSafeForRefresh(final BaseTable<?> sourceTable) {
        // nothing to validate by default
    }

    /**
     * Returns true if this column is stateless (i.e. one row does not depend on the order of evaluation for another
     * row).
     */
    boolean isStateless();

    /**
     * Create a copy of this SelectColumn.
     *
     * @return an independent copy of this SelectColumn.
     */
    SelectColumn copy();

    class ExpressionAdapter implements Expression.Visitor<SelectColumn> {
        private final ColumnName lhs;

        ExpressionAdapter(ColumnName lhs) {
            this.lhs = Objects.requireNonNull(lhs);
        }

        @Override
        public SelectColumn visit(ColumnName rhs) {
            return new SourceColumn(rhs.name(), lhs.name());
        }

        @Override
        public SelectColumn visit(Literal rhs) {
            return makeSelectColumn(Strings.of(rhs));
        }

        @Override
        public SelectColumn visit(Filter rhs) {
            return makeSelectColumn(Strings.of(rhs));
        }

        @Override
        public SelectColumn visit(Function rhs) {
            return makeSelectColumn(Strings.of(rhs));
        }

        @Override
        public SelectColumn visit(Method rhs) {
            return makeSelectColumn(Strings.of(rhs));
        }

        @Override
        public SelectColumn visit(RawString rhs) {
            return makeSelectColumn(Strings.of(rhs));
        }

        private SelectColumn makeSelectColumn(String rhs) {
            // TODO(deephaven-core#3740): Remove engine crutch on io.deephaven.api.Strings
            return SelectColumnFactory.getExpression(String.format("%s=%s", lhs.name(), rhs));
        }
    }

    // region Selectable impl

    @Override
    default ColumnName newColumn() {
        return ColumnName.of(getName());
    }

    @Override
    default Expression expression() {
        throw new UnsupportedOperationException("SelectColumns do not implement expression");
    }

    // endregion Selectable impl
}
