package io.deephaven.engine.table.impl.updateby;

import io.deephaven.api.ColumnName;
import io.deephaven.api.updateby.UpdateByControl;
import io.deephaven.api.updateby.UpdateByOperation;
import io.deephaven.base.verify.Assert;
import io.deephaven.engine.context.QueryScope;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.impl.QueryTable;
import io.deephaven.engine.testutil.EvalNugget;
import io.deephaven.engine.testutil.GenerateTableUpdates;
import io.deephaven.engine.testutil.TstUtils;
import io.deephaven.engine.testutil.generator.CharGenerator;
import io.deephaven.engine.testutil.generator.SortedDateTimeGenerator;
import io.deephaven.engine.testutil.generator.TestDataGenerator;
import io.deephaven.engine.updategraph.UpdateContext;
import io.deephaven.engine.util.TableDiff;
import io.deephaven.test.types.OutOfBandTest;
import io.deephaven.vector.ObjectVector;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import static io.deephaven.engine.testutil.GenerateTableUpdates.generateAppends;
import static io.deephaven.engine.testutil.testcase.RefreshingTableTestCase.simulateShiftAwareStep;
import static io.deephaven.function.Basic.isNull;
import static io.deephaven.time.DateTimeUtils.convertDateTime;

@Category(OutOfBandTest.class)
public class TestRollingStd extends BaseUpdateByTest {
    /**
     * These are used in the static tests and leverage the Numeric class functions for verification. Additional tests
     * are performed on BigInteger/BigDecimal columns as well.
     */
    final String[] primitiveColumns = new String[] {
            "charCol",
            "byteCol",
            "shortCol",
            "intCol",
            "longCol",
            "floatCol",
            "doubleCol",
    };

    /**
     * These are used in the ticking table evaluations where we verify dynamic vs static tables.
     */
    final String[] columns = new String[] {
            "charCol",
            "byteCol",
            "shortCol",
            "intCol",
            "longCol",
            "floatCol",
            "doubleCol",
            "bigIntCol",
            "bigDecimalCol",
    };

    final int STATIC_TABLE_SIZE = 10_000;
    final int DYNAMIC_TABLE_SIZE = 1_000;
    final int DYNAMIC_UPDATE_SIZE = 100;
    final int DYNAMIC_UPDATE_STEPS = 20;

    private String[] getFormulas(String[] columns) {
        return Arrays.stream(columns)
                // Force null instead of NaN when vector size == 0
                .map(c -> String.format("%s=%s.size() == 0 ? null : std(%s)", c, c, c))
                .toArray(String[]::new);
    }

