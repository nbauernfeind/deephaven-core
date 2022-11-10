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

public interface ConsoleServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionGetConsoleTypes(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.GetConsoleTypesRequest request);

    void checkPermissionStartConsole(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.StartConsoleRequest request);

    void checkPermissionGetHeapInfo(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.GetHeapInfoRequest request);

    void checkPermissionSubscribeToLogs(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.LogSubscriptionRequest request);

    void checkPermissionExecuteCommand(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.ExecuteCommandRequest request);

    void checkPermissionCancelCommand(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.CancelCommandRequest request);

    void checkPermissionBindTableToVariable(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.BindTableToVariableRequest request);

    void checkPermissionAutoCompleteStream(
        AuthContext authContext);

    void checkPermissionOpenAutoCompleteStream(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.AutoCompleteRequest request);

    void checkPermissionNextAutoCompleteStream(
        AuthContext authContext,
        io.deephaven.proto.backplane.script.grpc.AutoCompleteRequest request);

    class AllowAll implements ConsoleServiceAuthWiring {

        public void checkPermissionGetConsoleTypes(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.GetConsoleTypesRequest request) {}

        public void checkPermissionStartConsole(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.StartConsoleRequest request) {}

        public void checkPermissionGetHeapInfo(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.GetHeapInfoRequest request) {}

        public void checkPermissionSubscribeToLogs(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.LogSubscriptionRequest request) {}

        public void checkPermissionExecuteCommand(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.ExecuteCommandRequest request) {}

        public void checkPermissionCancelCommand(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.CancelCommandRequest request) {}

        public void checkPermissionBindTableToVariable(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.BindTableToVariableRequest request) {}

        public void checkPermissionAutoCompleteStream(
            AuthContext authContext) {}

        public void checkPermissionOpenAutoCompleteStream(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.AutoCompleteRequest request) {}

        public void checkPermissionNextAutoCompleteStream(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.AutoCompleteRequest request) {}
    }

    class DenyAll implements ConsoleServiceAuthWiring {

        public void checkPermissionGetConsoleTypes(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.GetConsoleTypesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionStartConsole(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.StartConsoleRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionGetHeapInfo(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.GetHeapInfoRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSubscribeToLogs(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.LogSubscriptionRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExecuteCommand(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.ExecuteCommandRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCancelCommand(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.CancelCommandRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionBindTableToVariable(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.BindTableToVariableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionAutoCompleteStream(
            AuthContext authContext) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionOpenAutoCompleteStream(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.AutoCompleteRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionNextAutoCompleteStream(
            AuthContext authContext,
            io.deephaven.proto.backplane.script.grpc.AutoCompleteRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
