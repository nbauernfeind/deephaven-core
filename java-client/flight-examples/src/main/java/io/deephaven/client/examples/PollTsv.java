package io.deephaven.client.examples;

import io.deephaven.client.impl.SessionAndFlight;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;

@Command(name = "poll-tsv", mixinStandardHelpOptions = true,
    description = "Send a QST, poll the results, and convert to TSV", version = "0.1.0")
public class PollTsv extends GetTsv {

    @Option(names = {"-i", "--interval"}, description = "The interval.", defaultValue = "PT1s")
    Duration interval;

    @Option(names = {"-c", "--count"}, description = "The number of polls.")
    Long count;

    @Override
    public Void callInContext(final SessionAndFlight sessionAndFlight) throws Exception {
        long times = count == null ? Long.MAX_VALUE : count;

        for (long i = 0; i < times; ++i) {
            super.callInContext(sessionAndFlight);

            if (i + 1 < times) {
                Thread.sleep(interval.toMillis());
            }
        }

        return null;
    }

    public static void main(String[] args) {
        int execute = new CommandLine(new PollTsv()).execute(args);
        System.exit(execute);
    }
}
