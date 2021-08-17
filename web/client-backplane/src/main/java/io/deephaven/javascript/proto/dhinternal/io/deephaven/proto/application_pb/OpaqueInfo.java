package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb;

import elemental2.core.Uint8Array;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.application_pb.OpaqueInfo",
    namespace = JsPackage.GLOBAL)
public class OpaqueInfo {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface GetAppMetadataUnionType {
    @JsOverlay
    static OpaqueInfo.GetAppMetadataUnionType of(Object o) {
      return Js.cast(o);
    }

    @JsOverlay
    default String asString() {
      return Js.asString(this);
    }

    @JsOverlay
    default Uint8Array asUint8Array() {
      return Js.cast(this);
    }

    @JsOverlay
    default boolean isString() {
      return (Object) this instanceof String;
    }

    @JsOverlay
    default boolean isUint8Array() {
      return (Object) this instanceof Uint8Array;
    }
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface SetAppMetadataValueUnionType {
    @JsOverlay
    static OpaqueInfo.SetAppMetadataValueUnionType of(Object o) {
      return Js.cast(o);
    }

    @JsOverlay
    default String asString() {
      return Js.asString(this);
    }

    @JsOverlay
    default Uint8Array asUint8Array() {
      return Js.cast(this);
    }

    @JsOverlay
    default boolean isString() {
      return (Object) this instanceof String;
    }

    @JsOverlay
    default boolean isUint8Array() {
      return (Object) this instanceof Uint8Array;
    }
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface GetAppMetadataUnionType {
      @JsOverlay
      static OpaqueInfo.ToObjectReturnType.GetAppMetadataUnionType of(Object o) {
        return Js.cast(o);
      }

      @JsOverlay
      default String asString() {
        return Js.asString(this);
      }

      @JsOverlay
      default Uint8Array asUint8Array() {
        return Js.cast(this);
      }

      @JsOverlay
      default boolean isString() {
        return (Object) this instanceof String;
      }

      @JsOverlay
      default boolean isUint8Array() {
        return (Object) this instanceof Uint8Array;
      }
    }

    @JsOverlay
    static OpaqueInfo.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    OpaqueInfo.ToObjectReturnType.GetAppMetadataUnionType getAppMetadata();

    @JsProperty
    void setAppMetadata(OpaqueInfo.ToObjectReturnType.GetAppMetadataUnionType appMetadata);

    @JsOverlay
    default void setAppMetadata(String appMetadata) {
      setAppMetadata(
          Js.<OpaqueInfo.ToObjectReturnType.GetAppMetadataUnionType>uncheckedCast(appMetadata));
    }

    @JsOverlay
    default void setAppMetadata(Uint8Array appMetadata) {
      setAppMetadata(
          Js.<OpaqueInfo.ToObjectReturnType.GetAppMetadataUnionType>uncheckedCast(appMetadata));
    }
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface GetAppMetadataUnionType {
      @JsOverlay
      static OpaqueInfo.ToObjectReturnType0.GetAppMetadataUnionType of(Object o) {
        return Js.cast(o);
      }

      @JsOverlay
      default String asString() {
        return Js.asString(this);
      }

      @JsOverlay
      default Uint8Array asUint8Array() {
        return Js.cast(this);
      }

      @JsOverlay
      default boolean isString() {
        return (Object) this instanceof String;
      }

      @JsOverlay
      default boolean isUint8Array() {
        return (Object) this instanceof Uint8Array;
      }
    }

    @JsOverlay
    static OpaqueInfo.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    OpaqueInfo.ToObjectReturnType0.GetAppMetadataUnionType getAppMetadata();

    @JsProperty
    void setAppMetadata(OpaqueInfo.ToObjectReturnType0.GetAppMetadataUnionType appMetadata);

    @JsOverlay
    default void setAppMetadata(String appMetadata) {
      setAppMetadata(
          Js.<OpaqueInfo.ToObjectReturnType0.GetAppMetadataUnionType>uncheckedCast(appMetadata));
    }

    @JsOverlay
    default void setAppMetadata(Uint8Array appMetadata) {
      setAppMetadata(
          Js.<OpaqueInfo.ToObjectReturnType0.GetAppMetadataUnionType>uncheckedCast(appMetadata));
    }
  }

  public static native OpaqueInfo deserializeBinary(Uint8Array bytes);

  public static native OpaqueInfo deserializeBinaryFromReader(OpaqueInfo message, Object reader);

  public static native void serializeBinaryToWriter(OpaqueInfo message, Object writer);

  public static native OpaqueInfo.ToObjectReturnType toObject(
      boolean includeInstance, OpaqueInfo msg);

  public native OpaqueInfo.GetAppMetadataUnionType getAppMetadata();

  public native String getAppMetadata_asB64();

  public native Uint8Array getAppMetadata_asU8();

  public native Uint8Array serializeBinary();

  public native void setAppMetadata(OpaqueInfo.SetAppMetadataValueUnionType value);

  @JsOverlay
  public final void setAppMetadata(String value) {
    setAppMetadata(Js.<OpaqueInfo.SetAppMetadataValueUnionType>uncheckedCast(value));
  }

  @JsOverlay
  public final void setAppMetadata(Uint8Array value) {
    setAppMetadata(Js.<OpaqueInfo.SetAppMetadataValueUnionType>uncheckedCast(value));
  }

  public native OpaqueInfo.ToObjectReturnType0 toObject();

  public native OpaqueInfo.ToObjectReturnType0 toObject(boolean includeInstance);
}
