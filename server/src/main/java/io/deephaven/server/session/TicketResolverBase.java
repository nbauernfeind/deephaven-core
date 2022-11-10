/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.server.session;

import io.deephaven.engine.table.Table;

public abstract class TicketResolverBase implements TicketResolver {

    @FunctionalInterface
    public interface AuthTableTransformation {
       Table transform(Table sourceTable);
    }

    protected final AuthTableTransformation authTableTransformation;
    private final byte ticketPrefix;
    private final String flightDescriptorRoute;

    public TicketResolverBase(
            final AuthTableTransformation authTableTransformation,
            final byte ticketPrefix, final String flightDescriptorRoute) {
        this.authTableTransformation = authTableTransformation;
        this.ticketPrefix = ticketPrefix;
        this.flightDescriptorRoute = flightDescriptorRoute;
    }

    @Override
    public byte ticketRoute() {
        return ticketPrefix;
    }

    @Override
    public String flightDescriptorRoute() {
        return flightDescriptorRoute;
    }
}
