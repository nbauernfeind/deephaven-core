package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.fieldinfo;

import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.arrow.flight.protocol.flight_pb.FlightInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.FigureInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.OpaqueInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.fieldinfo.fieldtype.FieldCase;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.field_pb.FieldInfo.FieldType",
    namespace = JsPackage.GLOBAL)
public class FieldType {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface OpaqueFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetAppMetadataUnionType {
        @JsOverlay
        static FieldType.ToObjectReturnType.OpaqueFieldType.GetAppMetadataUnionType of(Object o) {
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
      static FieldType.ToObjectReturnType.OpaqueFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      FieldType.ToObjectReturnType.OpaqueFieldType.GetAppMetadataUnionType getAppMetadata();

      @JsProperty
      void setAppMetadata(
          FieldType.ToObjectReturnType.OpaqueFieldType.GetAppMetadataUnionType appMetadata);

      @JsOverlay
      default void setAppMetadata(String appMetadata) {
        setAppMetadata(
            Js.<FieldType.ToObjectReturnType.OpaqueFieldType.GetAppMetadataUnionType>uncheckedCast(
                appMetadata));
      }

      @JsOverlay
      default void setAppMetadata(Uint8Array appMetadata) {
        setAppMetadata(
            Js.<FieldType.ToObjectReturnType.OpaqueFieldType.GetAppMetadataUnionType>uncheckedCast(
                appMetadata));
      }
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface TableFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface EndpointListFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface LocationListFieldType {
          @JsOverlay
          static FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType
                  .LocationListFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          String getUri();

          @JsProperty
          void setUri(String uri);
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface TicketFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetTicketUnionType {
            @JsOverlay
            static FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.TicketFieldType
                    .GetTicketUnionType
                of(Object o) {
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
          static FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.TicketFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.TicketFieldType
                  .GetTicketUnionType
              getTicket();

          @JsProperty
          void setTicket(
              FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.TicketFieldType
                      .GetTicketUnionType
                  ticket);

          @JsOverlay
          default void setTicket(String ticket) {
            setTicket(
                Js
                    .<FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType
                            .TicketFieldType.GetTicketUnionType>
                        uncheckedCast(ticket));
          }

          @JsOverlay
          default void setTicket(Uint8Array ticket) {
            setTicket(
                Js
                    .<FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType
                            .TicketFieldType.GetTicketUnionType>
                        uncheckedCast(ticket));
          }
        }

        @JsOverlay
        static FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        JsArray<
                FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType
                    .LocationListFieldType>
            getLocationList();

        @JsProperty
        FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.TicketFieldType
            getTicket();

        @JsProperty
        void setLocationList(
            JsArray<
                    FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType
                        .LocationListFieldType>
                locationList);

        @JsOverlay
        default void setLocationList(
            FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.LocationListFieldType
                    []
                locationList) {
          setLocationList(
              Js
                  .<JsArray<
                          FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType
                              .LocationListFieldType>>
                      uncheckedCast(locationList));
        }

        @JsProperty
        void setTicket(
            FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType.TicketFieldType
                ticket);
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface FlightDescriptorFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetCmdUnionType {
          @JsOverlay
          static FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType
                  .GetCmdUnionType
              of(Object o) {
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
        static FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType.GetCmdUnionType
            getCmd();

        @JsProperty
        JsArray<String> getPathList();

        @JsProperty
        double getType();

        @JsProperty
        void setCmd(
            FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType.GetCmdUnionType
                cmd);

        @JsOverlay
        default void setCmd(String cmd) {
          setCmd(
              Js
                  .<FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType
                          .GetCmdUnionType>
                      uncheckedCast(cmd));
        }

        @JsOverlay
        default void setCmd(Uint8Array cmd) {
          setCmd(
              Js
                  .<FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType
                          .GetCmdUnionType>
                      uncheckedCast(cmd));
        }

        @JsProperty
        void setPathList(JsArray<String> pathList);

        @JsOverlay
        default void setPathList(String[] pathList) {
          setPathList(Js.<JsArray<String>>uncheckedCast(pathList));
        }

        @JsProperty
        void setType(double type);
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetSchemaUnionType {
        @JsOverlay
        static FieldType.ToObjectReturnType.TableFieldType.GetSchemaUnionType of(Object o) {
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
      static FieldType.ToObjectReturnType.TableFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      JsArray<FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType> getEndpointList();

      @JsProperty
      FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType getFlightDescriptor();

      @JsProperty
      FieldType.ToObjectReturnType.TableFieldType.GetSchemaUnionType getSchema();

      @JsProperty
      double getTotalBytes();

      @JsProperty
      double getTotalRecords();

      @JsOverlay
      default void setEndpointList(
          FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType[] endpointList) {
        setEndpointList(
            Js
                .<JsArray<FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType>>
                    uncheckedCast(endpointList));
      }

      @JsProperty
      void setEndpointList(
          JsArray<FieldType.ToObjectReturnType.TableFieldType.EndpointListFieldType> endpointList);

      @JsProperty
      void setFlightDescriptor(
          FieldType.ToObjectReturnType.TableFieldType.FlightDescriptorFieldType flightDescriptor);

      @JsProperty
      void setSchema(FieldType.ToObjectReturnType.TableFieldType.GetSchemaUnionType schema);

      @JsOverlay
      default void setSchema(String schema) {
        setSchema(
            Js.<FieldType.ToObjectReturnType.TableFieldType.GetSchemaUnionType>uncheckedCast(
                schema));
      }

      @JsOverlay
      default void setSchema(Uint8Array schema) {
        setSchema(
            Js.<FieldType.ToObjectReturnType.TableFieldType.GetSchemaUnionType>uncheckedCast(
                schema));
      }

      @JsProperty
      void setTotalBytes(double totalBytes);

      @JsProperty
      void setTotalRecords(double totalRecords);
    }

    @JsOverlay
    static FieldType.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    Object getFigure();

    @JsProperty
    FieldType.ToObjectReturnType.OpaqueFieldType getOpaque();

    @JsProperty
    FieldType.ToObjectReturnType.TableFieldType getTable();

    @JsProperty
    void setFigure(Object figure);

    @JsProperty
    void setOpaque(FieldType.ToObjectReturnType.OpaqueFieldType opaque);

    @JsProperty
    void setTable(FieldType.ToObjectReturnType.TableFieldType table);
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface OpaqueFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetAppMetadataUnionType {
        @JsOverlay
        static FieldType.ToObjectReturnType0.OpaqueFieldType.GetAppMetadataUnionType of(Object o) {
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
      static FieldType.ToObjectReturnType0.OpaqueFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      FieldType.ToObjectReturnType0.OpaqueFieldType.GetAppMetadataUnionType getAppMetadata();

      @JsProperty
      void setAppMetadata(
          FieldType.ToObjectReturnType0.OpaqueFieldType.GetAppMetadataUnionType appMetadata);

      @JsOverlay
      default void setAppMetadata(String appMetadata) {
        setAppMetadata(
            Js.<FieldType.ToObjectReturnType0.OpaqueFieldType.GetAppMetadataUnionType>uncheckedCast(
                appMetadata));
      }

      @JsOverlay
      default void setAppMetadata(Uint8Array appMetadata) {
        setAppMetadata(
            Js.<FieldType.ToObjectReturnType0.OpaqueFieldType.GetAppMetadataUnionType>uncheckedCast(
                appMetadata));
      }
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface TableFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface EndpointListFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface LocationListFieldType {
          @JsOverlay
          static FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                  .LocationListFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          String getUri();

          @JsProperty
          void setUri(String uri);
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface TicketFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetTicketUnionType {
            @JsOverlay
            static FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                    .TicketFieldType.GetTicketUnionType
                of(Object o) {
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
          static FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType.TicketFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType.TicketFieldType
                  .GetTicketUnionType
              getTicket();

          @JsProperty
          void setTicket(
              FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType.TicketFieldType
                      .GetTicketUnionType
                  ticket);

          @JsOverlay
          default void setTicket(String ticket) {
            setTicket(
                Js
                    .<FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                            .TicketFieldType.GetTicketUnionType>
                        uncheckedCast(ticket));
          }

          @JsOverlay
          default void setTicket(Uint8Array ticket) {
            setTicket(
                Js
                    .<FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                            .TicketFieldType.GetTicketUnionType>
                        uncheckedCast(ticket));
          }
        }

        @JsOverlay
        static FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        JsArray<
                FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                    .LocationListFieldType>
            getLocationList();

        @JsProperty
        FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType.TicketFieldType
            getTicket();

        @JsProperty
        void setLocationList(
            JsArray<
                    FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                        .LocationListFieldType>
                locationList);

        @JsOverlay
        default void setLocationList(
            FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType.LocationListFieldType
                    []
                locationList) {
          setLocationList(
              Js
                  .<JsArray<
                          FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType
                              .LocationListFieldType>>
                      uncheckedCast(locationList));
        }

        @JsProperty
        void setTicket(
            FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType.TicketFieldType
                ticket);
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface FlightDescriptorFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetCmdUnionType {
          @JsOverlay
          static FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType
                  .GetCmdUnionType
              of(Object o) {
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
        static FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType.GetCmdUnionType
            getCmd();

        @JsProperty
        JsArray<String> getPathList();

        @JsProperty
        double getType();

        @JsProperty
        void setCmd(
            FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType.GetCmdUnionType
                cmd);

        @JsOverlay
        default void setCmd(String cmd) {
          setCmd(
              Js
                  .<FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType
                          .GetCmdUnionType>
                      uncheckedCast(cmd));
        }

        @JsOverlay
        default void setCmd(Uint8Array cmd) {
          setCmd(
              Js
                  .<FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType
                          .GetCmdUnionType>
                      uncheckedCast(cmd));
        }

        @JsProperty
        void setPathList(JsArray<String> pathList);

        @JsOverlay
        default void setPathList(String[] pathList) {
          setPathList(Js.<JsArray<String>>uncheckedCast(pathList));
        }

        @JsProperty
        void setType(double type);
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetSchemaUnionType {
        @JsOverlay
        static FieldType.ToObjectReturnType0.TableFieldType.GetSchemaUnionType of(Object o) {
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
      static FieldType.ToObjectReturnType0.TableFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      JsArray<FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType> getEndpointList();

      @JsProperty
      FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType getFlightDescriptor();

      @JsProperty
      FieldType.ToObjectReturnType0.TableFieldType.GetSchemaUnionType getSchema();

      @JsProperty
      double getTotalBytes();

      @JsProperty
      double getTotalRecords();

      @JsOverlay
      default void setEndpointList(
          FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType[] endpointList) {
        setEndpointList(
            Js
                .<JsArray<FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType>>
                    uncheckedCast(endpointList));
      }

      @JsProperty
      void setEndpointList(
          JsArray<FieldType.ToObjectReturnType0.TableFieldType.EndpointListFieldType> endpointList);

      @JsProperty
      void setFlightDescriptor(
          FieldType.ToObjectReturnType0.TableFieldType.FlightDescriptorFieldType flightDescriptor);

      @JsProperty
      void setSchema(FieldType.ToObjectReturnType0.TableFieldType.GetSchemaUnionType schema);

      @JsOverlay
      default void setSchema(String schema) {
        setSchema(
            Js.<FieldType.ToObjectReturnType0.TableFieldType.GetSchemaUnionType>uncheckedCast(
                schema));
      }

      @JsOverlay
      default void setSchema(Uint8Array schema) {
        setSchema(
            Js.<FieldType.ToObjectReturnType0.TableFieldType.GetSchemaUnionType>uncheckedCast(
                schema));
      }

      @JsProperty
      void setTotalBytes(double totalBytes);

      @JsProperty
      void setTotalRecords(double totalRecords);
    }

    @JsOverlay
    static FieldType.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    Object getFigure();

    @JsProperty
    FieldType.ToObjectReturnType0.OpaqueFieldType getOpaque();

    @JsProperty
    FieldType.ToObjectReturnType0.TableFieldType getTable();

    @JsProperty
    void setFigure(Object figure);

    @JsProperty
    void setOpaque(FieldType.ToObjectReturnType0.OpaqueFieldType opaque);

    @JsProperty
    void setTable(FieldType.ToObjectReturnType0.TableFieldType table);
  }

  public static native FieldType deserializeBinary(Uint8Array bytes);

  public static native FieldType deserializeBinaryFromReader(FieldType message, Object reader);

  public static native void serializeBinaryToWriter(FieldType message, Object writer);

  public static native FieldType.ToObjectReturnType toObject(
      boolean includeInstance, FieldType msg);

  public native void clearFigure();

  public native void clearOpaque();

  public native void clearTable();

  public native FieldCase getFieldCase();

  public native FigureInfo getFigure();

  public native OpaqueInfo getOpaque();

  public native FlightInfo getTable();

  public native boolean hasFigure();

  public native boolean hasOpaque();

  public native boolean hasTable();

  public native Uint8Array serializeBinary();

  public native void setFigure();

  public native void setFigure(FigureInfo value);

  public native void setOpaque();

  public native void setOpaque(OpaqueInfo value);

  public native void setTable();

  public native void setTable(FlightInfo value);

  public native FieldType.ToObjectReturnType0 toObject();

  public native FieldType.ToObjectReturnType0 toObject(boolean includeInstance);
}
