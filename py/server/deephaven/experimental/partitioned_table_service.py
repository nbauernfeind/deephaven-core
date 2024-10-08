#
# Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
#

from abc import ABC, abstractmethod
from typing import Tuple, Optional, Any, Callable

import jpy

import pyarrow as pa

from deephaven._wrapper import JObjectWrapper
from deephaven.table import Table

_JPythonTableDataService = jpy.get_type("io.deephaven.extensions.barrage.util.PythonTableDataService")
_JTableKeyImpl = jpy.get_type("io.deephaven.extensions.barrage.util.PythonTableDataService$TableKeyImpl")


class TableKey:
    """A key that identifies a table. The key should be unique for each table. The key can be any Python object and
    should include sufficient information to uniquely identify the table for the backend service."""

    def __init__(self, key: Any):
        self._key = key

    @property
    def key(self) -> Any:
        """The user defined key that identifies the table."""
        return self._key


class PartitionedTableLocationKey:
    """A key that identifies a specific partition of a table. The key should be unique for each partition of the table.
    The key can be any Python object and should include sufficient information to uniquely identify the partition for
    the backend service to fetch the partition data.
    """

    def __init__(self, pt_location_key: Any):
        self._pt_location_key = pt_location_key

    @property
    def pt_location_key(self) -> Any:
        return self._pt_location_key


class PartitionedTableServiceBackend(ABC):
    """An interface for a backend service that provides access to partitioned data."""

    @abstractmethod
    def table_schema(self, table_key: TableKey) -> Tuple[pa.Schema, Optional[pa.Schema]]:
        """ Returns the table schema and optionally the schema for the partition columns for the table with the given
        table key.
        The table schema is not required to include the partition columns defined in the partition schema. THe
        partition columns
        are limited to primitive types and strings.
        """
        pass

    @abstractmethod
    def existing_partitions(self, table_key: TableKey,
                            callback: Callable[[PartitionedTableLocationKey, Optional[pa.Table]], None]) -> None:
        """ Provides a callback for the backend service to pass the existing partitions for the table with the given
        table key.
        The 2nd argument of the callback is an optional pa.Table that contains the values for the partitions. The
        schema of the table
        should match the optional partition schema returned by table_schema() for the table_key. The table should
        have a single row
        for the particular partition location key provided in the 1st argument, with the values for the partition
        columns in the row.
        """
        pass

    @abstractmethod
    def subscribe_to_new_partitions(self, table_key: TableKey,
                                    callback: Callable[[PartitionedTableLocationKey, Optional[pa.Table]], None]) -> \
    Callable[[], None]:
        """ Provides a callback for the backend service to pass new partitions for the table with the given table key.
        The 2nd argument of the callback is a pa.Table that contains the values for the partitions. The schema of the
        table
        should match the optional partition schema returned by table_schema() for the table_key. The table should
        have a single row
        for the particular partition location key provided in the 1st argument, with the values for the partition
        columns in the row.

        The return value is a function that can be called to unsubscribe from the new partitions.
        """
        pass

    @abstractmethod
    def partition_size(self, table_key: TableKey, table_location_key: PartitionedTableLocationKey,
                       callback: Callable[[int], None]) -> None:
        """ Provides a callback for the backend service to pass the size of the partition with the given table key
        and partition location key.
        The callback should be called with the size of the partition in number of rows.
        """
        pass

    @abstractmethod
    def subscribe_to_partition_size_changes(self, table_key: TableKey, table_location_key: PartitionedTableLocationKey,
                                            callback: Callable[[int], None]) -> Callable[[], None]:
        """ Provides a callback for the backend service to pass the changed size of the partition with the given
        table key and partition location key.
        The callback should be called with the size of the partition in number of rows.

        The return value is a function that can be called to unsubscribe from the partition size changes.
        """
        pass

    @abstractmethod
    def column_values(self, table_key: TableKey, table_location_key: PartitionedTableLocationKey, col: str) -> pa.Table:
        """ Returns the values for the column with the given name for the partition with the given table key and
        partition location key.
        The returned pa.Table should have a single column with the values for the given column.

        """
        pass


class PythonTableDataService(JObjectWrapper):
    """ A Python wrapper for the Java PythonTableDataService class. It also serves as an adapter between the Java backend
    interface and the Python backend interface.
    """
    j_object_type = _JPythonTableDataService
    _backend: PartitionedTableServiceBackend

    def __init__(self, backend: PartitionedTableServiceBackend):
        """ Creates a new PythonTableDataService with the given user-implemented backend service.

        Args:
            backend (PartitionedTableServiceBackend): the user-implemented backend service implementation
        """
        self._backend = backend
        self._j_tbl_service = _JPythonTableDataService.create(self)

    @property
    def j_object(self):
        return self._j_tbl_service

    def make_table(self, table_key: TableKey) -> Table:
        """ Creates a Table from the backend service for the table with the given table key.

        Args:
            table_key:

        Returns:
            Table: a new table
        """
        j_table_key = _JTableKeyImpl(table_key)
        return Table(self._j_tbl_service.makeTable(j_table_key, True))

    def _table_schema(self, table_key: TableKey) -> Tuple[jpy.JType, jpy.JType]:
        """ Returns the table schema and the partition schema for the table with the given table key as two serialized
        byte buffers.

        Args:
            table_key (TableKey): the table key
        """
        schemas = self._backend.table_schema(table_key)
        pt_schema = schemas[0]
        pc_schema = schemas[1] if len(schemas) > 1 else None
        pc_schema = pc_schema if pc_schema is not None else pa.schema([])
        j_pt_schema_bb = jpy.byte_buffer(pt_schema.serialize())
        j_pc_schema_bb = jpy.byte_buffer(pc_schema.serialize())
        return j_pt_schema_bb, j_pc_schema_bb
