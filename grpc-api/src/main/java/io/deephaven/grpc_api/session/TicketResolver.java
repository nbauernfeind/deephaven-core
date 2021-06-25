/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.grpc_api.session;

import org.apache.arrow.flight.impl.Flight;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface TicketResolver {
    /**
     * @return the single byte prefix used as a route on the ticket
     */
    byte ticketRoute();

    /**
     * The first path entry on a route indicates which resolver to use. The remaining path elements are used to resolve
     * the descriptor.
     *
     * @return the string that will route from flight descriptor to this resolver
     */
    String flightDescriptorRoute();

    /**
     * Resolve a flight ticket to an export object future.
     *
     * @param session the user session context
     * @param ticket the ticket to resolve
     * @param <T> the expected return type of the ticket; this is not validated
     * @return an export object that can be used as a dependency; it is not guaranteed to be readily available
     */
    default <T> SessionState.ExportObject<T> resolve(SessionState session, Flight.Ticket ticket) {
        return resolve(session, ticket.getTicket().asReadOnlyByteBuffer());
    }

    /**
     * Resolve a flight ticket to an export object future.
     *
     * @param session the user session context
     * @param ticket (as ByteByffer) the ticket to resolve
     * @param <T> the expected return type of the ticket; this is not validated
     * @return an export object that can be used as a dependency; it is not guaranteed to be readily available
     */
    <T> SessionState.ExportObject<T> resolve(SessionState session, ByteBuffer ticket);

    /**
     * Resolve a flight descriptor to an export object future.
     *
     * @param session the user session context
     * @param descriptor the descriptor to resolve
     * @param <T> the expected return type of the ticket; this is not validated
     * @return an export object that can be used as a dependency; it is not guaranteed to be readily available
     */
    <T> SessionState.ExportObject<T> resolve(SessionState session, Flight.FlightDescriptor descriptor);

    /**
     * Publish a new result as a flight ticket to an export object future.
     *
     * The user must call {@link SessionState.ExportBuilder#submit} to publish the result value.
     *
     * @param session the user session context
     * @param ticket (as ByteByffer) the ticket to publish to
     * @param <T> the type of the result the export will publish
     * @return an export object that can be used as a dependency; it is not guaranteed to be readily available
     */
    <T> SessionState.ExportBuilder<T> publish(SessionState session, ByteBuffer ticket);

    /**
     * Retrieve a FlightInfo for a given FlightDescriptor.
     *
     * @param descriptor the flight descriptor to retrieve a ticket for
     * @return a FlightInfo describing this flight
     */
    Flight.FlightInfo flightInfoFor(Flight.FlightDescriptor descriptor);

    /**
     * This invokes the provided visitor for each valid flight descriptor this ticket resolver exposes via flight.
     *
     * @param visitor the callback to invoke per descriptor path
     */
    void forAllFlightInfo(Consumer<Flight.FlightInfo> visitor);
}
