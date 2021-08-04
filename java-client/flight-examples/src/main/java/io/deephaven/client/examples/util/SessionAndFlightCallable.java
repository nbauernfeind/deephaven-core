package io.deephaven.client.examples.util;

import io.deephaven.client.DaggerSessionImplComponent;
import io.deephaven.client.impl.DaggerFlightComponent;
import io.deephaven.client.impl.FlightClientModule;
import io.deephaven.client.impl.SessionAndFlight;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class SessionAndFlightCallable<T> implements Callable<T> {

    @Option(names = {"-t", "--target"}, description = "The host target.",
            defaultValue = "localhost:10000")
    String target;

    protected abstract T callInContext(SessionAndFlight sessionAndFlight) throws Exception;

    @Override
    public T call() throws Exception {
        BufferAllocator bufferAllocator = new RootAllocator();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
        ManagedChannel managedChannel =
                ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> onShutdown(scheduler, managedChannel)));

        // todo: fix dual-graph Dagger
        SessionAndFlight sessionAndFlight = DaggerFlightComponent.factory().create(
                new FlightClientModule(
                        DaggerSessionImplComponent.factory().create(managedChannel, scheduler).session()),
                managedChannel, scheduler, bufferAllocator).sessionAndFlight();

        T retVal = callInContext(sessionAndFlight);

        scheduler.shutdownNow();
        managedChannel.shutdownNow();
        return retVal;
    }

    private static void onShutdown(ScheduledExecutorService scheduler,
                                   ManagedChannel managedChannel) {
        scheduler.shutdownNow();
        managedChannel.shutdownNow();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Scheduler not shutdown after 10 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            if (!managedChannel.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Channel not shutdown after 10 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
