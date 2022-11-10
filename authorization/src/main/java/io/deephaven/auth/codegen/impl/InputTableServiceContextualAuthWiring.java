/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
/**
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit GenerateContextualAuthWiring and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.auth.codegen.impl;

import com.google.rpc.Code;
import io.deephaven.auth.AuthContext;
import io.deephaven.engine.table.Table;
import io.deephaven.proto.util.Exceptions;

import java.util.List;

public interface InputTableServiceContextualAuthWiring {

    void checkPermissionAddTableToInputTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.AddTableRequest request,
        List<Table> tables);

    void checkPermissionDeleteTableFromInputTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.DeleteTableRequest request,
        List<Table> tables);

    class AllowAll implements InputTableServiceContextualAuthWiring {

        public void checkPermissionAddTableToInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AddTableRequest request,
            List<Table> tables) {}

        public void checkPermissionDeleteTableFromInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DeleteTableRequest request,
            List<Table> tables) {}
    }

    class DenyAll implements InputTableServiceContextualAuthWiring {

        public void checkPermissionAddTableToInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AddTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDeleteTableFromInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DeleteTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
