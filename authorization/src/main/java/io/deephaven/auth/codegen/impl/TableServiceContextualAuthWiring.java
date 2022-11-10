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

public interface TableServiceContextualAuthWiring {

    void checkPermissionGetExportedTableCreationResponse(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.Ticket request,
        List<Table> tables);

    void checkPermissionFetchTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FetchTableRequest request,
        List<Table> tables);

    void checkPermissionFetchPandasTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FetchPandasTableRequest request,
        List<Table> tables);

    void checkPermissionApplyPreviewColumns(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ApplyPreviewColumnsRequest request,
        List<Table> tables);

    void checkPermissionEmptyTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.EmptyTableRequest request,
        List<Table> tables);

    void checkPermissionTimeTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.TimeTableRequest request,
        List<Table> tables);

    void checkPermissionDropColumns(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.DropColumnsRequest request,
        List<Table> tables);

    void checkPermissionUpdate(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
        List<Table> tables);

    void checkPermissionLazyUpdate(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
        List<Table> tables);

    void checkPermissionView(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
        List<Table> tables);

    void checkPermissionUpdateView(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
        List<Table> tables);

    void checkPermissionSelect(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
        List<Table> tables);

    void checkPermissionUpdateBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.UpdateByRequest request,
        List<Table> tables);

    void checkPermissionSelectDistinct(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SelectDistinctRequest request,
        List<Table> tables);

    void checkPermissionFilter(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FilterTableRequest request,
        List<Table> tables);

    void checkPermissionUnstructuredFilter(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.UnstructuredFilterTableRequest request,
        List<Table> tables);

    void checkPermissionSort(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SortTableRequest request,
        List<Table> tables);

    void checkPermissionHead(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailRequest request,
        List<Table> tables);

    void checkPermissionTail(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailRequest request,
        List<Table> tables);

    void checkPermissionHeadBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request,
        List<Table> tables);

    void checkPermissionTailBy(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request,
        List<Table> tables);

    void checkPermissionUngroup(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.UngroupRequest request,
        List<Table> tables);

    void checkPermissionMergeTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.MergeTablesRequest request,
        List<Table> tables);

    void checkPermissionCrossJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.CrossJoinTablesRequest request,
        List<Table> tables);

    void checkPermissionNaturalJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.NaturalJoinTablesRequest request,
        List<Table> tables);

    void checkPermissionExactJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ExactJoinTablesRequest request,
        List<Table> tables);

    void checkPermissionLeftJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.LeftJoinTablesRequest request,
        List<Table> tables);

    void checkPermissionAsOfJoinTables(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.AsOfJoinTablesRequest request,
        List<Table> tables);

    void checkPermissionComboAggregate(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ComboAggregateRequest request,
        List<Table> tables);

    void checkPermissionSnapshot(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.SnapshotTableRequest request,
        List<Table> tables);

    void checkPermissionFlatten(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.FlattenRequest request,
        List<Table> tables);

    void checkPermissionRunChartDownsample(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.RunChartDownsampleRequest request,
        List<Table> tables);

    void checkPermissionCreateInputTable(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.CreateInputTableRequest request,
        List<Table> tables);

    void checkPermissionExportedTableUpdates(
        AuthContext authContext,
        io.deephaven.proto.backplane.grpc.ExportedTableUpdatesRequest request,
        List<Table> tables);

    class AllowAll implements TableServiceContextualAuthWiring {

        public void checkPermissionGetExportedTableCreationResponse(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.Ticket request,
            List<Table> tables) {}

        public void checkPermissionFetchTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchTableRequest request,
            List<Table> tables) {}

        public void checkPermissionFetchPandasTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchPandasTableRequest request,
            List<Table> tables) {}

        public void checkPermissionApplyPreviewColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ApplyPreviewColumnsRequest request,
            List<Table> tables) {}

        public void checkPermissionEmptyTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.EmptyTableRequest request,
            List<Table> tables) {}

        public void checkPermissionTimeTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.TimeTableRequest request,
            List<Table> tables) {}

        public void checkPermissionDropColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DropColumnsRequest request,
            List<Table> tables) {}

        public void checkPermissionUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {}

        public void checkPermissionLazyUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {}

        public void checkPermissionView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {}

        public void checkPermissionUpdateView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {}

        public void checkPermissionSelect(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {}

        public void checkPermissionUpdateBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UpdateByRequest request,
            List<Table> tables) {}

        public void checkPermissionSelectDistinct(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectDistinctRequest request,
            List<Table> tables) {}

        public void checkPermissionFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FilterTableRequest request,
            List<Table> tables) {}

        public void checkPermissionUnstructuredFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UnstructuredFilterTableRequest request,
            List<Table> tables) {}

        public void checkPermissionSort(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SortTableRequest request,
            List<Table> tables) {}

        public void checkPermissionHead(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request,
            List<Table> tables) {}

        public void checkPermissionTail(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request,
            List<Table> tables) {}

        public void checkPermissionHeadBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request,
            List<Table> tables) {}

        public void checkPermissionTailBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request,
            List<Table> tables) {}

        public void checkPermissionUngroup(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UngroupRequest request,
            List<Table> tables) {}

        public void checkPermissionMergeTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MergeTablesRequest request,
            List<Table> tables) {}

        public void checkPermissionCrossJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CrossJoinTablesRequest request,
            List<Table> tables) {}

        public void checkPermissionNaturalJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.NaturalJoinTablesRequest request,
            List<Table> tables) {}

        public void checkPermissionExactJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExactJoinTablesRequest request,
            List<Table> tables) {}

        public void checkPermissionLeftJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.LeftJoinTablesRequest request,
            List<Table> tables) {}

        public void checkPermissionAsOfJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AsOfJoinTablesRequest request,
            List<Table> tables) {}

        public void checkPermissionComboAggregate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ComboAggregateRequest request,
            List<Table> tables) {}

        public void checkPermissionSnapshot(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SnapshotTableRequest request,
            List<Table> tables) {}

        public void checkPermissionFlatten(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FlattenRequest request,
            List<Table> tables) {}

        public void checkPermissionRunChartDownsample(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.RunChartDownsampleRequest request,
            List<Table> tables) {}

        public void checkPermissionCreateInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CreateInputTableRequest request,
            List<Table> tables) {}

        public void checkPermissionExportedTableUpdates(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportedTableUpdatesRequest request,
            List<Table> tables) {}
    }

    class DenyAll implements TableServiceContextualAuthWiring {

        public void checkPermissionGetExportedTableCreationResponse(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.Ticket request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFetchTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFetchPandasTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FetchPandasTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionApplyPreviewColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ApplyPreviewColumnsRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionEmptyTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.EmptyTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTimeTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.TimeTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionDropColumns(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.DropColumnsRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionLazyUpdate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUpdateView(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSelect(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectOrUpdateRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUpdateBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UpdateByRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSelectDistinct(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SelectDistinctRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FilterTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUnstructuredFilter(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UnstructuredFilterTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSort(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SortTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionHead(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTail(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionHeadBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionTailBy(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.HeadOrTailByRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionUngroup(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.UngroupRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionMergeTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.MergeTablesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCrossJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CrossJoinTablesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionNaturalJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.NaturalJoinTablesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExactJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExactJoinTablesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionLeftJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.LeftJoinTablesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionAsOfJoinTables(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.AsOfJoinTablesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionComboAggregate(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ComboAggregateRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionSnapshot(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.SnapshotTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionFlatten(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.FlattenRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionRunChartDownsample(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.RunChartDownsampleRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionCreateInputTable(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.CreateInputTableRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }

        public void checkPermissionExportedTableUpdates(
            AuthContext authContext,
            io.deephaven.proto.backplane.grpc.ExportedTableUpdatesRequest request,
            List<Table> tables) {
            throw Exceptions.statusRuntimeException(Code.PERMISSION_DENIED, "Operation not allowed.");
        }
    }
}
