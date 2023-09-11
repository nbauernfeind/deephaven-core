/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.parquet.table.transfer;

import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.table.ColumnDefinition;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.impl.CodecLookup;
import io.deephaven.engine.util.BigDecimalUtils;
import io.deephaven.parquet.table.*;
import io.deephaven.util.SafeCloseable;
import io.deephaven.util.codec.BigDecimalCodec;
import io.deephaven.util.codec.ObjectCodec;
import org.apache.parquet.column.ColumnWriter;
import org.apache.parquet.column.statistics.Statistics;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Classes that implement this interface are responsible for converting data from individual DH columns into buffers
 * to be written out to the Parquet file.
 *
 * @param <B>
 */
public interface TransferObject<B> extends SafeCloseable {
    /**
     * Create an appropriate {@link TransferObject} to copy data from upstream {@link ColumnSource}s into appropriate buffer
     * format for a Parquet {@link ColumnWriter}
     */
    static <DATA_TYPE> TransferObject<?> create(
            @NotNull final Map<String, Map<ParquetCacheTags, Object>> computedCache,
            @NotNull final RowSet tableRowSet,
            @NotNull final ColumnSource<DATA_TYPE> columnSource,
            @NotNull final ColumnDefinition<DATA_TYPE> columnDefinition,
            final int maxValuesPerPage,
            @NotNull final Class<DATA_TYPE> columnType,
            @NotNull final ParquetInstructions instructions) {
        if (int.class.equals(columnType)) {
            return new IntTransfer(columnSource, maxValuesPerPage);
        } else if (long.class.equals(columnType)) {
            return new LongTransfer(columnSource, maxValuesPerPage);
        } else if (double.class.equals(columnType)) {
            return new DoubleTransfer(columnSource, maxValuesPerPage);
        } else if (float.class.equals(columnType)) {
            return new FloatTransfer(columnSource, maxValuesPerPage);
        } else if (Boolean.class.equals(columnType)) {
            return new BooleanTransfer(columnSource, maxValuesPerPage);
        } else if (short.class.equals(columnType)) {
            return new ShortTransfer(columnSource, maxValuesPerPage);
        } else if (char.class.equals(columnType)) {
            return new CharTransfer(columnSource, maxValuesPerPage);
        } else if (byte.class.equals(columnType)) {
            return new ByteTransfer(columnSource, maxValuesPerPage);
        } else if (String.class.equals(columnType)) {
            return new StringTransfer(columnSource, maxValuesPerPage);
        }

        // If there's an explicit codec, we should disregard the defaults for these CodecLookup#lookup() will properly
        // select the codec assigned by the instructions so we only need to check and redirect once.
        if (!CodecLookup.explicitCodecPresent(instructions.getCodecName(columnDefinition.getName()))) {
            if (BigDecimal.class.equals(columnType)) {
                // noinspection unchecked
                final ColumnSource<BigDecimal> bigDecimalColumnSource = (ColumnSource<BigDecimal>) columnSource;
                final BigDecimalUtils.PrecisionAndScale precisionAndScale = TypeInfos.getPrecisionAndScale(
                        computedCache, columnDefinition.getName(), tableRowSet, () -> bigDecimalColumnSource);
                final ObjectCodec<BigDecimal> codec = precisionAndScale.scale == 0
                        ? new BigDecimalCodec(precisionAndScale.precision, precisionAndScale.scale, false)
                        : new BigDecimalParquetBytesCodec(precisionAndScale.precision, precisionAndScale.scale,  -1);
                return new CodecTransfer<>(bigDecimalColumnSource, codec, maxValuesPerPage);
            } else if (BigInteger.class.equals(columnType)) {
                //noinspection unchecked
                return new CodecTransfer<>((ColumnSource<BigInteger>)columnSource,
                        new BigIntegerParquetBytesCodec(-1), maxValuesPerPage);
            }
        }

        final ObjectCodec<? super DATA_TYPE> codec = CodecLookup.lookup(columnDefinition, instructions);
        return new CodecTransfer<>(columnSource, codec, maxValuesPerPage);
    }

    /**
     * Get the buffer suitable for writing to a Parquet file
     * @return the buffer
     */
    B getBuffer();

    /**
     * Get the number of rows contained within the current transfer set to be written
     * @return the number of rows that are ready to write to the Parquet file
     */
    int rowCount();

    /**
     * Copy the data from the upstream {@link ColumnSource} into this object in preparation for writing
     * @param rs the {@link RowSequence} to copy
     */
    void fetchData(@NotNull final RowSequence rs);

    /**
     * After a sequence of calls to {@link #fetchData(RowSequence)} this may be called to append statistic information
     * into the Parquet footer for readers.
     */
    <T extends Comparable<T>> void updateStatistics(@NotNull final Statistics<T> stats);
}
