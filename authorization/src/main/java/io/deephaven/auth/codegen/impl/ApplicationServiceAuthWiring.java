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

public interface ApplicationServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionListFields(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ListFieldsRequest request);

    class AllowAll implements ApplicationServiceAuthWiring {

        public void checkPermissionListFields(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ListFieldsRequest request) {}
    }

    class DenyAll implements ApplicationServiceAuthWiring {

        public void checkPermissionListFields(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ListFieldsRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
