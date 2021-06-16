package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.table_pb.batchtablerequest.operation;

import jsinterop.annotations.JsEnum;
import jsinterop.annotations.JsPackage;

@JsEnum(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.table_pb.BatchTableRequest.Operation.OpCase",
    namespace = JsPackage.GLOBAL)
public enum OpCase {
  COMBO_AGGREGATE,
  DROP_COLUMNS,
  EMPTY_TABLE,
  FILTER,
  FLATTEN,
  HEAD,
  HEAD_BY,
  JOIN,
  LAZY_UPDATE,
  MERGE,
  OP_NOT_SET,
  SELECT,
  SELECT_DISTINCT,
  SNAPSHOT,
  SORT,
  TAIL,
  TAIL_BY,
  TIME_TABLE,
  UNGROUP,
  UNSTRUCTURED_FILTER,
  UPDATE,
  UPDATE_VIEW,
  VIEW;
}
