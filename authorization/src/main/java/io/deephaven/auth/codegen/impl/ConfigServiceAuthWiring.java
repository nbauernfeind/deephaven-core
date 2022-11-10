/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
/**
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit GenerateServiceAuthWiring and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.auth.codegen.impl;

import com.google.rpc.Code;
import io.deephaven.auth.AuthContext;
import io.deephaven.auth.ServiceAuthWiring;
import io.deephaven.proto.util.Exceptions;

public interface ConfigServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionGetAuthenticationConstants(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.AuthenticationConstantsRequest request);

    void checkPermissionGetConfigurationConstants(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ConfigurationConstantsRequest request);

    class AllowAll implements ConfigServiceAuthWiring {

        public void checkPermissionGetAuthenticationConstants(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AuthenticationConstantsRequest request) {}

        public void checkPermissionGetConfigurationConstants(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ConfigurationConstantsRequest request) {}
    }

    class DenyAll implements ConfigServiceAuthWiring {

        public void checkPermissionGetAuthenticationConstants(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AuthenticationConstantsRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionGetConfigurationConstants(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ConfigurationConstantsRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
