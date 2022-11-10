/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.server.auth;

import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import io.deephaven.auth.ServiceAuthWiring;
import io.deephaven.auth.codegen.impl.ApplicationServiceAuthWiring;
import io.deephaven.auth.codegen.impl.BrowserFlightServiceAuthWiring;
import io.deephaven.auth.codegen.impl.ConfigServiceAuthWiring;
import io.deephaven.auth.codegen.impl.ConsoleServiceAuthWiring;
import io.deephaven.auth.codegen.impl.FlightServiceAuthWiring;
import io.deephaven.auth.codegen.impl.InputTableServiceAuthWiring;
import io.deephaven.auth.codegen.impl.InputTableServiceContextualAuthWiring;
import io.deephaven.auth.codegen.impl.ObjectServiceAuthWiring;
import io.deephaven.auth.codegen.impl.PartitionedTableServiceAuthWiring;
import io.deephaven.auth.codegen.impl.SessionServiceAuthWiring;
import io.deephaven.auth.codegen.impl.StorageServiceAuthWiring;
import io.deephaven.auth.codegen.impl.TableServiceAuthWiring;
import io.deephaven.auth.codegen.impl.TableServiceContextualAuthWiring;
import io.deephaven.flightjs.protocol.BrowserFlightServiceGrpc;
import io.deephaven.proto.backplane.grpc.ApplicationServiceGrpc;
import io.deephaven.proto.backplane.grpc.ConfigServiceGrpc;
import io.deephaven.proto.backplane.grpc.InputTableServiceGrpc;
import io.deephaven.proto.backplane.grpc.ObjectServiceGrpc;
import io.deephaven.proto.backplane.grpc.PartitionedTableServiceGrpc;
import io.deephaven.proto.backplane.grpc.SessionServiceGrpc;
import io.deephaven.proto.backplane.grpc.StorageServiceGrpc;
import io.deephaven.proto.backplane.grpc.TableServiceGrpc;
import io.deephaven.proto.backplane.script.grpc.ConsoleServiceGrpc;
import io.deephaven.server.session.TicketResolverBase;
import org.apache.arrow.flight.impl.FlightServiceGrpc;

import javax.inject.Singleton;

@MapKey
@interface ServiceGrpcKey {
    String value();
}

@Module
public class CommunityAuthenticationModule {
    @Provides
    @IntoMap
    @ServiceGrpcKey(ApplicationServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindApplicationServiceAuthWiring() {
        return new ApplicationServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(ConfigServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindConfigServiceAuthWiring() {
        return new ConfigServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(ConsoleServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindConsoleServiceAuthWiring() {
        return new ConsoleServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(InputTableServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindInputTableServiceAuthWiring() {
        return new InputTableServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(ObjectServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindObjectServiceAuthWiring() {
        return new ObjectServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(TableServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindTableServiceAuthWiring() {
        return new TableServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(PartitionedTableServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindPartitionedTableServiceAuthWiring() {
        return new PartitionedTableServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(SessionServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindSessionServiceAuthWiring() {
        return new SessionServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(StorageServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindStorageServiceAuthWiring() {
        return new StorageServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(FlightServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindFlightServiceAuthWiring() {
        return new FlightServiceAuthWiring.AllowAll();
    }

    @Provides
    @IntoMap
    @ServiceGrpcKey(BrowserFlightServiceGrpc.SERVICE_NAME)
    ServiceAuthWiring bindBrowserFlightServiceAuthWiring() {
        return new BrowserFlightServiceAuthWiring.AllowAll();
    }


    @Provides
    @Singleton
    public TableServiceContextualAuthWiring bindTableServiceContextualAuthWiring() {
        return new TableServiceContextualAuthWiring.AllowAll();
    }

    @Provides
    @Singleton
    public InputTableServiceContextualAuthWiring bindInputTableServiceContextualAuthWiring() {
        return new InputTableServiceContextualAuthWiring.AllowAll();
    }

    @Provides
    @Singleton
    public TicketResolverBase.AuthTableTransformation bindAuthTableTransformation() {
        // in community this is an identity transformation
        return (table) -> table;
    }
}
