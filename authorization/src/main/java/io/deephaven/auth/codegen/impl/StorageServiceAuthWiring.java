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

public interface StorageServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionListItems(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ListItemsRequest request);

    void checkPermissionFetchFile(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FetchFileRequest request);

    void checkPermissionSaveFile(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SaveFileRequest request);

    void checkPermissionMoveItem(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.MoveItemRequest request);

    void checkPermissionCreateDirectory(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.CreateDirectoryRequest request);

    void checkPermissionDeleteItem(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.DeleteItemRequest request);

    class AllowAll implements StorageServiceAuthWiring {

        public void checkPermissionListItems(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ListItemsRequest request) {}

        public void checkPermissionFetchFile(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchFileRequest request) {}

        public void checkPermissionSaveFile(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SaveFileRequest request) {}

        public void checkPermissionMoveItem(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MoveItemRequest request) {}

        public void checkPermissionCreateDirectory(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CreateDirectoryRequest request) {}

        public void checkPermissionDeleteItem(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DeleteItemRequest request) {}
    }

    class DenyAll implements StorageServiceAuthWiring {

        public void checkPermissionListItems(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ListItemsRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFetchFile(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchFileRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSaveFile(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SaveFileRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionMoveItem(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MoveItemRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCreateDirectory(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CreateDirectoryRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDeleteItem(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DeleteItemRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
