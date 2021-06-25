/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.grpc_api.console;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.ByteStringAccess;
import com.google.rpc.Code;
import io.deephaven.db.tables.Table;
import io.deephaven.db.util.ScriptSession;
import io.deephaven.grpc_api.barrage.util.BarrageSchemaUtil;
import io.deephaven.grpc_api.session.SessionState;
import io.deephaven.grpc_api.session.TicketResolverBase;
import io.deephaven.grpc_api.util.GrpcUtil;
import org.apache.arrow.flight.impl.Flight;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class ScopeTicketResolver extends TicketResolverBase {
    private static final char TICKET_PREFIX = 's';
    private static final String FLIGHT_ROUTE = "scope";

    private final GlobalSessionProvider globalSessionProvider;

    @Inject
    public ScopeTicketResolver(final GlobalSessionProvider globalSessionProvider) {
        super((byte)TICKET_PREFIX, FLIGHT_ROUTE);
        this.globalSessionProvider = globalSessionProvider;
    }

    @Override
    public Flight.FlightInfo flightInfoFor(final Flight.FlightDescriptor descriptor) {
        if (descriptor.getPathCount() < 2) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "could not compute flight info: no variable name provided");
        }
        final String varName = descriptor.getPath(1);
        final Object varObj = globalSessionProvider.getGlobalSession().getVariable(varName);
        if (varObj instanceof Table) {
            return getFlightInfo((Table) varObj, descriptor, ticketForName(varName));
        } else {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "could not compute flight info: variable '" + varName + "' is not a flight");
        }
    }

    @Override
    public void forAllFlightInfo(final Consumer<Flight.FlightInfo> visitor) {
        globalSessionProvider.getGlobalSession().getVariables().forEach((varName, varObj) -> {
            if (varObj instanceof Table) {
                visitor.accept(getFlightInfo((Table) varObj, descriptorForName(varName), ticketForName(varName)));
            }
        });
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(final SessionState session,
                                                    final ByteBuffer ticket) {
        final String varName = nameForTicket(ticket);
        //noinspection unchecked
        final T result = (T)globalSessionProvider.getGlobalSession().getVariable(varName);

        if (result == null) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "could not resolve ticket: no variable exists with name '" + varName + "'");
        }
        return session.<T>nonExport().submit(() -> result);
    }

    @Override
    public <T> SessionState.ExportObject<T> resolve(final SessionState session,
                                                    final Flight.FlightDescriptor descriptor) {
        final String scopeName = nameForDescriptor(descriptor);
        final ScriptSession gss = globalSessionProvider.getGlobalSession();
        return session.<T>nonExport().submit(() -> gss.getVariable(scopeName));
    }

    @Override
    public <T> SessionState.ExportBuilder<T> publish(final SessionState session,
                                                     final ByteBuffer ticket) {
        // We publish to the query scope after the client finishes publishing their result. We accomplish this by
        // directly depending on the result of this export builder.
        final SessionState.ExportBuilder<T> resultBuilder = session.nonExport();
        final SessionState.ExportObject<T> resultExport = resultBuilder.getExport();
        final SessionState.ExportBuilder<T> publishTask = session.nonExport();

        final String varName = nameForTicket(ticket);
        publishTask
                .requiresSerialQueue()
                .require(resultExport)
                .submit(() -> {
                    final ScriptSession gss = globalSessionProvider.getGlobalSession();
                    gss.setVariable(varName, resultExport.get());
                });

        return resultBuilder;
    }

    public static Flight.Ticket ticketForName(final String name) {
        final byte[] ticket = (TICKET_PREFIX + '/' + name).getBytes();
        return Flight.Ticket.newBuilder()
                .setTicket(ByteStringAccess.wrap(ticket))
                .build();
    }

    public static Flight.FlightDescriptor descriptorForName(final String name) {
        return Flight.FlightDescriptor.newBuilder()
                .setType(Flight.FlightDescriptor.DescriptorType.PATH)
                .addPath(FLIGHT_ROUTE)
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
        final FlatBufferBuilder builder = new FlatBufferBuilder();
        builder.finish(BarrageSchemaUtil.makeSchemaPayload(builder, table.getDefinition(), table.getAttributes()));
        return ByteStringAccess.wrap(builder.dataBuffer());
    }

    private static String nameForTicket(final Flight.Ticket ticket) {
        return nameForTicket(ticket.getTicket().asReadOnlyByteBuffer());
    }

    private static String nameForTicket(final ByteBuffer ticket) {
        if (ticket.remaining() < 3 || ticket.get(0) != TICKET_PREFIX || ticket.get(1) != '/') {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION, "ticket is not a ScopeTicketResolver ticket");
        }
        return ticket.toString().substring(2);
    }

    private static String nameForDescriptor(final Flight.FlightDescriptor descriptor) {
        if (descriptor.getType() != Flight.FlightDescriptor.DescriptorType.PATH) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION,
                    "cannot resolve descriptor: not a path");
        }
        if (descriptor.getPathCount() != 2) {
            throw GrpcUtil.statusRuntimeException(Code.FAILED_PRECONDITION,
                    "cannot resolve descriptor: unexpected path length (found: " + descriptor.getPathCount() + ", expected: 2)");
        }

        return descriptor.getPath(1);
    }
}
