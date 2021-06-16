package io.deephaven.javascript.proto.dhinternal.io.deephaven.barrage.flatbuf.message_generated.io.deephaven.barrage.flatbuf;

import jsinterop.annotations.JsEnum;
import jsinterop.annotations.JsPackage;

@JsEnum(
    isNative = true,
    name =
        "dhinternal.io.deephaven.barrage.flatbuf.Message_generated.io.deephaven.barrage.flatbuf.MessageHeader",
    namespace = JsPackage.GLOBAL)
public enum MessageHeader {
  BarrageRecordBatch,
  DictionaryBatch,
  NONE,
  RecordBatch,
  Schema,
  SparseTensor,
  Tensor;
}
