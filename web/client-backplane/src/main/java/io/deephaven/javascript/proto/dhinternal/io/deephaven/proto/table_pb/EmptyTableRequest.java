package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.table_pb;

import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.arrow.flight.protocol.flight_pb.Ticket;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.table_pb.EmptyTableRequest",
    namespace = JsPackage.GLOBAL)
public class EmptyTableRequest {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ResultIdFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static EmptyTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType of(
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
      static EmptyTableRequest.ToObjectReturnType.ResultIdFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      EmptyTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(
          EmptyTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js
                .<EmptyTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js
                .<EmptyTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }
    }

    @JsOverlay
    static EmptyTableRequest.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    JsArray<String> getColumnNamesList();

    @JsProperty
    JsArray<String> getColumnTypesList();

    @JsProperty
    EmptyTableRequest.ToObjectReturnType.ResultIdFieldType getResultId();

    @JsProperty
    String getSize();

    @JsProperty
    void setColumnNamesList(JsArray<String> columnNamesList);

    @JsOverlay
    default void setColumnNamesList(String[] columnNamesList) {
      setColumnNamesList(Js.<JsArray<String>>uncheckedCast(columnNamesList));
    }

    @JsProperty
    void setColumnTypesList(JsArray<String> columnTypesList);

    @JsOverlay
    default void setColumnTypesList(String[] columnTypesList) {
      setColumnTypesList(Js.<JsArray<String>>uncheckedCast(columnTypesList));
    }

    @JsProperty
    void setResultId(EmptyTableRequest.ToObjectReturnType.ResultIdFieldType resultId);

    @JsProperty
    void setSize(String size);
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ResultIdFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType of(
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
      static EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(
          EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js
                .<EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js
                .<EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }
    }

    @JsOverlay
    static EmptyTableRequest.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    JsArray<String> getColumnNamesList();

    @JsProperty
    JsArray<String> getColumnTypesList();

    @JsProperty
    EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType getResultId();

    @JsProperty
    String getSize();

    @JsProperty
    void setColumnNamesList(JsArray<String> columnNamesList);

    @JsOverlay
    default void setColumnNamesList(String[] columnNamesList) {
      setColumnNamesList(Js.<JsArray<String>>uncheckedCast(columnNamesList));
    }

    @JsProperty
    void setColumnTypesList(JsArray<String> columnTypesList);

    @JsOverlay
    default void setColumnTypesList(String[] columnTypesList) {
      setColumnTypesList(Js.<JsArray<String>>uncheckedCast(columnTypesList));
    }

    @JsProperty
    void setResultId(EmptyTableRequest.ToObjectReturnType0.ResultIdFieldType resultId);

    @JsProperty
    void setSize(String size);
  }

  public static native EmptyTableRequest deserializeBinary(Uint8Array bytes);

  public static native EmptyTableRequest deserializeBinaryFromReader(
      EmptyTableRequest message, Object reader);

  public static native void serializeBinaryToWriter(EmptyTableRequest message, Object writer);

  public static native EmptyTableRequest.ToObjectReturnType toObject(
      boolean includeInstance, EmptyTableRequest msg);

  public native String addColumnNames(String value, double index);

  public native String addColumnNames(String value);

  public native String addColumnTypes(String value, double index);

  public native String addColumnTypes(String value);

  public native void clearColumnNamesList();

  public native void clearColumnTypesList();

  public native void clearResultId();

  public native JsArray<String> getColumnNamesList();

  public native JsArray<String> getColumnTypesList();

  public native Ticket getResultId();

  public native String getSize();

  public native boolean hasResultId();

  public native Uint8Array serializeBinary();

  public native void setColumnNamesList(JsArray<String> value);

  @JsOverlay
  public final void setColumnNamesList(String[] value) {
    setColumnNamesList(Js.<JsArray<String>>uncheckedCast(value));
  }

  public native void setColumnTypesList(JsArray<String> value);

  @JsOverlay
  public final void setColumnTypesList(String[] value) {
    setColumnTypesList(Js.<JsArray<String>>uncheckedCast(value));
  }

  public native void setResultId();

  public native void setResultId(Ticket value);

  public native void setSize(String value);

  public native EmptyTableRequest.ToObjectReturnType0 toObject();

  public native EmptyTableRequest.ToObjectReturnType0 toObject(boolean includeInstance);
}
