package io.deephaven.grpc_api.console;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.protobuf.ByteString;
import com.google.rpc.Code;
import io.deephaven.db.tables.Table;
import io.deephaven.db.v2.sources.ColumnSource;
import io.deephaven.grpc_api.barrage.util.BarrageSchemaUtil;
import io.deephaven.grpc_api.session.SessionState;
import io.deephaven.grpc_api.session.TicketResolverBase;
import io.deephaven.grpc_api.util.GrpcUtil;
import org.apache.arrow.flight.impl.Flight;

import javax.inject.Inject;
import java.util.function.Consumer;

public class ScopeTicketResolver extends TicketResolverBase {
    private static final char TICKET_PREFIX = 's';

    private final ConsoleServiceGrpcImpl consoleService;

    @Inject
    public ScopeTicketResolver(final ConsoleServiceGrpcImpl consoleService) {
        super((byte)TICKET_PREFIX, "scope");
        this.consoleService = consoleService;
    }

    @Override
    public Flight.FlightInfo flightInfoFor(final Flight.FlightDescriptor descriptor) {
        if (descriptor.getPathCount() < 2) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "could not compute flight info: no variable name provided");
        }
        final String varName = descriptor.getPath(1);
        final Object varObj = consoleService.getGlobalSession().getVariable(varName);
        if (varObj instanceof Table) {
            return getFlightInfo((Table) varObj, descriptor, ticketForName(varName));
        } else {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "could not compute flight info: variable '" + varName + "' is not a flight");
        }
    }

    @Override
    public void forAllFlightInfo(final Consumer<Flight.FlightInfo> visitor) {
        consoleService.getGlobalSession().getVariables().forEach((varName, varObj) -> {
            if (varObj instanceof Table) {
                visitor.accept(getFlightInfo((Table) varObj, descriptorForName(varName), ticketForName(varName)));
            }
        });
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(
            final SessionState session,
            final Flight.Ticket ticket) {
        // Skip the initial byte which is used to route the ticket to this resolver.
        // Skip the second byte which is a '/' separating route from varName
        final String varName = ticket.getTicket().toStringUtf8().substring(2);
        //noinspection unchecked
        final T result = (T)consoleService.getGlobalSession().getVariable(varName);

        if (result == null) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "could not resolve ticket: no variable exists with name '" + varName + "'");
        }
        return session.<T>nonExport().submit(() -> result);
    }

    private Flight.FlightDescriptor descriptorForName(final String name) {
        return Flight.FlightDescriptor.newBuilder()
                .setType(Flight.FlightDescriptor.DescriptorType.PATH)
                .addPath(flightDescriptorRoute())
                .addPath(name)
                .build();
    }

    private static Flight.FlightInfo getFlightInfo(final Table table,
                                                   final Flight.FlightDescriptor descriptor,
                                                   final Flight.Ticket ticket) {
        return Flight.FlightInfo.newBuilder()
                .setSchema(schemaBytesFromTable(table))
                .setFlightDescriptor(descriptor)
                .addEndpoint(Flight.FlightEndpoint.newBuilder()
                        .setTicket(ticket)
                        .build())
                .setTotalRecords(table.isLive() ? -1 : table.size())
                .setTotalBytes(-1)
                .build();
    }

    private static ByteString schemaBytesFromTable(final Table table) {
        final String[] columnNames = table.getDefinition().getColumnNamesArray();
        final ColumnSource<?>[] columnSources = table.getColumnSources().toArray(ColumnSource.ZERO_LENGTH_COLUMN_SOURCE_ARRAY);
        final FlatBufferBuilder builder = new FlatBufferBuilder();
        builder.finish(BarrageSchemaUtil.makeSchemaPayload(builder, columnNames, columnSources, table.getAttributes()));
        return ByteString.copyFrom(builder.dataBuffer());
    }

    private static Flight.Ticket ticketForName(final String name) {
        final byte[] ticket = (TICKET_PREFIX + '/' + name).getBytes();
        return Flight.Ticket.newBuilder()
                .setTicket(ByteString.copyFrom(ticket))
                .build();
    }
}
