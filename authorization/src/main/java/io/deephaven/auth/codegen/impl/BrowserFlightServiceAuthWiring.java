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

public interface BrowserFlightServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionOpenHandshake(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.HandshakeRequest request);

    void checkPermissionNextHandshake(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.HandshakeRequest request);

    void checkPermissionOpenDoPut(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.FlightData request);

    void checkPermissionNextDoPut(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.FlightData request);

    void checkPermissionOpenDoExchange(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.FlightData request);

    void checkPermissionNextDoExchange(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.FlightData request);

    class AllowAll implements BrowserFlightServiceAuthWiring {

        public void checkPermissionOpenHandshake(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.HandshakeRequest request) {}

        public void checkPermissionNextHandshake(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.HandshakeRequest request) {}

        public void checkPermissionOpenDoPut(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {}

        public void checkPermissionNextDoPut(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {}

        public void checkPermissionOpenDoExchange(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {}

        public void checkPermissionNextDoExchange(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {}
    }

    class DenyAll implements BrowserFlightServiceAuthWiring {

        public void checkPermissionOpenHandshake(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.HandshakeRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionNextHandshake(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.HandshakeRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionOpenDoPut(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionNextDoPut(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionOpenDoExchange(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionNextDoExchange(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightData request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
