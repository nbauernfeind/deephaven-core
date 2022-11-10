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

public interface FlightServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionHandshake(
        AuthContext authContext);

    void checkPermissionListFlights(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.Criteria request);

    void checkPermissionGetFlightInfo(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.FlightDescriptor request);

    void checkPermissionGetSchema(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.FlightDescriptor request);

    void checkPermissionDoGet(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.Ticket request);

    void checkPermissionDoPut(
        AuthContext authContext);

    void checkPermissionDoExchange(
        AuthContext authContext);

    void checkPermissionDoAction(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.Action request);

    void checkPermissionListActions(
        AuthContext authContext,
        org.apache.arrow.flight.impl.Flight.Empty request);

    class AllowAll implements FlightServiceAuthWiring {

        public void checkPermissionHandshake(
            AuthContext authContext) {}

        public void checkPermissionListFlights(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Criteria request) {}

        public void checkPermissionGetFlightInfo(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightDescriptor request) {}

        public void checkPermissionGetSchema(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightDescriptor request) {}

        public void checkPermissionDoGet(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Ticket request) {}

        public void checkPermissionDoPut(
            AuthContext authContext) {}

        public void checkPermissionDoExchange(
            AuthContext authContext) {}

        public void checkPermissionDoAction(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Action request) {}

        public void checkPermissionListActions(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Empty request) {}
    }

    class DenyAll implements FlightServiceAuthWiring {

        public void checkPermissionHandshake(
            AuthContext authContext) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionListFlights(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Criteria request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionGetFlightInfo(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightDescriptor request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionGetSchema(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.FlightDescriptor request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDoGet(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Ticket request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDoPut(
            AuthContext authContext) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDoExchange(
            AuthContext authContext) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDoAction(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Action request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionListActions(
            AuthContext authContext,
            org.apache.arrow.flight.impl.Flight.Empty request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
