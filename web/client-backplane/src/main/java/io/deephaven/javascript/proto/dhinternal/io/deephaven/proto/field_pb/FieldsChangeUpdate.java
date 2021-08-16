package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb;

import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
    isNative = true,
    name = "dhinternal.io.deephaven.proto.field_pb.FieldsChangeUpdate",
    namespace = JsPackage.GLOBAL)
public class FieldsChangeUpdate {
  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface FieldsListFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface FieldFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface OpaqueFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetAppMetadataUnionType {
            @JsOverlay
            static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                    .OpaqueFieldType.GetAppMetadataUnionType
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
          static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                  .OpaqueFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType.OpaqueFieldType
                  .GetAppMetadataUnionType
              getAppMetadata();

          @JsProperty
          void setAppMetadata(
              FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                      .OpaqueFieldType.GetAppMetadataUnionType
                  appMetadata);

          @JsOverlay
          default void setAppMetadata(String appMetadata) {
            setAppMetadata(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                            .OpaqueFieldType.GetAppMetadataUnionType>
                        uncheckedCast(appMetadata));
          }

          @JsOverlay
          default void setAppMetadata(Uint8Array appMetadata) {
            setAppMetadata(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                            .OpaqueFieldType.GetAppMetadataUnionType>
                        uncheckedCast(appMetadata));
          }
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface TableFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetSchemaHeaderUnionType {
            @JsOverlay
            static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                    .TableFieldType.GetSchemaHeaderUnionType
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
          static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                  .TableFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType.TableFieldType
                  .GetSchemaHeaderUnionType
              getSchemaHeader();

          @JsProperty
          String getSize();

          @JsProperty
          boolean isIsStatic();

          @JsProperty
          void setIsStatic(boolean isStatic);

          @JsProperty
          void setSchemaHeader(
              FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                      .TableFieldType.GetSchemaHeaderUnionType
                  schemaHeader);

          @JsOverlay
          default void setSchemaHeader(String schemaHeader) {
            setSchemaHeader(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                            .TableFieldType.GetSchemaHeaderUnionType>
                        uncheckedCast(schemaHeader));
          }

          @JsOverlay
          default void setSchemaHeader(Uint8Array schemaHeader) {
            setSchemaHeader(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType
                            .TableFieldType.GetSchemaHeaderUnionType>
                        uncheckedCast(schemaHeader));
          }

          @JsProperty
          void setSize(String size);
        }

        @JsOverlay
        static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        Object getFigure();

        @JsProperty
        FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType.OpaqueFieldType
            getOpaque();

        @JsProperty
        Object getRemoved();

        @JsProperty
        FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType.TableFieldType
            getTable();

        @JsProperty
        void setFigure(Object figure);

        @JsProperty
        void setOpaque(
            FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType.OpaqueFieldType
                opaque);

        @JsProperty
        void setRemoved(Object removed);

        @JsProperty
        void setTable(
            FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType.TableFieldType
                table);
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface TicketFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetTicketUnionType {
          @JsOverlay
          static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType
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
        static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType.GetTicketUnionType
            getTicket();

        @JsProperty
        void setTicket(
            FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType
                    .GetTicketUnionType
                ticket);

        @JsOverlay
        default void setTicket(String ticket) {
          setTicket(
              Js
                  .<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType
                          .GetTicketUnionType>
                      uncheckedCast(ticket));
        }

        @JsOverlay
        default void setTicket(Uint8Array ticket) {
          setTicket(
              Js
                  .<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType
                          .GetTicketUnionType>
                      uncheckedCast(ticket));
        }
      }

      @JsOverlay
      static FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      String getFieldDescription();

      @JsProperty
      String getFieldName();

      @JsProperty
      FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType getFieldType();

      @JsProperty
      FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType getTicket();

      @JsProperty
      void setFieldDescription(String fieldDescription);

      @JsProperty
      void setFieldName(String fieldName);

      @JsProperty
      void setFieldType(
          FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.FieldFieldType fieldType);

      @JsProperty
      void setTicket(
          FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType.TicketFieldType ticket);
    }

    @JsOverlay
    static FieldsChangeUpdate.ToObjectReturnType create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    JsArray<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType> getFieldsList();

    @JsOverlay
    default void setFieldsList(
        FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType[] fieldsList) {
      setFieldsList(
          Js.<JsArray<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType>>uncheckedCast(
              fieldsList));
    }

