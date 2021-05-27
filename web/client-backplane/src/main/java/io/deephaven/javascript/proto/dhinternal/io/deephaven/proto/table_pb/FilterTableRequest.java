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
    name = "dhinternal.io.deephaven.proto.table_pb.FilterTableRequest",
    namespace = JsPackage.GLOBAL)
public class FilterTableRequest {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface FiltersListFieldType {
      @JsOverlay
      static FilterTableRequest.ToObjectReturnType.FiltersListFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      JsArray<Object> getChildrenList();

      @JsProperty
      double getDoubleValue();

      @JsProperty
      double getFilterOp();

      @JsProperty
      double getLongValue();

      @JsProperty
      double getNanoTimeValue();

      @JsProperty
      String getStringValue();

      @JsProperty
      boolean isBoolValue();

      @JsProperty
      void setBoolValue(boolean boolValue);

      @JsProperty
      void setChildrenList(JsArray<Object> childrenList);

      @JsOverlay
      default void setChildrenList(Object[] childrenList) {
        setChildrenList(Js.<JsArray<Object>>uncheckedCast(childrenList));
      }

      @JsProperty
      void setDoubleValue(double doubleValue);

      @JsProperty
      void setFilterOp(double filterOp);

      @JsProperty
      void setLongValue(double longValue);

      @JsProperty
      void setNanoTimeValue(double nanoTimeValue);

      @JsProperty
      void setStringValue(String stringValue);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ResultIdFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static FilterTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType of(
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
      static FilterTableRequest.ToObjectReturnType.ResultIdFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      FilterTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(
          FilterTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js
                .<FilterTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js
                .<FilterTableRequest.ToObjectReturnType.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface SourceIdFieldType {
      @JsOverlay
      static FilterTableRequest.ToObjectReturnType.SourceIdFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      double getBatchOffset();

      @JsProperty
      Object getTicket();

      @JsProperty
      void setBatchOffset(double batchOffset);

      @JsProperty
      void setTicket(Object ticket);
    }

    @JsOverlay
    static FilterTableRequest.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    JsArray<FilterTableRequest.ToObjectReturnType.FiltersListFieldType> getFiltersList();

    @JsProperty
    FilterTableRequest.ToObjectReturnType.ResultIdFieldType getResultId();

    @JsProperty
    FilterTableRequest.ToObjectReturnType.SourceIdFieldType getSourceId();

    @JsOverlay
    default void setFiltersList(
        FilterTableRequest.ToObjectReturnType.FiltersListFieldType[] filtersList) {
      setFiltersList(
          Js.<JsArray<FilterTableRequest.ToObjectReturnType.FiltersListFieldType>>uncheckedCast(
              filtersList));
    }

    @JsProperty
    void setFiltersList(
        JsArray<FilterTableRequest.ToObjectReturnType.FiltersListFieldType> filtersList);

    @JsProperty
    void setResultId(FilterTableRequest.ToObjectReturnType.ResultIdFieldType resultId);

    @JsProperty
    void setSourceId(FilterTableRequest.ToObjectReturnType.SourceIdFieldType sourceId);
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface FiltersListFieldType {
      @JsOverlay
      static FilterTableRequest.ToObjectReturnType0.FiltersListFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      JsArray<Object> getChildrenList();

      @JsProperty
      double getDoubleValue();

      @JsProperty
      double getFilterOp();

      @JsProperty
      double getLongValue();

      @JsProperty
      double getNanoTimeValue();

      @JsProperty
      String getStringValue();

      @JsProperty
      boolean isBoolValue();

      @JsProperty
      void setBoolValue(boolean boolValue);

      @JsProperty
      void setChildrenList(JsArray<Object> childrenList);

      @JsOverlay
      default void setChildrenList(Object[] childrenList) {
        setChildrenList(Js.<JsArray<Object>>uncheckedCast(childrenList));
      }

      @JsProperty
      void setDoubleValue(double doubleValue);

      @JsProperty
      void setFilterOp(double filterOp);

      @JsProperty
      void setLongValue(double longValue);

      @JsProperty
      void setNanoTimeValue(double nanoTimeValue);

      @JsProperty
      void setStringValue(String stringValue);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ResultIdFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static FilterTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType of(
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
      static FilterTableRequest.ToObjectReturnType0.ResultIdFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      FilterTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(
          FilterTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js
                .<FilterTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js
                .<FilterTableRequest.ToObjectReturnType0.ResultIdFieldType.GetTicketUnionType>
                    uncheckedCast(ticket));
      }
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface SourceIdFieldType {
      @JsOverlay
      static FilterTableRequest.ToObjectReturnType0.SourceIdFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      double getBatchOffset();

      @JsProperty
      Object getTicket();

      @JsProperty
      void setBatchOffset(double batchOffset);

      @JsProperty
      void setTicket(Object ticket);
    }

    @JsOverlay
    static FilterTableRequest.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    JsArray<FilterTableRequest.ToObjectReturnType0.FiltersListFieldType> getFiltersList();

    @JsProperty
    FilterTableRequest.ToObjectReturnType0.ResultIdFieldType getResultId();

    @JsProperty
    FilterTableRequest.ToObjectReturnType0.SourceIdFieldType getSourceId();

    @JsOverlay
    default void setFiltersList(
        FilterTableRequest.ToObjectReturnType0.FiltersListFieldType[] filtersList) {
      setFiltersList(
          Js.<JsArray<FilterTableRequest.ToObjectReturnType0.FiltersListFieldType>>uncheckedCast(
              filtersList));
    }

    @JsProperty
    void setFiltersList(
        JsArray<FilterTableRequest.ToObjectReturnType0.FiltersListFieldType> filtersList);

    @JsProperty
    void setResultId(FilterTableRequest.ToObjectReturnType0.ResultIdFieldType resultId);

    @JsProperty
    void setSourceId(FilterTableRequest.ToObjectReturnType0.SourceIdFieldType sourceId);
  }

  public static native FilterTableRequest deserializeBinary(Uint8Array bytes);

  public static native FilterTableRequest deserializeBinaryFromReader(
      FilterTableRequest message, Object reader);

  public static native void serializeBinaryToWriter(FilterTableRequest message, Object writer);

  public static native FilterTableRequest.ToObjectReturnType toObject(
      boolean includeInstance, FilterTableRequest msg);

  public native FilterDescription addFilters();

  public native FilterDescription addFilters(FilterDescription value, double index);

  public native FilterDescription addFilters(FilterDescription value);

  public native void clearFiltersList();

  public native void clearResultId();

  public native void clearSourceId();

  public native JsArray<FilterDescription> getFiltersList();

  public native Ticket getResultId();

  public native TableReference getSourceId();

  public native boolean hasResultId();

  public native boolean hasSourceId();

  public native Uint8Array serializeBinary();

  @JsOverlay
  public final void setFiltersList(FilterDescription[] value) {
    setFiltersList(Js.<JsArray<FilterDescription>>uncheckedCast(value));
  }

  public native void setFiltersList(JsArray<FilterDescription> value);

  public native void setResultId();

  public native void setResultId(Ticket value);

  public native void setSourceId();

  public native void setSourceId(TableReference value);

  public native FilterTableRequest.ToObjectReturnType0 toObject();

  public native FilterTableRequest.ToObjectReturnType0 toObject(boolean includeInstance);
}
