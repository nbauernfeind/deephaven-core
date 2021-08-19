package io.deephaven.grpc_api.app_mode;

import io.deephaven.db.appmode.ApplicationState;
import io.deephaven.grpc_api.session.SessionState;
import io.deephaven.grpc_api.session.TicketResolverBase;
import io.deephaven.proto.backplane.grpc.Ticket;
import org.apache.arrow.flight.impl.Flight;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Singleton
public class ApplicationTicketResolver extends TicketResolverBase {
    private static final char TICKET_PREFIX = 'a';
    private static final String FLIGHT_DESCRIPTOR_ROUTE = "app";

    @Inject
    public ApplicationTicketResolver() {
        super((byte)TICKET_PREFIX, FLIGHT_DESCRIPTOR_ROUTE);
    }

    public static Ticket ticketForName(ApplicationState app, String fieldName) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void onApplicationLoad(final ApplicationState app) {

    }

    public void visitAllApplications(Consumer<ApplicationState> consumer) {

    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(@Nullable SessionState session, ByteBuffer ticket) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(@Nullable SessionState session, Flight.FlightDescriptor descriptor) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public <T> SessionState.ExportBuilder<T> publish(SessionState session, ByteBuffer ticket) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public <T> SessionState.ExportBuilder<T> publish(SessionState session, Flight.FlightDescriptor descriptor) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public SessionState.ExportObject<Flight.FlightInfo> flightInfoFor(@Nullable SessionState session, Flight.FlightDescriptor descriptor) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public String getLogNameFor(ByteBuffer ticket) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void forAllFlightInfo(@Nullable SessionState session, Consumer<Flight.FlightInfo> visitor) {
        // TODO NOCOMMIT (NATE): implement
    }
}
