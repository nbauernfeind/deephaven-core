package io.deephaven.client.examples;

import io.deephaven.client.examples.util.SessionAndFlightCallable;
import io.deephaven.client.impl.Export;
import io.deephaven.client.impl.Flight;
import io.deephaven.client.impl.SessionAndFlight;
import io.deephaven.qst.table.TableSpec;
import org.apache.arrow.vector.types.pojo.Schema;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Command(name = "get-schema", mixinStandardHelpOptions = true,
    description = "Get the schema of a QST", version = "0.1.0")
public class GetSchema extends SessionAndFlightCallable<Void> {

    @Parameters(arity = "1", paramLabel = "QST", description = "QST file to send.",
        converter = TableConverter.class)
    TableSpec table;

    @Override
    public Void callInContext(final SessionAndFlight sessionAndFlight) throws Exception {
        final long start = System.nanoTime();
        final long end;
        try (final Flight flight = sessionAndFlight.flight();
            final Export export = sessionAndFlight.session().export(table)) {
            Schema schema = flight.getSchema(export);
            end = System.nanoTime();
            System.out.println(schema);
        } finally {
            sessionAndFlight.session().closeFuture().get(5, TimeUnit.SECONDS);
        }
        System.out.printf("%s duration%n", Duration.ofNanos(end - start));

        return null;
    }

    public static void main(String[] args) {
        int execute = new CommandLine(new GetSchema()).execute(args);
        System.exit(execute);
    }
}
