package io.deephaven.grpc_api.session;

import org.apache.arrow.flight.impl.Flight;

import java.util.function.Consumer;

public interface TicketResolver {
    /**
     * @return the single byte prefix used as a route on the ticket
     */
    byte ticketPrefix();

    /**
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
    <T> SessionState.ExportObject<T> resolve(SessionState session, Flight.Ticket ticket);

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
