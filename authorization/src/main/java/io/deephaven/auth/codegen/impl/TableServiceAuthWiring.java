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

public interface TableServiceAuthWiring extends ServiceAuthWiring {

    void checkPermissionGetExportedTableCreationResponse(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.Ticket request);

    void checkPermissionFetchTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FetchTableRequest request);

    void checkPermissionFetchPandasTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FetchPandasTableRequest request);

    void checkPermissionApplyPreviewColumns(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ApplyPreviewColumnsRequest request);

    void checkPermissionEmptyTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.EmptyTableRequest request);

    void checkPermissionTimeTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.TimeTableRequest request);

    void checkPermissionDropColumns(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.DropColumnsRequest request);

    void checkPermissionUpdate(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request);

    void checkPermissionLazyUpdate(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request);

    void checkPermissionView(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request);

    void checkPermissionUpdateView(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request);

    void checkPermissionSelect(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request);

    void checkPermissionUpdateBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.UpdateByRequest request);

    void checkPermissionSelectDistinct(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectDistinctRequest request);

    void checkPermissionFilter(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FilterTableRequest request);

    void checkPermissionUnstructuredFilter(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.UnstructuredFilterTableRequest request);

    void checkPermissionSort(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SortTableRequest request);

    void checkPermissionHead(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailRequest request);

    void checkPermissionTail(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailRequest request);

    void checkPermissionHeadBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request);

    void checkPermissionTailBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request);

    void checkPermissionUngroup(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.UngroupRequest request);

    void checkPermissionMergeTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.MergeTablesRequest request);

    void checkPermissionCrossJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.CrossJoinTablesRequest request);

    void checkPermissionNaturalJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.NaturalJoinTablesRequest request);

    void checkPermissionExactJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ExactJoinTablesRequest request);

    void checkPermissionLeftJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.LeftJoinTablesRequest request);

    void checkPermissionAsOfJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.AsOfJoinTablesRequest request);

    void checkPermissionComboAggregate(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ComboAggregateRequest request);

    void checkPermissionSnapshot(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SnapshotTableRequest request);

    void checkPermissionFlatten(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FlattenRequest request);

    void checkPermissionRunChartDownsample(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.RunChartDownsampleRequest request);

    void checkPermissionCreateInputTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.CreateInputTableRequest request);

    void checkPermissionBatch(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.BatchTableRequest request);

    void checkPermissionExportedTableUpdates(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ExportedTableUpdatesRequest request);

    class AllowAll implements TableServiceAuthWiring {

        public void checkPermissionGetExportedTableCreationResponse(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.Ticket request) {}

        public void checkPermissionFetchTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchTableRequest request) {}

        public void checkPermissionFetchPandasTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchPandasTableRequest request) {}

        public void checkPermissionApplyPreviewColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ApplyPreviewColumnsRequest request) {}

        public void checkPermissionEmptyTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.EmptyTableRequest request) {}

        public void checkPermissionTimeTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.TimeTableRequest request) {}

        public void checkPermissionDropColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DropColumnsRequest request) {}

        public void checkPermissionUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {}

        public void checkPermissionLazyUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {}

        public void checkPermissionView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {}

        public void checkPermissionUpdateView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {}

        public void checkPermissionSelect(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {}

        public void checkPermissionUpdateBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UpdateByRequest request) {}

        public void checkPermissionSelectDistinct(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectDistinctRequest request) {}

        public void checkPermissionFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FilterTableRequest request) {}

        public void checkPermissionUnstructuredFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UnstructuredFilterTableRequest request) {}

        public void checkPermissionSort(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SortTableRequest request) {}

        public void checkPermissionHead(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request) {}

        public void checkPermissionTail(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request) {}

        public void checkPermissionHeadBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request) {}

        public void checkPermissionTailBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request) {}

        public void checkPermissionUngroup(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UngroupRequest request) {}

        public void checkPermissionMergeTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MergeTablesRequest request) {}

        public void checkPermissionCrossJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CrossJoinTablesRequest request) {}

        public void checkPermissionNaturalJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.NaturalJoinTablesRequest request) {}

        public void checkPermissionExactJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExactJoinTablesRequest request) {}

        public void checkPermissionLeftJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.LeftJoinTablesRequest request) {}

        public void checkPermissionAsOfJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AsOfJoinTablesRequest request) {}

        public void checkPermissionComboAggregate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ComboAggregateRequest request) {}

        public void checkPermissionSnapshot(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SnapshotTableRequest request) {}

        public void checkPermissionFlatten(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FlattenRequest request) {}

        public void checkPermissionRunChartDownsample(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.RunChartDownsampleRequest request) {}

        public void checkPermissionCreateInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CreateInputTableRequest request) {}

        public void checkPermissionBatch(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.BatchTableRequest request) {}

        public void checkPermissionExportedTableUpdates(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportedTableUpdatesRequest request) {}
    }

    class DenyAll implements TableServiceAuthWiring {

        public void checkPermissionGetExportedTableCreationResponse(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.Ticket request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFetchTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFetchPandasTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchPandasTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionApplyPreviewColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ApplyPreviewColumnsRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionEmptyTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.EmptyTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTimeTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.TimeTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDropColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DropColumnsRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionLazyUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUpdateView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSelect(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUpdateBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UpdateByRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSelectDistinct(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectDistinctRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FilterTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUnstructuredFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UnstructuredFilterTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSort(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SortTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionHead(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTail(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionHeadBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTailBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUngroup(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UngroupRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionMergeTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MergeTablesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCrossJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CrossJoinTablesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionNaturalJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.NaturalJoinTablesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExactJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExactJoinTablesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionLeftJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.LeftJoinTablesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionAsOfJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AsOfJoinTablesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionComboAggregate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ComboAggregateRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSnapshot(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SnapshotTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFlatten(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FlattenRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionRunChartDownsample(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.RunChartDownsampleRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCreateInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CreateInputTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionBatch(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.BatchTableRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExportedTableUpdates(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportedTableUpdatesRequest request) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