    // For verification, we will upcast some columns and use already-defined Numeric class functions.
    private String[] getCastingFormulas(String[] columns) {
        return Arrays.stream(columns)
                .map(c -> c.equals("charCol")
                        ? String.format("%s=(short)%s", c, c)
                        : null)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    // region Object Helper functions

    final Function<ObjectVector<BigInteger>, BigDecimal> stdBigInt = bigIntegerObjectVector -> {
        MathContext mathContextDefault = UpdateByControl.mathContextDefault();

        if (bigIntegerObjectVector == null || bigIntegerObjectVector.size() == 0) {
            return null;
        }

        BigDecimal sum = new BigDecimal(0);
        BigDecimal sumSquares = new BigDecimal(0);
        long count = 0;

        final long n = bigIntegerObjectVector.size();

        for (long i = 0; i < n; i++) {
            BigInteger val = bigIntegerObjectVector.get(i);
            if (!isNull(val)) {
                final BigDecimal decVal = new BigDecimal(val);
                final BigDecimal decValSquare = decVal.multiply(decVal, mathContextDefault);

                sum = sum.add(decVal, mathContextDefault);
                sumSquares = sumSquares.add(decValSquare, mathContextDefault);
                count++;
            }
        }
        if (count <= 1) {
            return null;
        }

        // complex calc for variance and std
        final BigDecimal biCount = BigDecimal.valueOf(count);
        final BigDecimal biCountMinusOne = BigDecimal.valueOf(count - 1);

        final BigDecimal variance = sumSquares.divide(biCountMinusOne, mathContextDefault)
                .subtract(sum.multiply(sum, mathContextDefault).divide(biCount, mathContextDefault)
                        .divide(biCountMinusOne, mathContextDefault));

        return variance.sqrt(mathContextDefault);
    };

    final Function<ObjectVector<BigDecimal>, BigDecimal> stdBigDec = bigDecimalObjectVector -> {
        MathContext mathContextDefault = UpdateByControl.mathContextDefault();

        if (bigDecimalObjectVector == null || bigDecimalObjectVector.size() == 0) {
            return null;
        }

        BigDecimal sum = new BigDecimal(0);
        BigDecimal sumSquares = new BigDecimal(0);
        long count = 0;

        final long n = bigDecimalObjectVector.size();

        for (long i = 0; i < n; i++) {
            BigDecimal decVal = bigDecimalObjectVector.get(i);
            if (!isNull(decVal)) {
                final BigDecimal decValSquare = decVal.multiply(decVal, mathContextDefault);

                sum = sum.add(decVal, mathContextDefault);
                sumSquares = sumSquares.add(decValSquare, mathContextDefault);
                count++;
            }
        }
        if (count <= 1) {
            return null;
        }

        // complex calc for variance and std
        final BigDecimal biCount = BigDecimal.valueOf(count);
        final BigDecimal biCountMinusOne = BigDecimal.valueOf(count - 1);

        final BigDecimal variance = sumSquares.divide(biCountMinusOne, mathContextDefault)
                .subtract(sum.multiply(sum, mathContextDefault).divide(biCount, mathContextDefault)
                        .divide(biCountMinusOne, mathContextDefault));

        return variance.sqrt(mathContextDefault);
    };

    private void doTestStaticZeroKeyBigNumbers(final QueryTable t, final int prevTicks, final int postTicks) {
        QueryScope.addParam("stdBigInt", stdBigInt);
        QueryScope.addParam("stdBigDec", stdBigDec);

        Table actual = t.updateBy(UpdateByOperation.RollingStd(prevTicks, postTicks, "bigIntCol", "bigDecimalCol"));
        Table expected = t.updateBy(UpdateByOperation.RollingGroup(prevTicks, postTicks, "bigIntCol", "bigDecimalCol"))
                .update("bigIntCol=stdBigInt.apply(bigIntCol)", "bigDecimalCol=stdBigDec.apply(bigDecimalCol)");

        BigDecimal[] biActual = (BigDecimal[]) actual.getColumn("bigIntCol").getDirect();
        Object[] biExpected = (Object[]) expected.getColumn("bigIntCol").getDirect();

        Assert.eq(biActual.length, "array length", biExpected.length);
        for (int ii = 0; ii < biActual.length; ii++) {
            BigDecimal actualVal = biActual[ii];
            BigDecimal expectedVal = (BigDecimal) biExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }

        BigDecimal[] bdActual = (BigDecimal[]) actual.getColumn("bigDecimalCol").getDirect();
        Object[] bdExpected = (Object[]) expected.getColumn("bigDecimalCol").getDirect();

        Assert.eq(bdActual.length, "array length", bdExpected.length);
        for (int ii = 0; ii < bdActual.length; ii++) {
            BigDecimal actualVal = bdActual[ii];
            BigDecimal expectedVal = (BigDecimal) bdExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }
    }

    private void doTestStaticZeroKeyTimedBigNumbers(final QueryTable t, final Duration prevTime,
            final Duration postTime) {
        QueryScope.addParam("stdBigInt", stdBigInt);
        QueryScope.addParam("stdBigDec", stdBigDec);

        Table actual = t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, "bigIntCol", "bigDecimalCol"));
        Table expected =
                t.updateBy(UpdateByOperation.RollingGroup("ts", prevTime, postTime, "bigIntCol", "bigDecimalCol"))
                        .update("bigIntCol=stdBigInt.apply(bigIntCol)", "bigDecimalCol=stdBigDec.apply(bigDecimalCol)");

        BigDecimal[] biActual = (BigDecimal[]) actual.getColumn("bigIntCol").getDirect();
        Object[] biExpected = (Object[]) expected.getColumn("bigIntCol").getDirect();

        Assert.eq(biActual.length, "array length", biExpected.length);
        for (int ii = 0; ii < biActual.length; ii++) {
            BigDecimal actualVal = biActual[ii];
            BigDecimal expectedVal = (BigDecimal) biExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }

