//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl.select;

import io.deephaven.api.RawString;
import io.deephaven.api.expression.Expression;
import io.deephaven.base.Pair;
import io.deephaven.engine.table.impl.MatchPair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface FormulaColumn extends SelectColumn {

    static FormulaColumn createFormulaColumn(
            @NotNull final String columnName,
            @NotNull final Expression expression,
            @NotNull final FormulaParserConfiguration parser) {
        switch (parser) {
            case Deephaven:
                return new DhFormulaColumn(columnName, expression);
            case Numba:
                throw new UnsupportedOperationException("Python formula columns must be created from python");
            default:
                throw new UnsupportedOperationException("Parser support not implemented for " + parser);
        }
    }

    static FormulaColumn createFormulaColumn(
            @NotNull final String columnName,
            @NotNull final String formulaString,
            @NotNull final FormulaParserConfiguration parser) {
        return createFormulaColumn(columnName, RawString.of(formulaString), parser);
    }

    static FormulaColumn createFormulaColumn(
            @NotNull final String columnName,
            @NotNull final String formulaString) {
        return createFormulaColumn(columnName, formulaString, FormulaParserConfiguration.parser);
    }

    static FormulaColumn createFormulaColumn(
            @NotNull final String columnName,
            @NotNull final Expression expression) {
        return createFormulaColumn(columnName, expression, FormulaParserConfiguration.parser);
    }

    /**
     * @return true if all rows have a single constant value
     */
    default boolean hasConstantValue() {
        return false;
    }

    /**
     * Returns true if the formula expression of the column has Array Access that conforms to "i +/- &lt;constant&gt;"
     * or "ii +/- &lt;constant&gt;".
     *
     * @return true or false
     */
    default boolean hasConstantArrayAccess() {
        return getFormulaShiftColPair() != null;
    }

    /**
     * Returns a Pair object consisting of formula string and shift to column MatchPairs. If the column formula or
     * expression has Array Access that conforms to "i +/- &lt;constant&gt;" or "ii +/- &lt;constant&gt;". If there is a
     * parsing error for the expression null is returned.
     *
     * @return Pair of final formula string and shift to column MatchPairs.
     */
    default Pair<String, Map<Long, List<MatchPair>>> getFormulaShiftColPair() {
        return null;
    }
}
