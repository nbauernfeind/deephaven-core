package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb;

import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.arrow.flight.protocol.flight_pb.Ticket;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.fieldinfo.FieldType;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.field_pb.FieldInfo",
    namespace = JsPackage.GLOBAL)
public class FieldInfo {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface FieldFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface OpaqueFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetAppMetadataUnionType {
          @JsOverlay
          static FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType.GetAppMetadataUnionType
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
        static FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType.GetAppMetadataUnionType
            getAppMetadata();

        @JsProperty
        void setAppMetadata(
            FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType.GetAppMetadataUnionType
                appMetadata);

        @JsOverlay
        default void setAppMetadata(String appMetadata) {
          setAppMetadata(
              Js
                  .<FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType
                          .GetAppMetadataUnionType>
                      uncheckedCast(appMetadata));
        }

        @JsOverlay
        default void setAppMetadata(Uint8Array appMetadata) {
          setAppMetadata(
              Js
                  .<FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType
                          .GetAppMetadataUnionType>
                      uncheckedCast(appMetadata));
        }
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface TableFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface EndpointListFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface LocationListFieldType {
            @JsOverlay
            static FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.EndpointListFieldType
                    .LocationListFieldType
                create() {
              return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            String getUri();

            @JsProperty
            void setUri(String uri);
          }

          @JsOverlay
          static FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.EndpointListFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          JsArray<
                  FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.EndpointListFieldType
                      .LocationListFieldType>
              getLocationList();

          @JsProperty
          Object getTicket();

          @JsProperty
          void setLocationList(
              JsArray<
                      FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                          .EndpointListFieldType.LocationListFieldType>
                  locationList);

          @JsOverlay
          default void setLocationList(
              FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.EndpointListFieldType
                          .LocationListFieldType
                      []
                  locationList) {
            setLocationList(
                Js
                    .<JsArray<
                            FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                                .EndpointListFieldType.LocationListFieldType>>
                        uncheckedCast(locationList));
          }

          @JsProperty
          void setTicket(Object ticket);
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface FlightDescriptorFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetCmdUnionType {
            @JsOverlay
            static FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                    .FlightDescriptorFieldType.GetCmdUnionType
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
          static FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                  .FlightDescriptorFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.FlightDescriptorFieldType
                  .GetCmdUnionType
              getCmd();

          @JsProperty
          JsArray<String> getPathList();

          @JsProperty
          double getType();

          @JsProperty
          void setCmd(
              FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.FlightDescriptorFieldType
                      .GetCmdUnionType
                  cmd);

          @JsOverlay
          default void setCmd(String cmd) {
            setCmd(
                Js
                    .<FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                            .FlightDescriptorFieldType.GetCmdUnionType>
                        uncheckedCast(cmd));
          }

          @JsOverlay
          default void setCmd(Uint8Array cmd) {
            setCmd(
                Js
                    .<FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                            .FlightDescriptorFieldType.GetCmdUnionType>
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
          static FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.GetSchemaUnionType of(
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
        static FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        JsArray<FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.EndpointListFieldType>
            getEndpointList();

        @JsProperty
        FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.FlightDescriptorFieldType
            getFlightDescriptor();

        @JsProperty
        FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.GetSchemaUnionType getSchema();

        @JsProperty
        double getTotalBytes();

        @JsProperty
        double getTotalRecords();

        @JsOverlay
        default void setEndpointList(
            FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.EndpointListFieldType[]
                endpointList) {
          setEndpointList(
              Js
                  .<JsArray<
                          FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                              .EndpointListFieldType>>
                      uncheckedCast(endpointList));
        }

        @JsProperty
        void setEndpointList(
            JsArray<
                    FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType
                        .EndpointListFieldType>
                endpointList);

        @JsProperty
        void setFlightDescriptor(
            FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.FlightDescriptorFieldType
                flightDescriptor);

        @JsProperty
        void setSchema(
            FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.GetSchemaUnionType schema);

        @JsOverlay
        default void setSchema(String schema) {
          setSchema(
              Js
                  .<FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.GetSchemaUnionType>
                      uncheckedCast(schema));
        }

        @JsOverlay
        default void setSchema(Uint8Array schema) {
          setSchema(
              Js
                  .<FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType.GetSchemaUnionType>
                      uncheckedCast(schema));
        }

        @JsProperty
        void setTotalBytes(double totalBytes);

        @JsProperty
        void setTotalRecords(double totalRecords);
      }

      @JsOverlay
      static FieldInfo.ToObjectReturnType.FieldFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      Object getFigure();

      @JsProperty
      FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType getOpaque();

      @JsProperty
      FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType getTable();

      @JsProperty
      void setFigure(Object figure);

      @JsProperty
      void setOpaque(FieldInfo.ToObjectReturnType.FieldFieldType.OpaqueFieldType opaque);

      @JsProperty
      void setTable(FieldInfo.ToObjectReturnType.FieldFieldType.TableFieldType table);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface TicketFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static FieldInfo.ToObjectReturnType.TicketFieldType.GetTicketUnionType of(Object o) {
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
      static FieldInfo.ToObjectReturnType.TicketFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      FieldInfo.ToObjectReturnType.TicketFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(FieldInfo.ToObjectReturnType.TicketFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js.<FieldInfo.ToObjectReturnType.TicketFieldType.GetTicketUnionType>uncheckedCast(
                ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js.<FieldInfo.ToObjectReturnType.TicketFieldType.GetTicketUnionType>uncheckedCast(
                ticket));
      }
    }

    @JsOverlay
    static FieldInfo.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    String getFieldDescription();

    @JsProperty
    String getFieldName();

    @JsProperty
    FieldInfo.ToObjectReturnType.FieldFieldType getFieldType();

    @JsProperty
    FieldInfo.ToObjectReturnType.TicketFieldType getTicket();

    @JsProperty
    void setFieldDescription(String fieldDescription);

    @JsProperty
    void setFieldName(String fieldName);

    @JsProperty
    void setFieldType(FieldInfo.ToObjectReturnType.FieldFieldType fieldType);

    @JsProperty
    void setTicket(FieldInfo.ToObjectReturnType.TicketFieldType ticket);
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface FieldFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface OpaqueFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetAppMetadataUnionType {
          @JsOverlay
          static FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType
                  .GetAppMetadataUnionType
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
        static FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType.GetAppMetadataUnionType
            getAppMetadata();

        @JsProperty
        void setAppMetadata(
            FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType.GetAppMetadataUnionType
                appMetadata);

        @JsOverlay
        default void setAppMetadata(String appMetadata) {
          setAppMetadata(
              Js
                  .<FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType
                          .GetAppMetadataUnionType>
                      uncheckedCast(appMetadata));
        }

        @JsOverlay
        default void setAppMetadata(Uint8Array appMetadata) {
          setAppMetadata(
              Js
                  .<FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType
                          .GetAppMetadataUnionType>
                      uncheckedCast(appMetadata));
        }
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface TableFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface EndpointListFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface LocationListFieldType {
            @JsOverlay
            static FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.EndpointListFieldType
                    .LocationListFieldType
                create() {
              return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            String getUri();

            @JsProperty
            void setUri(String uri);
          }

          @JsOverlay
          static FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.EndpointListFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          JsArray<
                  FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.EndpointListFieldType
                      .LocationListFieldType>
              getLocationList();

          @JsProperty
          Object getTicket();

          @JsProperty
          void setLocationList(
              JsArray<
                      FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                          .EndpointListFieldType.LocationListFieldType>
                  locationList);

          @JsOverlay
          default void setLocationList(
              FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.EndpointListFieldType
                          .LocationListFieldType
                      []
                  locationList) {
            setLocationList(
                Js
                    .<JsArray<
                            FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                                .EndpointListFieldType.LocationListFieldType>>
                        uncheckedCast(locationList));
          }

          @JsProperty
          void setTicket(Object ticket);
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface FlightDescriptorFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetCmdUnionType {
            @JsOverlay
            static FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                    .FlightDescriptorFieldType.GetCmdUnionType
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
          static FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                  .FlightDescriptorFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.FlightDescriptorFieldType
                  .GetCmdUnionType
              getCmd();

          @JsProperty
          JsArray<String> getPathList();

          @JsProperty
          double getType();

          @JsProperty
          void setCmd(
              FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.FlightDescriptorFieldType
                      .GetCmdUnionType
                  cmd);

          @JsOverlay
          default void setCmd(String cmd) {
            setCmd(
                Js
                    .<FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                            .FlightDescriptorFieldType.GetCmdUnionType>
                        uncheckedCast(cmd));
          }

          @JsOverlay
          default void setCmd(Uint8Array cmd) {
            setCmd(
                Js
                    .<FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                            .FlightDescriptorFieldType.GetCmdUnionType>
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
          static FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.GetSchemaUnionType of(
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
        static FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        JsArray<FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.EndpointListFieldType>
            getEndpointList();

        @JsProperty
        FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.FlightDescriptorFieldType
            getFlightDescriptor();

        @JsProperty
        FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.GetSchemaUnionType getSchema();

        @JsProperty
        double getTotalBytes();

        @JsProperty
        double getTotalRecords();

        @JsOverlay
        default void setEndpointList(
            FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.EndpointListFieldType[]
                endpointList) {
          setEndpointList(
              Js
                  .<JsArray<
                          FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                              .EndpointListFieldType>>
                      uncheckedCast(endpointList));
        }

        @JsProperty
        void setEndpointList(
            JsArray<
                    FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType
                        .EndpointListFieldType>
                endpointList);

        @JsProperty
        void setFlightDescriptor(
            FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.FlightDescriptorFieldType
                flightDescriptor);

        @JsProperty
        void setSchema(
            FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.GetSchemaUnionType schema);

        @JsOverlay
        default void setSchema(String schema) {
          setSchema(
              Js
                  .<FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.GetSchemaUnionType>
                      uncheckedCast(schema));
        }

        @JsOverlay
        default void setSchema(Uint8Array schema) {
          setSchema(
              Js
                  .<FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType.GetSchemaUnionType>
                      uncheckedCast(schema));
        }

        @JsProperty
        void setTotalBytes(double totalBytes);

        @JsProperty
        void setTotalRecords(double totalRecords);
      }

      @JsOverlay
      static FieldInfo.ToObjectReturnType0.FieldFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      Object getFigure();

      @JsProperty
      FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType getOpaque();

      @JsProperty
      FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType getTable();

      @JsProperty
      void setFigure(Object figure);

      @JsProperty
      void setOpaque(FieldInfo.ToObjectReturnType0.FieldFieldType.OpaqueFieldType opaque);

      @JsProperty
      void setTable(FieldInfo.ToObjectReturnType0.FieldFieldType.TableFieldType table);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface TicketFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface GetTicketUnionType {
        @JsOverlay
        static FieldInfo.ToObjectReturnType0.TicketFieldType.GetTicketUnionType of(Object o) {
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
      static FieldInfo.ToObjectReturnType0.TicketFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      FieldInfo.ToObjectReturnType0.TicketFieldType.GetTicketUnionType getTicket();

      @JsProperty
      void setTicket(FieldInfo.ToObjectReturnType0.TicketFieldType.GetTicketUnionType ticket);

      @JsOverlay
      default void setTicket(String ticket) {
        setTicket(
            Js.<FieldInfo.ToObjectReturnType0.TicketFieldType.GetTicketUnionType>uncheckedCast(
                ticket));
      }

      @JsOverlay
      default void setTicket(Uint8Array ticket) {
        setTicket(
            Js.<FieldInfo.ToObjectReturnType0.TicketFieldType.GetTicketUnionType>uncheckedCast(
                ticket));
      }
    }

    @JsOverlay
    static FieldInfo.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    String getFieldDescription();

    @JsProperty
    String getFieldName();

    @JsProperty
    FieldInfo.ToObjectReturnType0.FieldFieldType getFieldType();

    @JsProperty
    FieldInfo.ToObjectReturnType0.TicketFieldType getTicket();

    @JsProperty
    void setFieldDescription(String fieldDescription);

    @JsProperty
    void setFieldName(String fieldName);

    @JsProperty
    void setFieldType(FieldInfo.ToObjectReturnType0.FieldFieldType fieldType);

    @JsProperty
    void setTicket(FieldInfo.ToObjectReturnType0.TicketFieldType ticket);
  }

  public static native FieldInfo deserializeBinary(Uint8Array bytes);

  public static native FieldInfo deserializeBinaryFromReader(FieldInfo message, Object reader);

  public static native void serializeBinaryToWriter(FieldInfo message, Object writer);

  public static native FieldInfo.ToObjectReturnType toObject(
      boolean includeInstance, FieldInfo msg);

  public native void clearFieldType();

  public native void clearTicket();

  public native String getFieldDescription();

  public native String getFieldName();

  public native FieldType getFieldType();

  public native Ticket getTicket();

  public native boolean hasFieldType();

  public native boolean hasTicket();

  public native Uint8Array serializeBinary();

  public native void setFieldDescription(String value);

  public native void setFieldName(String value);

  public native void setFieldType();

  public native void setFieldType(FieldType value);

  public native void setTicket();

  public native void setTicket(Ticket value);

  public native FieldInfo.ToObjectReturnType0 toObject();

  public native FieldInfo.ToObjectReturnType0 toObject(boolean includeInstance);
}
