/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.engine.table.impl;

import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.rowset.WritableRowSet;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.testutil.TstUtils;
import io.deephaven.time.DateTimeUtils;
import io.deephaven.time.DateTime;
import io.deephaven.engine.util.TableTools;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.deephaven.engine.util.TableTools.*;
import static io.deephaven.engine.testutil.TstUtils.assertTableEquals;
import static io.deephaven.engine.testutil.TstUtils.i;

public class TestBlinkTableTools {
    @Before
    public void setUp() throws Exception {
        ExecutionContext.getContext().getUpdateGraph().enableUnitTestMode();
        ExecutionContext.getContext().getUpdateGraph().resetForUnitTests(false);
    }

    @After
    public void tearDown() throws Exception {
        ExecutionContext.getContext().getUpdateGraph().resetForUnitTests(true);
    }

    @Test
    public void testBlinkToAppendOnlyTable() {
        final DateTime dt1 = DateTimeUtils.convertDateTime("2021-08-11T8:20:00 NY");
        final DateTime dt2 = DateTimeUtils.convertDateTime("2021-08-11T8:21:00 NY");
        final DateTime dt3 = DateTimeUtils.convertDateTime("2021-08-11T11:22:00 NY");

        final QueryTable blinkTable = TstUtils.testRefreshingTable(i(1).toTracking(), intCol("I", 7),
                doubleCol("D", Double.NEGATIVE_INFINITY), dateTimeCol("DT", dt1), col("B", Boolean.TRUE));
        blinkTable.setAttribute(Table.BLINK_TABLE_ATTRIBUTE, true);

        final Table appendOnly = BlinkTableTools.blinkToAppendOnly(blinkTable);

        assertTableEquals(blinkTable, appendOnly);
        TestCase.assertEquals(true, appendOnly.getAttribute(Table.ADD_ONLY_TABLE_ATTRIBUTE));
        TestCase.assertTrue(appendOnly.isFlat());

        ExecutionContext.getContext().getUpdateGraph().runWithinUnitTestCycle(() -> {
            RowSet removed = blinkTable.getRowSet().copyPrev();
            ((WritableRowSet) blinkTable.getRowSet()).clear();
            TstUtils.addToTable(blinkTable, i(7), intCol("I", 1), doubleCol("D", Math.PI), dateTimeCol("DT", dt2),
                    col("B", true));
            blinkTable.notifyListeners(i(7), removed, i());
        });

        assertTableEquals(TableTools.newTable(intCol("I", 7, 1), doubleCol("D", Double.NEGATIVE_INFINITY, Math.PI),
                dateTimeCol("DT", dt1, dt2), col("B", true, true)), appendOnly);

        ExecutionContext.getContext().getUpdateGraph().runWithinUnitTestCycle(() -> {
            RowSet removed = blinkTable.getRowSet().copyPrev();
            ((WritableRowSet) blinkTable.getRowSet()).clear();
            TstUtils.addToTable(blinkTable, i(7), intCol("I", 2), doubleCol("D", Math.E), dateTimeCol("DT", dt3),
                    col("B", false));
            blinkTable.notifyListeners(i(7), removed, i());
        });
        assertTableEquals(
                TableTools.newTable(intCol("I", 7, 1, 2), doubleCol("D", Double.NEGATIVE_INFINITY, Math.PI, Math.E),
                        dateTimeCol("DT", dt1, dt2, dt3), col("B", true, true, false)),
                appendOnly);
    }

}
