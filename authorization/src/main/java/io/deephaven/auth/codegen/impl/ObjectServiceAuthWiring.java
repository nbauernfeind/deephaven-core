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

public interface ObjectServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionFetchObject(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FetchObjectRequest request);

    class AllowAll implements ObjectServiceAuthWiring {

        public void checkPermissionFetchObject(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchObjectRequest request) {}
    }

    class DenyAll implements ObjectServiceAuthWiring {

        public void checkPermissionFetchObject(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchObjectRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
