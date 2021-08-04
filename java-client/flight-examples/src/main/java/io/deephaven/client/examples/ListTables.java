package io.deephaven.client.examples;

import io.deephaven.client.examples.util.SessionAndFlightCallable;
import io.deephaven.client.impl.Flight;
import io.deephaven.client.impl.SessionAndFlight;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.vector.types.pojo.Field;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.TimeUnit;

@Command(name = "list-tables", mixinStandardHelpOptions = true, description = "List the flights",
    version = "0.1.0")
public class ListTables extends SessionAndFlightCallable<Void> {

    @Option(names = {"-s", "--schema"}, description = "Whether to include schema",
        defaultValue = "false")
    boolean showSchema;

    @Override
    public Void callInContext(final SessionAndFlight sessionAndFlight) throws Exception {
        try (final Flight flight = sessionAndFlight.flight()) {
            for (FlightInfo flightInfo : flight.list()) {
                if (showSchema) {
                    StringBuilder sb = new StringBuilder(flightInfo.getDescriptor().toString())
                        .append(System.lineSeparator());
                    for (Field field : flightInfo.getSchema().getFields()) {
                        sb.append('\t').append(field).append(System.lineSeparator());
                    }
                    System.out.println(sb);
                } else {
                    System.out.printf("%s%n", flightInfo.getDescriptor());
                }
            }
        } finally {
            sessionAndFlight.session().closeFuture().get(5, TimeUnit.SECONDS);
        }

        return null;
    }

    public static void main(String[] args) {
        int execute = new CommandLine(new ListTables()).execute(args);
        System.exit(execute);
    }
}
