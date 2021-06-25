/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.grpc_api.session;

import com.google.protobuf.ByteStringAccess;
import com.google.rpc.Code;
import io.deephaven.grpc_api.util.GrpcUtil;
import org.apache.arrow.flight.impl.Flight;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

@Singleton
public class SessionTicketResolver extends TicketResolverBase {
    public static final String FLIGHT_DESCRIPTOR_ROUTE = "export";

    @Inject
    public SessionTicketResolver() {
        super((byte) 'e', FLIGHT_DESCRIPTOR_ROUTE);
    }

    @Override
    public Flight.FlightInfo flightInfoFor(final Flight.FlightDescriptor descriptor) {
        // sessions do not participate in resolving flight descriptors
        throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "no such flight exists");
    }

    @Override
    public void forAllFlightInfo(final Consumer<Flight.FlightInfo> visitor) {
        // sessions do not expose tickets via list flights
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(final SessionState session, final ByteBuffer ticket) {
        return session.getExport(ticketToExportId(ticket));
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(final SessionState session, final Flight.FlightDescriptor descriptor) {
        return session.getExport(descriptorToExportId(descriptor));
    }

    @Override
    public <T> SessionState.ExportBuilder<T> publish(final SessionState session, final ByteBuffer ticket) {
        return session.newExport(ticketToExportId(ticket));
    }

    /**
     * Convenience method to convert from export id to {@link Flight.Ticket}.
     *
     * @param exportId the export id
     * @return a grpc Ticket wrapping the export id
     */
    public static Flight.Ticket exportIdToTicket(int exportId) {
        final byte[] dest = exportIdToBytes(exportId);
        return Flight.Ticket.newBuilder().setTicket(ByteStringAccess.wrap(dest, 0, dest.length)).build();
    }

    /**
     * Convenience method to convert from export id to {@link Flight.FlightDescriptor}.
     *
     * @param exportId the export id
     * @return a grpc Ticket wrapping the export id
     */
    public static Flight.FlightDescriptor exportIdToDescriptor(int exportId) {
        return Flight.FlightDescriptor.newBuilder()
                .setType(Flight.FlightDescriptor.DescriptorType.PATH)
                .addPath(SessionTicketResolver.FLIGHT_DESCRIPTOR_ROUTE)
                .addPath(Integer.toString(exportId))
                .build();
    }

    /**
     * Convenience method to convert from {@link Flight.Ticket} to export id.
     *
     * Ticket's byte[0] must be 'e', bytes[1-4] are a signed int export id in little-endian.
     *
     * @param ticket the grpc Ticket
     * @return the export id that the Ticket wraps
     */
    public static int ticketToExportId(final Flight.Ticket ticket) {
        return ticketToExportId(ticket.getTicket().asReadOnlyByteBuffer());
    }

    /**
     * Convenience method to convert from {@link ByteBuffer} to export id.
     *
     * Ticket's byte[0] must be 'e', bytes[1-4] are a signed int export id in little-endian.
     *
     * @param ticket the grpc Ticket
     * @return the export id that the Ticket wraps
     */
    public static int ticketToExportId(final ByteBuffer ticket) {
        if (ticket == null) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "ticket not supplied");
        }
        ticket.order(ByteOrder.LITTLE_ENDIAN);
        if (ticket.remaining() != 5 || ticket.get() != 'e') {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "ticket is not an export id");
        }

        return ticket.getInt();
    }

    /**
     * Convenience method to convert from {@link Flight.FlightDescriptor} to export id.
     *
     * Descriptor must be a path.
     *
     * @param descriptor the grpc Ticket
     * @return the export id that the Ticket wraps
     */
    public static int descriptorToExportId(final Flight.FlightDescriptor descriptor) {
        if (descriptor == null) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "ticket not supplied");
        }
        if (descriptor.getType() != Flight.FlightDescriptor.DescriptorType.PATH) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "do not support CMD Flight Descriptors");
        }
        if (descriptor.getPathCount() != 2) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "flight not found");
        }

        try {
            return Integer.parseInt(descriptor.getPath(1));
        } catch (final NumberFormatException nfe) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "flight not found: " + nfe.getMessage());
        }
    }

    /**
     * Convenience method to convert from a Flight.Ticket to a Flight.FlightDescriptor.
     *
     * @param ticket the ticket to convert
     * @return a flight descriptor that represents the ticket
     */
    public static Flight.FlightDescriptor ticketToDescriptor(final Flight.Ticket ticket) {
        return exportIdToDescriptor(ticketToExportId(ticket));
    }

    /**
     * Convenience method to convert from a Flight.Descriptor to a Flight.Ticket.
     *
     * @param descriptor the descriptor to convert
     * @return a flight ticket that represents the descriptor
     */
    public static Flight.Ticket descriptorToTicket(final Flight.FlightDescriptor descriptor) {
        return exportIdToTicket(descriptorToExportId(descriptor));
    }

    private static byte[] exportIdToBytes(int exportId) {
        final byte[] dest = new byte[5];
        dest[0] = 'e';
        dest[1] = (byte) exportId;
        dest[2] = (byte) (exportId >>> 8);
        dest[3] = (byte) (exportId >>> 16);
        dest[4] = (byte) (exportId >>> 24);
        return dest;
    }
}
