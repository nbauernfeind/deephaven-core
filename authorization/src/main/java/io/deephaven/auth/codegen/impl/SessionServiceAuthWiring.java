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

public interface SessionServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionNewSession(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HandshakeRequest request);

    void checkPermissionRefreshSessionToken(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HandshakeRequest request);

    void checkPermissionCloseSession(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HandshakeRequest request);

    void checkPermissionRelease(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ReleaseRequest request);

    void checkPermissionExportFromTicket(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ExportRequest request);

    void checkPermissionExportNotifications(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ExportNotificationRequest request);

    void checkPermissionTerminationNotification(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.TerminationNotificationRequest request);

    class AllowAll implements SessionServiceAuthWiring {

        public void checkPermissionNewSession(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HandshakeRequest request) {}

        public void checkPermissionRefreshSessionToken(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HandshakeRequest request) {}

        public void checkPermissionCloseSession(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HandshakeRequest request) {}

        public void checkPermissionRelease(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ReleaseRequest request) {}

        public void checkPermissionExportFromTicket(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportRequest request) {}

        public void checkPermissionExportNotifications(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportNotificationRequest request) {}

        public void checkPermissionTerminationNotification(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.TerminationNotificationRequest request) {}
    }

    class DenyAll implements SessionServiceAuthWiring {

        public void checkPermissionNewSession(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HandshakeRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionRefreshSessionToken(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HandshakeRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCloseSession(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HandshakeRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionRelease(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ReleaseRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExportFromTicket(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExportNotifications(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportNotificationRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTerminationNotification(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.TerminationNotificationRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
