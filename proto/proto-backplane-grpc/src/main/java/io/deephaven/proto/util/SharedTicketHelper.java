//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.proto.util;

import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.List;

public class SharedTicketHelper {
    public static final char TICKET_PREFIX = 'h';
    public static final String FLIGHT_DESCRIPTOR_ROUTE = "shared";

    /**
     * Convenience method to create the flight descriptor path for the given shared identifier.
     *
     * @param sharedId the shared identifier
     * @return the path
     */
    public static List<String> idToPath(byte[] sharedId) {
        return Arrays.asList(FLIGHT_DESCRIPTOR_ROUTE, Hex.encodeHexString(sharedId));
    }

    /**
     * Convenience method to create the flight ticket bytes for the given shared identifier.
     *
     * @param sharedId the shared identifier
     * @return the ticket bytes
     */
    public static byte[] idToBytes(byte[] sharedId) {
        final byte[] fullTicket = new byte[1 + sharedId.length];
        fullTicket[0] = (byte) TICKET_PREFIX;
        System.arraycopy(sharedId, 0, fullTicket, 1, sharedId.length);
        return fullTicket;
    }
}