        BigDecimal[] bdActual = (BigDecimal[]) actual.getColumn("bigDecimalCol").getDirect();
        Object[] bdExpected = (Object[]) expected.getColumn("bigDecimalCol").getDirect();

        Assert.eq(bdActual.length, "array length", bdExpected.length);
        for (int ii = 0; ii < bdActual.length; ii++) {
            BigDecimal actualVal = bdActual[ii];
            BigDecimal expectedVal = (BigDecimal) bdExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }
    }

    private void doTestStaticBucketedBigNumbers(final QueryTable t, final int prevTicks, final int postTicks) {
        QueryScope.addParam("stdBigInt", stdBigInt);
        QueryScope.addParam("stdBigDec", stdBigDec);

        Table actual =
                t.updateBy(UpdateByOperation.RollingStd(prevTicks, postTicks, "bigIntCol", "bigDecimalCol"), "Sym");
        Table expected =
                t.updateBy(UpdateByOperation.RollingGroup(prevTicks, postTicks, "bigIntCol", "bigDecimalCol"), "Sym")
                        .update("bigIntCol=stdBigInt.apply(bigIntCol)", "bigDecimalCol=stdBigDec.apply(bigDecimalCol)");

        BigDecimal[] biActual = (BigDecimal[]) actual.getColumn("bigIntCol").getDirect();
        Object[] biExpected = (Object[]) expected.getColumn("bigIntCol").getDirect();

        Assert.eq(biActual.length, "array length", biExpected.length);
        for (int ii = 0; ii < biActual.length; ii++) {
            BigDecimal actualVal = biActual[ii];
            BigDecimal expectedVal = (BigDecimal) biExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }

        BigDecimal[] bdActual = (BigDecimal[]) actual.getColumn("bigDecimalCol").getDirect();
        Object[] bdExpected = (Object[]) expected.getColumn("bigDecimalCol").getDirect();

        Assert.eq(bdActual.length, "array length", bdExpected.length);
        for (int ii = 0; ii < bdActual.length; ii++) {
            BigDecimal actualVal = bdActual[ii];
            BigDecimal expectedVal = (BigDecimal) bdExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }
    }

    private void doTestStaticBucketedTimedBigNumbers(final QueryTable t, final Duration prevTime,
            final Duration postTime) {
        QueryScope.addParam("stdBigInt", stdBigInt);
        QueryScope.addParam("stdBigDec", stdBigDec);

        Table actual =
                t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, "bigIntCol", "bigDecimalCol"), "Sym");
        Table expected = t
                .updateBy(UpdateByOperation.RollingGroup("ts", prevTime, postTime, "bigIntCol", "bigDecimalCol"), "Sym")
                .update("bigIntCol=stdBigInt.apply(bigIntCol)", "bigDecimalCol=stdBigDec.apply(bigDecimalCol)");

        BigDecimal[] biActual = (BigDecimal[]) actual.getColumn("bigIntCol").getDirect();
        Object[] biExpected = (Object[]) expected.getColumn("bigIntCol").getDirect();

        Assert.eq(biActual.length, "array length", biExpected.length);
        for (int ii = 0; ii < biActual.length; ii++) {
            BigDecimal actualVal = biActual[ii];
            BigDecimal expectedVal = (BigDecimal) biExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }

        BigDecimal[] bdActual = (BigDecimal[]) actual.getColumn("bigDecimalCol").getDirect();
        Object[] bdExpected = (Object[]) expected.getColumn("bigDecimalCol").getDirect();

        Assert.eq(bdActual.length, "array length", bdExpected.length);
        for (int ii = 0; ii < bdActual.length; ii++) {
            BigDecimal actualVal = bdActual[ii];
            BigDecimal expectedVal = (BigDecimal) bdExpected[ii];
            if (actualVal != null || expectedVal != null) {
                Assert.eqTrue(actualVal.compareTo(expectedVal) == 0, "values match");
            }
        }
    }
    // endregion Object Helper functions

    // region Static Zero Key Tests

    @Test
    public void testStaticZeroKeyRev() {
        final int prevTicks = 100;
        final int postTicks = 0;

        doTestStaticZeroKey(prevTicks, postTicks);
    }

    @Test
    public void testStaticZeroKeyRevExclusive() {
        final int prevTicks = 100;
        final int postTicks = -50;

        doTestStaticZeroKey(prevTicks, postTicks);
    }

    @Test
    public void testStaticZeroKeyFwd() {
        final int prevTicks = 0;
        final int postTicks = 100;

        doTestStaticZeroKey(prevTicks, postTicks);
    }

    @Test
    public void testStaticZeroKeyFwdExclusive() {
        final int prevTicks = -50;
        final int postTicks = 100;

        doTestStaticZeroKey(prevTicks, postTicks);
    }

    @Test
    public void testStaticZeroKeyFwdRevWindow() {
        final int prevTicks = 100;
        final int postTicks = 100;

        doTestStaticZeroKey(prevTicks, postTicks);
    }

    @Test
    public void testStaticZeroKeyTimedRev() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ZERO;

        doTestStaticZeroKeyTimed(prevTime, postTime);
    }

    @Test
    public void testStaticZeroKeyTimedRevExclusive() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(-5);

        doTestStaticZeroKeyTimed(prevTime, postTime);
    }

    @Test
    public void testStaticZeroKeyTimedFwd() {
        final Duration prevTime = Duration.ZERO;
        final Duration postTime = Duration.ofMinutes(10);

        doTestStaticZeroKeyTimed(prevTime, postTime);
    }

    @Test
    public void testStaticZeroKeyTimedFwdExclusive() {
        final Duration prevTime = Duration.ofMinutes(-5);
        final Duration postTime = Duration.ofMinutes(10);

        doTestStaticZeroKeyTimed(prevTime, postTime);
    }

    @Test
    public void testStaticZeroKeyTimedFwdRev() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(10);

        doTestStaticZeroKeyTimed(prevTime, postTime);
    }

    private void doTestStaticZeroKey(final int prevTicks, final int postTicks) {
        final QueryTable t = createTestTable(STATIC_TABLE_SIZE, false, false, false, 0x31313131,
                new String[] {"charCol"},
                new TestDataGenerator[] {new CharGenerator('A', 'z', 0.1)}).t;

        final Table actual = t.updateBy(UpdateByOperation.RollingStd(prevTicks, postTicks, primitiveColumns));
        final Table expected = t.update(getCastingFormulas(primitiveColumns))
                .updateBy(UpdateByOperation.RollingGroup(prevTicks, postTicks, primitiveColumns))
                .update(getFormulas(primitiveColumns));
        TstUtils.assertTableEquals(expected, actual, TableDiff.DiffItems.DoublesExact);

        doTestStaticZeroKeyBigNumbers(t, prevTicks, postTicks);
    }

    private void doTestStaticZeroKeyTimed(final Duration prevTime, final Duration postTime) {
        final QueryTable t = createTestTable(STATIC_TABLE_SIZE, false, false, false, 0xFFFABBBC,
                new String[] {"ts", "charCol"}, new TestDataGenerator[] {new SortedDateTimeGenerator(
                        convertDateTime("2022-03-09T09:00:00.000 NY"),
                        convertDateTime("2022-03-09T16:30:00.000 NY")),
                        new CharGenerator('A', 'z', 0.1)}).t;

        final Table actual = t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, primitiveColumns));
        final Table expected = t.update(getCastingFormulas(primitiveColumns))
                .updateBy(UpdateByOperation.RollingGroup("ts", prevTime, postTime, primitiveColumns))
                .update(getFormulas(primitiveColumns));
        TstUtils.assertTableEquals(expected, actual, TableDiff.DiffItems.DoublesExact);

        doTestStaticZeroKeyTimedBigNumbers(t, prevTime, postTime);
    }

    // endregion

    // region Static Bucketed Tests

    @Test
    public void testStaticGroupedBucketed() {
        final int prevTicks = 100;
        final int postTicks = 0;

        doTestStaticBucketed(true, prevTicks, postTicks);
    }

    @Test
    public void testStaticGroupedBucketedTimed() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(0);

        doTestStaticBucketedTimed(true, prevTime, postTime);
    }

    @Test
    public void testStaticBucketedRev() {
        final int prevTicks = 100;
        final int postTicks = 0;

        doTestStaticBucketed(false, prevTicks, postTicks);
    }

    @Test
    public void testStaticBucketedRevExclusive() {
        final int prevTicks = 100;
        final int postTicks = -50;

        doTestStaticBucketed(false, prevTicks, postTicks);
    }

    @Test
    public void testStaticBucketedFwd() {
        final int prevTicks = 0;
        final int postTicks = 100;

        doTestStaticBucketed(false, prevTicks, postTicks);
    }

    @Test
    public void testStaticBucketedFwdExclusive() {
        final int prevTicks = -50;
        final int postTicks = 100;

        doTestStaticBucketed(false, prevTicks, postTicks);
    }

    @Test
    public void testStaticBucketedTimedRev() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(0);

        doTestStaticBucketedTimed(false, prevTime, postTime);
    }

    @Test
    public void testStaticBucketedTimedRevExclusive() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(-5);

        doTestStaticBucketedTimed(false, prevTime, postTime);
    }

    @Test
    public void testStaticBucketedTimedFwd() {
        final Duration prevTime = Duration.ofMinutes(0);
        final Duration postTime = Duration.ofMinutes(10);

        doTestStaticBucketedTimed(false, prevTime, postTime);
    }

    @Test
    public void testStaticBucketedTimedFwdExclusive() {
        final Duration prevTime = Duration.ofMinutes(-5);
        final Duration postTime = Duration.ofMinutes(10);

        doTestStaticBucketedTimed(false, prevTime, postTime);
    }

    @Test
    public void testStaticBucketedFwdRevWindowTimed() {
        final Duration prevTime = Duration.ofMinutes(5);
        final Duration postTime = Duration.ofMinutes(5);

        doTestStaticBucketedTimed(false, prevTime, postTime);
    }

    private void doTestStaticBucketed(boolean grouped, int prevTicks, int postTicks) {
        final QueryTable t = createTestTable(STATIC_TABLE_SIZE, true, grouped, false, 0x31313131,
                new String[] {"charCol"},
                new TestDataGenerator[] {new CharGenerator('A', 'z', 0.1)}).t;

        final Table actual = t.updateBy(UpdateByOperation.RollingStd(prevTicks, postTicks, primitiveColumns));
        final Table expected = t.update(getCastingFormulas(primitiveColumns))
                .updateBy(UpdateByOperation.RollingGroup(prevTicks, postTicks, primitiveColumns))
                .update(getFormulas(primitiveColumns));
        TstUtils.assertTableEquals(expected, actual, TableDiff.DiffItems.DoublesExact);

        doTestStaticBucketedBigNumbers(t, prevTicks, postTicks);
    }

    private void doTestStaticBucketedTimed(boolean grouped, Duration prevTime, Duration postTime) {
        final QueryTable t = createTestTable(STATIC_TABLE_SIZE, true, grouped, false, 0xFFFABBBC,
                new String[] {"ts", "charCol"}, new TestDataGenerator[] {new SortedDateTimeGenerator(
                        convertDateTime("2022-03-09T09:00:00.000 NY"),
                        convertDateTime("2022-03-09T16:30:00.000 NY")),
                        new CharGenerator('A', 'z', 0.1)}).t;

        final Table actual =
                t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, primitiveColumns), "Sym");
        final Table expected =
                t.update(getCastingFormulas(primitiveColumns))
                        .updateBy(UpdateByOperation.RollingGroup("ts", prevTime, postTime, primitiveColumns), "Sym")
                        .update(getFormulas(primitiveColumns));
        TstUtils.assertTableEquals(expected, actual, TableDiff.DiffItems.DoublesExact);

        doTestStaticBucketedTimedBigNumbers(t, prevTime, postTime);
    }

    // endregion

    // region Append Only Tests

    @Test
    public void testZeroKeyAppendOnlyRev() {
        final int prevTicks = 100;
        final int postTicks = 0;

        doTestAppendOnly(false, prevTicks, postTicks);
    }

    @Test
    public void testZeroKeyAppendOnlyRevExclusive() {
        final int prevTicks = 100;
        final int postTicks = -50;

        doTestAppendOnly(false, prevTicks, postTicks);
    }

    @Test
    public void testZeroKeyAppendOnlyFwd() {
        final int prevTicks = 0;
        final int postTicks = 100;

        doTestAppendOnly(false, prevTicks, postTicks);
    }

    @Test
    public void testZeroKeyAppendOnlyFwdExclusive() {
        final int prevTicks = -50;
        final int postTicks = 100;

        doTestAppendOnly(false, prevTicks, postTicks);
    }

    @Test
    public void testZeroKeyAppendOnlyFwdRev() {
        final int prevTicks = 50;
        final int postTicks = 50;

        doTestAppendOnly(false, prevTicks, postTicks);
    }

    @Test
    public void testBucketedAppendOnlyRev() {
        final int prevTicks = 100;
        final int postTicks = 0;

        doTestAppendOnly(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedAppendOnlyRevExclusive() {
        final int prevTicks = 100;
        final int postTicks = -50;

        doTestAppendOnly(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedAppendOnlyFwd() {
        final int prevTicks = 0;
        final int postTicks = 100;

        doTestAppendOnly(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedAppendOnlyFwdExclusive() {
        final int prevTicks = -50;
        final int postTicks = 100;

        doTestAppendOnly(true, prevTicks, postTicks);
    }

    @Test
    public void testZeroKeyAppendOnlyTimedRev() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(0);

        doTestAppendOnlyTimed(false, prevTime, postTime);
    }

    @Test
    public void testZeroKeyAppendOnlyTimedRevExclusive() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(-5);

        doTestAppendOnlyTimed(false, prevTime, postTime);
    }

    @Test
    public void testZeroKeyAppendOnlyTimedFwd() {
        final Duration prevTime = Duration.ofMinutes(0);
        final Duration postTime = Duration.ofMinutes(10);

        doTestAppendOnlyTimed(false, prevTime, postTime);
    }

    @Test
    public void testZeroKeyAppendOnlyTimedFwdExclusive() {
        final Duration prevTime = Duration.ofMinutes(-5);
        final Duration postTime = Duration.ofMinutes(10);

        doTestAppendOnlyTimed(false, prevTime, postTime);
    }

    @Test
    public void testZeroKeyAppendOnlyTimedFwdRev() {
        final Duration prevTime = Duration.ofMinutes(5);
        final Duration postTime = Duration.ofMinutes(5);

        doTestAppendOnlyTimed(false, prevTime, postTime);
    }

    @Test
    public void testBucketedAppendOnlyTimedRev() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(0);

        doTestAppendOnlyTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedAppendOnlyTimedRevExclusive() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(-5);

        doTestAppendOnlyTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedAppendOnlyTimedFwd() {
        final Duration prevTime = Duration.ofMinutes(0);
        final Duration postTime = Duration.ofMinutes(10);

        doTestAppendOnlyTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedAppendOnlyTimedFwdExclusive() {
        final Duration prevTime = Duration.ofMinutes(-5);
        final Duration postTime = Duration.ofMinutes(10);

        doTestAppendOnlyTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedAppendOnlyTimedFwdRev() {
        final Duration prevTime = Duration.ofMinutes(5);
        final Duration postTime = Duration.ofMinutes(5);

        doTestAppendOnlyTimed(true, prevTime, postTime);
    }

    private void doTestAppendOnly(boolean bucketed, int prevTicks, int postTicks) {
        final CreateResult result = createTestTable(DYNAMIC_TABLE_SIZE, bucketed, false, true, 0x31313131,
                new String[] {"charCol"},
                new TestDataGenerator[] {new CharGenerator('A', 'z', 0.1)});

        final QueryTable t = result.t;
        t.setAttribute(Table.APPEND_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);

        final EvalNugget[] nuggets = new EvalNugget[] {
                EvalNugget.from(() -> bucketed
                        ? t.updateBy(UpdateByOperation.RollingStd(prevTicks, postTicks, columns), "Sym")
                        : t.updateBy(UpdateByOperation.RollingStd(prevTicks, postTicks, columns)))
        };

        final Random billy = new Random(0xB177B177);
        for (int ii = 0; ii < DYNAMIC_UPDATE_STEPS; ii++) {
            UpdateContext.updateGraphProcessor()
                    .runWithinUnitTestCycle(() -> generateAppends(DYNAMIC_UPDATE_SIZE, billy, t, result.infos));
            TstUtils.validate("Table", nuggets);
        }
    }

    private void doTestAppendOnlyTimed(boolean bucketed, Duration prevTime, Duration postTime) {
        final CreateResult result = createTestTable(DYNAMIC_TABLE_SIZE, bucketed, false, true, 0x31313131,
                new String[] {"ts", "charCol"}, new TestDataGenerator[] {new SortedDateTimeGenerator(
                        convertDateTime("2022-03-09T09:00:00.000 NY"),
                        convertDateTime("2022-03-09T16:30:00.000 NY")),
                        new CharGenerator('A', 'z', 0.1)});
        final QueryTable t = result.t;
        t.setAttribute(Table.APPEND_ONLY_TABLE_ATTRIBUTE, Boolean.TRUE);

        final EvalNugget[] nuggets = new EvalNugget[] {
                EvalNugget.from(() -> bucketed
                        ? t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, columns), "Sym")
                        : t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, columns)))
        };

        final Random billy = new Random(0xB177B177);
        for (int ii = 0; ii < DYNAMIC_UPDATE_STEPS; ii++) {
            UpdateContext.updateGraphProcessor()
                    .runWithinUnitTestCycle(() -> generateAppends(DYNAMIC_UPDATE_SIZE, billy, t, result.infos));
            TstUtils.validate("Table", nuggets);
        }
    }

    // endregion Append Only Tests

    // region General Ticking Tests

    @Test
    public void testZeroKeyGeneralTickingRev() {
        final long prevTicks = 100;
        final long fwdTicks = 0;

        doTestTicking(false, prevTicks, fwdTicks);
    }

    @Test
    public void testZeroKeyGeneralTickingRevExclusive() {
        final long prevTicks = 100;
        final long fwdTicks = -50;

        doTestTicking(false, prevTicks, fwdTicks);
    }

    @Test
    public void testZeroKeyGeneralTickingFwd() {
        final long prevTicks = 0;
        final long fwdTicks = 100;

        doTestTicking(false, prevTicks, fwdTicks);
    }

    @Test
    public void testZeroKeyGeneralTickingFwdExclusive() {
        final long prevTicks = -50;
        final long fwdTicks = 100;

        doTestTicking(false, prevTicks, fwdTicks);
    }

    @Test
    public void testBucketedGeneralTickingRev() {
        final int prevTicks = 100;
        final int postTicks = 0;

        doTestTicking(false, prevTicks, postTicks);
    }

    @Test
    public void testBucketedGeneralTickingRevExclusive() {
        final int prevTicks = 100;
        final int postTicks = -50;

        doTestTicking(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedGeneralTickingFwd() {
        final int prevTicks = 0;
        final int postTicks = 100;

        doTestTicking(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedGeneralTickingFwdExclusive() {
        final int prevTicks = -50;
        final int postTicks = 100;

        doTestTicking(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedGeneralTickingFwdRev() {
        final int prevTicks = 50;
        final int postTicks = 50;

        doTestTicking(true, prevTicks, postTicks);
    }

    @Test
    public void testBucketedGeneralTickingTimedRev() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(0);

        doTestTickingTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedGeneralTickingTimedRevExclusive() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(-5);

        doTestTickingTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedGeneralTickingTimedFwd() {
        final Duration prevTime = Duration.ofMinutes(0);
        final Duration postTime = Duration.ofMinutes(10);

        doTestTickingTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedGeneralTickingTimedFwdExclusive() {
        final Duration prevTime = Duration.ofMinutes(-5);
        final Duration postTime = Duration.ofMinutes(10);

        doTestTickingTimed(true, prevTime, postTime);
    }

    @Test
    public void testBucketedGeneralTickingTimedFwdRev() {
        final Duration prevTime = Duration.ofMinutes(5);
        final Duration postTime = Duration.ofMinutes(5);

        doTestTickingTimed(true, prevTime, postTime);
    }

    private void doTestTicking(final boolean bucketed, final long prevTicks, final long fwdTicks) {
        final CreateResult result = createTestTable(DYNAMIC_TABLE_SIZE, bucketed, false, true, 0x31313131,
                new String[] {"charCol"},
                new TestDataGenerator[] {new CharGenerator('A', 'z', 0.1)});
        final QueryTable t = result.t;

        final EvalNugget[] nuggets = new EvalNugget[] {
                EvalNugget.from(() -> bucketed ? t.updateBy(
                        UpdateByOperation.RollingStd(prevTicks, fwdTicks, columns), "Sym")
                        : t.updateBy(UpdateByOperation.RollingStd(prevTicks, fwdTicks, columns)))
        };


        final Random billy = new Random(0xB177B177);
        for (int ii = 0; ii < DYNAMIC_UPDATE_STEPS; ii++) {
            UpdateContext.updateGraphProcessor().runWithinUnitTestCycle(
                    () -> GenerateTableUpdates.generateTableUpdates(DYNAMIC_UPDATE_SIZE, billy, t, result.infos));
            TstUtils.validate("Table - step " + ii, nuggets);
        }
    }

    private void doTestTickingTimed(final boolean bucketed, final Duration prevTime, final Duration postTime) {
        final CreateResult result = createTestTable(DYNAMIC_TABLE_SIZE, bucketed, false, true, 0x31313131,
                new String[] {"ts", "charCol"}, new TestDataGenerator[] {new SortedDateTimeGenerator(
                        convertDateTime("2022-03-09T09:00:00.000 NY"),
                        convertDateTime("2022-03-09T16:30:00.000 NY")),
                        new CharGenerator('A', 'z', 0.1)});
        final QueryTable t = result.t;

        final EvalNugget[] nuggets = new EvalNugget[] {
                EvalNugget.from(() -> bucketed ? t.updateBy(
                        UpdateByOperation.RollingStd("ts", prevTime, postTime, columns), "Sym")
                        : t.updateBy(UpdateByOperation.RollingStd("ts", prevTime, postTime, columns)))
        };


        final Random billy = new Random(0xB177B177);
        for (int ii = 0; ii < DYNAMIC_UPDATE_STEPS; ii++) {
            UpdateContext.updateGraphProcessor().runWithinUnitTestCycle(
                    () -> GenerateTableUpdates.generateTableUpdates(DYNAMIC_UPDATE_SIZE, billy, t, result.infos));
            TstUtils.validate("Table - step " + ii, nuggets);
        }
    }

    @Test
    public void testBucketedGeneralTickingRevRedirected() {
        final int prevTicks = 100;
        final int postTicks = 0;

        final CreateResult result = createTestTable(DYNAMIC_TABLE_SIZE, true, false, true, 0x31313131,
                new String[] {"charCol"},
                new TestDataGenerator[] {new CharGenerator('A', 'z', 0.1)});
        final QueryTable t = result.t;

        final UpdateByControl control = UpdateByControl.builder().useRedirection(true).build();

        final EvalNugget[] nuggets = new EvalNugget[] {
                new EvalNugget() {
                    @Override
                    protected Table e() {
                        return t.updateBy(control,
                                List.of(UpdateByOperation.RollingStd(prevTicks, postTicks, columns)),
                                ColumnName.from("Sym"));
                    }
                }
        };

        final Random billy = new Random(0xB177B177);
        for (int ii = 0; ii < DYNAMIC_UPDATE_STEPS; ii++) {
            try {
                simulateShiftAwareStep(DYNAMIC_UPDATE_SIZE, billy, t, result.infos, nuggets);
            } catch (Throwable ex) {
                System.out.println("Crapped out on step " + ii);
                throw ex;
            }
        }
    }

    @Test
    public void testBucketedGeneralTickingTimedRevRedirected() {
        final Duration prevTime = Duration.ofMinutes(10);
        final Duration postTime = Duration.ofMinutes(0);

        final CreateResult result = createTestTable(DYNAMIC_TABLE_SIZE, true, false, true, 0x31313131,
                new String[] {"ts", "charCol"}, new TestDataGenerator[] {new SortedDateTimeGenerator(
                        convertDateTime("2022-03-09T09:00:00.000 NY"),
                        convertDateTime("2022-03-09T16:30:00.000 NY")),
                        new CharGenerator('A', 'z', 0.1)});

        final QueryTable t = result.t;

        final UpdateByControl control = UpdateByControl.builder().useRedirection(true).build();

        final EvalNugget[] nuggets = new EvalNugget[] {
                new EvalNugget() {
                    @Override
                    protected Table e() {
                        return t.updateBy(control,
                                List.of(UpdateByOperation.RollingStd("ts", prevTime, postTime, columns)),
                                ColumnName.from("Sym"));
                    }
                }
        };

        final Random billy = new Random(0xB177B177);
        for (int ii = 0; ii < DYNAMIC_UPDATE_STEPS; ii++) {
            try {
                simulateShiftAwareStep(DYNAMIC_UPDATE_SIZE, billy, t, result.infos, nuggets);
            } catch (Throwable ex) {
                System.out.println("Crapped out on step " + ii);
                throw ex;
            }
        }
    }

    // endregion
}
