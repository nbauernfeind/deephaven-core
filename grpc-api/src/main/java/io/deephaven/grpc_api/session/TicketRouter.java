package io.deephaven.grpc_api.session;

import com.google.rpc.Code;
import io.deephaven.grpc_api.util.GrpcUtil;
import io.deephaven.hash.KeyedIntObjectHashMap;
import io.deephaven.hash.KeyedIntObjectKey;
import io.deephaven.hash.KeyedObjectHashMap;
import io.deephaven.hash.KeyedObjectKey;
import org.apache.arrow.flight.impl.Flight;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class TicketRouter {

    private final KeyedIntObjectHashMap<TicketResolver> byteResolverMap = new KeyedIntObjectHashMap<>(RESOLVER_OBJECT_TICKET_ID);
    private final KeyedObjectHashMap<String, TicketResolver> descriptorResolverMap = new KeyedObjectHashMap<>(RESOLVER_OBJECT_DESCRIPTOR_ID);

    @Inject
    public TicketRouter(final Set<TicketResolver> resolvers) {
        resolvers.forEach(resolver -> {
            byteResolverMap.add(resolver);
            descriptorResolverMap.add(resolver);
        });
    }

    /**
     * Resolve a flight ticket to an export object future.
     *
     * @param session the user session context
     * @param ticket the ticket to resolve
     * @param <T> the expected return type of the ticket; this is not validated
     * @return an export object that can be used as a dependency; it is not guaranteed to be readily available
     */
    public <T> SessionState.ExportObject<T> resolve(
            final SessionState session,
            final Flight.Ticket ticket) {
        final byte route = ticket.getTicket().byteAt(0);
        final TicketResolver resolver = byteResolverMap.get(route);
        if (resolver == null) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION,
                    "cannot resolve ticket: no router for '" + route + "' (byte)");
        }
        return resolver.resolve(session, ticket);
    }

    public Flight.FlightInfo flightInfoFor(final Flight.FlightDescriptor descriptor) {
        return getResolver(descriptor).flightInfoFor(descriptor);
    }

    private TicketResolver getResolver(final Flight.FlightDescriptor descriptor) {
        if (descriptor.getType() != Flight.FlightDescriptor.DescriptorType.PATH) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION,
                    "cannot resolve descriptor: not a path");
        }
        if (descriptor.getPathCount() <= 0) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION,
                    "cannot resolve descriptor: path not long enough");
        }

        final String route = descriptor.getPath(0);
        final TicketResolver resolver = descriptorResolverMap.get(route);
        if (resolver == null) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION,
                    "cannot resolve ticket: no router for '" + route + "'");
        }

        return resolver;
    }

    public void visitFlightInfo(final Consumer<Flight.FlightInfo> visitor) {
        byteResolverMap.forEach((route, resolver) ->
                resolver.forAllFlightInfo(visitor)
        );
    }

    private static final KeyedIntObjectKey<TicketResolver> RESOLVER_OBJECT_TICKET_ID = new KeyedIntObjectKey.BasicStrict<TicketResolver>() {
        @Override
        public int getIntKey(final TicketResolver ticketResolver) {
            return ticketResolver.ticketPrefix();
        }
    };

    private static final KeyedObjectKey<String, TicketResolver> RESOLVER_OBJECT_DESCRIPTOR_ID = new KeyedObjectKey.Basic<String, TicketResolver>() {
        @Override
        public String getKey(TicketResolver ticketResolver) {
            return ticketResolver.flightDescriptorRoute();
        }
    };
}
