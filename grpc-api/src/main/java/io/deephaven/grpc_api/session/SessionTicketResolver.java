package io.deephaven.grpc_api.session;

import com.google.rpc.Code;
import io.deephaven.grpc_api.util.GrpcUtil;
import org.apache.arrow.flight.impl.Flight;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class SessionTicketResolver extends TicketResolverBase {

    @Inject
    public SessionTicketResolver() {
        super((byte) 0, "export");
    }

    @Override
    public Flight.FlightInfo flightInfoFor(Flight.FlightDescriptor descriptor) {
        // sessions do not participate in resolving flight descriptors
        throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "no such flight exists");
    }

    @Override
    public void forAllFlightInfo(Consumer<Flight.FlightInfo> visitor) {
        // sessions do not expose tickets via list flights
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(final SessionState session,
                                                    final Flight.Ticket ticket) {
        return session.getExport(ticket);
    }
}