    @JsProperty
    void setFieldsList(
        JsArray<FieldsChangeUpdate.ToObjectReturnType.FieldsListFieldType> fieldsList);
  }

  @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
  public interface ToObjectReturnType0 {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface FieldsListFieldType {
      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface FieldFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface OpaqueFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetAppMetadataUnionType {
            @JsOverlay
            static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                    .OpaqueFieldType.GetAppMetadataUnionType
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
          static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                  .OpaqueFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType.OpaqueFieldType
                  .GetAppMetadataUnionType
              getAppMetadata();

          @JsProperty
          void setAppMetadata(
              FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                      .OpaqueFieldType.GetAppMetadataUnionType
                  appMetadata);

          @JsOverlay
          default void setAppMetadata(String appMetadata) {
            setAppMetadata(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                            .OpaqueFieldType.GetAppMetadataUnionType>
                        uncheckedCast(appMetadata));
          }

          @JsOverlay
          default void setAppMetadata(Uint8Array appMetadata) {
            setAppMetadata(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                            .OpaqueFieldType.GetAppMetadataUnionType>
                        uncheckedCast(appMetadata));
          }
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface TableFieldType {
          @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
          public interface GetSchemaHeaderUnionType {
            @JsOverlay
            static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                    .TableFieldType.GetSchemaHeaderUnionType
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
          static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                  .TableFieldType
              create() {
            return Js.uncheckedCast(JsPropertyMap.of());
          }

          @JsProperty
          FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType.TableFieldType
                  .GetSchemaHeaderUnionType
              getSchemaHeader();

          @JsProperty
          String getSize();

          @JsProperty
          boolean isIsStatic();

          @JsProperty
          void setIsStatic(boolean isStatic);

          @JsProperty
          void setSchemaHeader(
              FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                      .TableFieldType.GetSchemaHeaderUnionType
                  schemaHeader);

          @JsOverlay
          default void setSchemaHeader(String schemaHeader) {
            setSchemaHeader(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                            .TableFieldType.GetSchemaHeaderUnionType>
                        uncheckedCast(schemaHeader));
          }

          @JsOverlay
          default void setSchemaHeader(Uint8Array schemaHeader) {
            setSchemaHeader(
                Js
                    .<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                            .TableFieldType.GetSchemaHeaderUnionType>
                        uncheckedCast(schemaHeader));
          }

          @JsProperty
          void setSize(String size);
        }

        @JsOverlay
        static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        Object getFigure();

        @JsProperty
        FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType.OpaqueFieldType
            getOpaque();

        @JsProperty
        Object getRemoved();

        @JsProperty
        FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType.TableFieldType
            getTable();

        @JsProperty
        void setFigure(Object figure);

        @JsProperty
        void setOpaque(
            FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType
                    .OpaqueFieldType
                opaque);

        @JsProperty
        void setRemoved(Object removed);

        @JsProperty
        void setTable(
            FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType.TableFieldType
                table);
      }

      @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
      public interface TicketFieldType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetTicketUnionType {
          @JsOverlay
          static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType
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
        static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType create() {
          return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType
                .GetTicketUnionType
            getTicket();

        @JsProperty
        void setTicket(
            FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType
                    .GetTicketUnionType
                ticket);

        @JsOverlay
        default void setTicket(String ticket) {
          setTicket(
              Js
                  .<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType
                          .GetTicketUnionType>
                      uncheckedCast(ticket));
        }

        @JsOverlay
        default void setTicket(Uint8Array ticket) {
          setTicket(
              Js
                  .<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType
                          .GetTicketUnionType>
                      uncheckedCast(ticket));
        }
      }

      @JsOverlay
      static FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType create() {
        return Js.uncheckedCast(JsPropertyMap.of());
      }

      @JsProperty
      String getFieldDescription();

      @JsProperty
      String getFieldName();

      @JsProperty
      FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType getFieldType();

      @JsProperty
      FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType getTicket();

      @JsProperty
      void setFieldDescription(String fieldDescription);

      @JsProperty
      void setFieldName(String fieldName);

      @JsProperty
      void setFieldType(
          FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.FieldFieldType fieldType);

      @JsProperty
      void setTicket(
          FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType.TicketFieldType ticket);
    }

    @JsOverlay
    static FieldsChangeUpdate.ToObjectReturnType0 create() {
      return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsProperty
    JsArray<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType> getFieldsList();

    @JsOverlay
    default void setFieldsList(
        FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType[] fieldsList) {
      setFieldsList(
          Js.<JsArray<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType>>uncheckedCast(
              fieldsList));
    }

    @JsProperty
    void setFieldsList(
        JsArray<FieldsChangeUpdate.ToObjectReturnType0.FieldsListFieldType> fieldsList);
  }

  public static native FieldsChangeUpdate deserializeBinary(Uint8Array bytes);

  public static native FieldsChangeUpdate deserializeBinaryFromReader(
      FieldsChangeUpdate message, Object reader);

  public static native void serializeBinaryToWriter(FieldsChangeUpdate message, Object writer);

  public static native FieldsChangeUpdate.ToObjectReturnType toObject(
      boolean includeInstance, FieldsChangeUpdate msg);

  public native FieldInfo addFields();

  public native FieldInfo addFields(FieldInfo value, double index);

  public native FieldInfo addFields(FieldInfo value);

  public native void clearFieldsList();

  public native JsArray<FieldInfo> getFieldsList();

  public native Uint8Array serializeBinary();

  @JsOverlay
  public final void setFieldsList(FieldInfo[] value) {
    setFieldsList(Js.<JsArray<FieldInfo>>uncheckedCast(value));
  }

  public native void setFieldsList(JsArray<FieldInfo> value);

  public native FieldsChangeUpdate.ToObjectReturnType0 toObject();

  public native FieldsChangeUpdate.ToObjectReturnType0 toObject(boolean includeInstance);
}
