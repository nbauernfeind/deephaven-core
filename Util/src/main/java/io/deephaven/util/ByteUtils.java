/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.util;

public class ByteUtils {
    private static final char[] HEX_LOOKUP = new char[] {
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};

    public static String byteArrToHex(byte[] bytes) {
        // our output size will be exactly 2x byte-array length
        final char[] buffer = new char[bytes.length * 2];

        for (int ii = 0; ii < bytes.length; ii++) {
            // extract the upper 4 bits
            buffer[ii << 1] = HEX_LOOKUP[(bytes[ii] >> 4) & 0xF];
            // extract the lower 4 bits
            buffer[(ii << 1) + 1] = HEX_LOOKUP[bytes[ii] & 0xF];
        }

        return new String(buffer);
    }
}
