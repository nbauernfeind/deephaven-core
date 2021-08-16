package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.table_pb;

import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.ticket_pb.Ticket;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.table_pb.ExportTicketRequest",
    namespace = JsPackage.GLOBAL)
public class ExportTicketRequest {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface SourceTicketFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType.GetTicketUnionType of(
            Object o) {
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
      static ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(
          ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js
                .<ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js
                .<ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }
    }

    @JsOverlay
    static ExportTicketRequest.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    Object getResultId();

    @JsProperty
    ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType getSourceTicket();

    @JsProperty
    void setResultId(Object resultId);

    @JsProperty
    void setSourceTicket(ExportTicketRequest.ToObjectReturnType.SourceTicketFieldType sourceTicket);
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface SourceTicketFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType.GetTicketUnionType of(
            Object o) {
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
      static ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(
          ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js
                .<ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js
                .<ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }
    }

    @JsOverlay
    static ExportTicketRequest.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    Object getResultId();

    @JsProperty
    ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType getSourceTicket();

    @JsProperty
    void setResultId(Object resultId);

    @JsProperty
    void setSourceTicket(
        ExportTicketRequest.ToObjectReturnType0.SourceTicketFieldType sourceTicket);
  }

  public static native ExportTicketRequest deserializeBinary(Uint8Array bytes);

  public static native ExportTicketRequest deserializeBinaryFromReader(
      ExportTicketRequest message, Object reader);

  public static native void serializeBinaryToWriter(ExportTicketRequest message, Object writer);

  public static native ExportTicketRequest.ToObjectReturnType toObject(
      boolean includeInstance, ExportTicketRequest msg);

  public native void clearResultId();

  public native void clearSourceTicket();

  public native Ticket getResultId();

  public native Ticket getSourceTicket();

  public native boolean hasResultId();

  public native boolean hasSourceTicket();

  public native Uint8Array serializeBinary();

  public native void setResultId();

  public native void setResultId(Ticket value);

  public native void setSourceTicket();

  public native void setSourceTicket(Ticket value);

  public native ExportTicketRequest.ToObjectReturnType0 toObject();

  public native ExportTicketRequest.ToObjectReturnType0 toObject(boolean includeInstance);
}
