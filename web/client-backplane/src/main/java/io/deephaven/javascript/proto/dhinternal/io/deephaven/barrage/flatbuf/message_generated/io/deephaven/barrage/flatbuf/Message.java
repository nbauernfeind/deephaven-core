package io.deephaven.javascript.proto.dhinternal.io.deephaven.barrage.flatbuf.message_generated.io.deephaven.barrage.flatbuf;

import elemental2.core.JsArray;
import io.deephaven.javascript.proto.dhinternal.flatbuffers.Builder;
import io.deephaven.javascript.proto.dhinternal.flatbuffers.ByteBuffer;
import io.deephaven.javascript.proto.dhinternal.flatbuffers.Long;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.barrage.flatbuf.schema_generated.io.deephaven.barrage.flatbuf.KeyValue;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.barrage.flatbuf.schema_generated.io.deephaven.barrage.flatbuf.MetadataVersion;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(
    isNative = true,
    name =
        "dhinternal.io.deephaven.barrage.flatbuf.Message_generated.io.deephaven.barrage.flatbuf.Message",
    namespace = JsPackage.GLOBAL)
public class Message {
  public static native void addBodyLength(Builder builder, Long bodyLength);

  public static native void addCustomMetadata(Builder builder, double customMetadataOffset);

  public static native void addHeader(Builder builder, double headerOffset);

  public static native void addHeaderType(Builder builder, MessageHeader headerType);

  public static native void addVersion(Builder builder, MetadataVersion version);

  public static native double createCustomMetadataVector(Builder builder, JsArray<Double> data);

  @JsOverlay
  public static final double createCustomMetadataVector(Builder builder, double[] data) {
    return createCustomMetadataVector(builder, Js.<JsArray<Double>>uncheckedCast(data));
  }

  public static native double createMessage(
      Builder builder,
      MetadataVersion version,
      MessageHeader headerType,
      double headerOffset,
      Long bodyLength,
      double customMetadataOffset);

  public static native double endMessage(Builder builder);

  public static native void finishMessageBuffer(Builder builder, double offset);

  public static native void finishSizePrefixedMessageBuffer(Builder builder, double offset);

  public static native Message getRootAsMessage(ByteBuffer bb, Message obj);

  public static native Message getRootAsMessage(ByteBuffer bb);

  public static native Message getSizePrefixedRootAsMessage(ByteBuffer bb, Message obj);

  public static native Message getSizePrefixedRootAsMessage(ByteBuffer bb);

  public static native void startCustomMetadataVector(Builder builder, double numElems);

  public static native void startMessage(Builder builder);

  public ByteBuffer bb;
  public double bb_pos;

  public native Message __init(double i, ByteBuffer bb);

  public native Long bodyLength();

  public native KeyValue customMetadata(double index, KeyValue obj);

  public native KeyValue customMetadata(double index);

  public native double customMetadataLength();

  public native <T> T header(T obj);

  public native MessageHeader headerType();

  public native MetadataVersion version();
}
