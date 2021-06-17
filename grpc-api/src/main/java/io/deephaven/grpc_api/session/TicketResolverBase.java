package io.deephaven.grpc_api.session;

public abstract class TicketResolverBase implements TicketResolver {
    private final byte ticketPrefix;
    private final String flightDescriptorRoute;

    public TicketResolverBase(final byte ticketPrefix, final String flightDescriptorRoute) {
        this.ticketPrefix = ticketPrefix;
        this.flightDescriptorRoute = flightDescriptorRoute;
    }

    @Override
    public byte ticketPrefix() {
        return ticketPrefix;
    }

    @Override
    public String flightDescriptorRoute() {
        return flightDescriptorRoute;
    }
}
