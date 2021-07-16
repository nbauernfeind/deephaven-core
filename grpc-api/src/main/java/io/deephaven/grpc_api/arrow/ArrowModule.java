package io.deephaven.grpc_api.arrow;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.deephaven.UncheckedDeephavenException;
import io.deephaven.barrage.flatbuf.BarrageSubscriptionRequest;
import io.deephaven.grpc_api.barrage.BarrageMessageProducer;
import io.deephaven.grpc_api.barrage.BarrageStreamGenerator;
import io.deephaven.grpc_api_client.barrage.chunk.ChunkInputStreamGenerator;
import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

@Module
public interface ArrowModule {
    @Binds @IntoSet
    BindableService bindFlightServiceBinding(FlightServiceGrpcBinding service);

    @Binds @Singleton
    public abstract BarrageMessageProducer.StreamGenerator.Factory<ChunkInputStreamGenerator.Options, BarrageStreamGenerator.View> bindStreamGenerator(BarrageStreamGenerator.Factory factory);

    @Provides
    public static BarrageMessageProducer.Adapter<StreamObserver<InputStream>, StreamObserver<BarrageStreamGenerator.View>> provideListenerAdapter() {
        return delegate -> new StreamObserver<BarrageStreamGenerator.View>() {
            @Override
            public void onNext(final BarrageStreamGenerator.View view) {
                try {
                    view.forEachStream(delegate::onNext);
                } catch (final IOException ioe) {
                    throw new UncheckedDeephavenException(ioe);
                }
            }

            @Override
            public void onError(Throwable t) {
                delegate.onError(t);
            }

            @Override
            public void onCompleted() {
                delegate.onCompleted();
            }
        };
    }

    @Provides
    public static BarrageMessageProducer.Adapter<BarrageSubscriptionRequest, ChunkInputStreamGenerator.Options> optionsAdapter() {
        return subscriptionRequest -> new ChunkInputStreamGenerator.Options.Builder()
                .setIsViewport(subscriptionRequest.viewportVector() != null)
                .setUseDeephavenNulls(subscriptionRequest.useDeephavenNulls())
                .build();
    }
}
