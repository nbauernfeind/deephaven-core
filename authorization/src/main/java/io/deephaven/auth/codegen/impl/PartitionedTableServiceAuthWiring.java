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

public interface PartitionedTableServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionPartitionBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.PartitionByRequest request);

    void checkPermissionMerge(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.MergeRequest request);

    void checkPermissionGetTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.GetTableRequest request);

    class AllowAll implements PartitionedTableServiceAuthWiring {

        public void checkPermissionPartitionBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.PartitionByRequest request) {}

        public void checkPermissionMerge(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MergeRequest request) {}

        public void checkPermissionGetTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.GetTableRequest request) {}
    }

    class DenyAll implements PartitionedTableServiceAuthWiring {

        public void checkPermissionPartitionBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.PartitionByRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionMerge(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MergeRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionGetTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.GetTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
