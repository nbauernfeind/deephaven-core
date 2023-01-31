/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.engine.table;

import io.deephaven.util.QueryConstants;
import org.jetbrains.annotations.Nullable;

/**
 * A source of element data within a table.
 * 
 * @param <T> the type of underlying data.
 */
public interface ElementSource<T> {

    /**
     * Get the value from the source. This may return boxed values for basic types.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the value at the rowKey, potentially null.
     */
    @Nullable
    T get(long rowKey);

    /**
     * Get the value at the rowKey as a Boolean.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the boolean at the rowKey, potentially null.
     */
    @Nullable
    Boolean getBoolean(long rowKey);

    /**
     * Get the value at the rowKey as a byte.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the boolean at the rowKey, null values are represented by {@link QueryConstants#NULL_BYTE}
     */
    byte getByte(long rowKey);

    /**
     * Get the value at the rowKey as a char.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the char at the rowKey, null values are represented by {@link QueryConstants#NULL_CHAR}
     */
    char getChar(long rowKey);

    /**
     * Get the value at the rowKey as a double.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the double at the rowKey, null values are represented by {@link QueryConstants#NULL_DOUBLE}
     */
    double getDouble(long rowKey);

    /**
     * Get the value at the rowKey as a float.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the float at the rowKey, null values are represented by {@link QueryConstants#NULL_FLOAT}
     */
    float getFloat(long rowKey);

    /**
     * Get the value at the rowKey as an int.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the int at the rowKey, null values are represented by {@link QueryConstants#NULL_INT}
     */
    int getInt(long rowKey);

    /**
     * Get the value at the rowKey as a long.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the long at the rowKey, null values are represented by {@link QueryConstants#NULL_LONG}
     */
    long getLong(long rowKey);

    /**
     * Get the value at the rowKey as a short.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the short at the rowKey, null values are represented by {@link QueryConstants#NULL_SHORT}
     */
    short getShort(long rowKey);

    /**
     * Get the previous value at the rowKey. Previous values are used during an
     * {@link io.deephaven.engine.updategraph.UpdateGraphProcessor UGP}
     * {@link io.deephaven.engine.updategraph.LogicalClock.State#Updating update} cycle to process changes in data.
     * During {@link io.deephaven.engine.updategraph.LogicalClock.State#Idle normal} operation previous values will be
     * identical to {@link #get(long) current} values.
     *
     * @param rowKey the location in key space to get the value from.
     * @return the previous value at the rowKey, or null.
     */
    @Nullable
    T getPrev(long rowKey);

    /**
     * Get the previous value at the rowKey as a Boolean. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous boolean at the rowKey, or null.
     */
    @Nullable
    Boolean getPrevBoolean(long rowKey);

    /**
     * Get the previous value at the rowKey as a byte. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous boolean at the rowKey, null values are represented by {@link QueryConstants#NULL_BYTE}
     */
    byte getPrevByte(long rowKey);

    /**
     * Get the previous value at the rowKey as a char. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey ohe location in key space to get the previous value from.
     * @return the previous char at the rowKey, null values are represented by {@link QueryConstants#NULL_CHAR}
     */
    char getPrevChar(long rowKey);

    /**
     * Get the previous value at the rowKey as a double. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous double at the rowKey, null values are represented by {@link QueryConstants#NULL_DOUBLE}
     */
    double getPrevDouble(long rowKey);

    /**
     * Get the previous value at the rowKey as a float. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous float at the rowKey, null values are represented by {@link QueryConstants#NULL_FLOAT}
     */
    float getPrevFloat(long rowKey);

    /**
     * Get the previous value at the rowKey as an int. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous int at the rowKey, null values are represented by {@link QueryConstants#NULL_INT}
     */
    int getPrevInt(long rowKey);

    /**
     * Get the previous value at the rowKey as a long. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous long at the rowKey, null values are represented by {@link QueryConstants#NULL_LONG}
     */
    long getPrevLong(long rowKey);

    /**
     * Get the previous value at the rowKey as a short. See {@link #getPrev(long)} for more details.
     *
     * @param rowKey the location in key space to get the previous value from.
     * @return the previous short at the rowKey, null values are represented by {@link QueryConstants#NULL_SHORT}
     */
    short getPrevShort(long rowKey);
}
