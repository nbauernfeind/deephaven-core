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

public interface InputTableServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionAddTableToInputTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.AddTableRequest request);

    void checkPermissionDeleteTableFromInputTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.DeleteTableRequest request);

    class AllowAll implements InputTableServiceAuthWiring {

        public void checkPermissionAddTableToInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AddTableRequest request) {}

        public void checkPermissionDeleteTableFromInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DeleteTableRequest request) {}
    }

    class DenyAll implements InputTableServiceAuthWiring {

        public void checkPermissionAddTableToInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AddTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDeleteTableFromInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DeleteTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
