package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb.fieldinfo;

import elemental2.core.JsArray;
import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb.CustomInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb.FigureInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb.TableInfo;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
        isNative = true,
        name = "dhinternal.io.deephaven.proto.application_pb.FieldInfo.FieldType",
        namespace = JsPackage.GLOBAL)
public class FieldType {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface CustomFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetDataUnionType {
                @JsOverlay
                static FieldType.ToObjectReturnType.CustomFieldType.GetDataUnionType of(Object o) {
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
            public interface NestedTicketsListFieldType {
                @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
                public interface GetTicketUnionType {
                    @JsOverlay
                    static FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType of(
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
                static FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType create() {
                    return Js.uncheckedCast(JsPropertyMap.of());
                }

                @JsProperty
                FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType getTicket();

                @JsProperty
                void setTicket(
                        FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType ticket);

                @JsOverlay
                default void setTicket(String ticket) {
                    setTicket(
                            Js.<FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                    ticket));
                }

                @JsOverlay
                default void setTicket(Uint8Array ticket) {
                    setTicket(
                            Js.<FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                    ticket));
                }
            }

            @JsOverlay
            static FieldType.ToObjectReturnType.CustomFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            FieldType.ToObjectReturnType.CustomFieldType.GetDataUnionType getData();

            @JsProperty
            JsArray<FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType> getNestedTicketsList();

            @JsProperty
            String getType();

            @JsProperty
            void setData(FieldType.ToObjectReturnType.CustomFieldType.GetDataUnionType data);

            @JsOverlay
            default void setData(String data) {
                setData(
                        Js.<FieldType.ToObjectReturnType.CustomFieldType.GetDataUnionType>uncheckedCast(data));
            }

            @JsOverlay
            default void setData(Uint8Array data) {
                setData(
                        Js.<FieldType.ToObjectReturnType.CustomFieldType.GetDataUnionType>uncheckedCast(data));
            }

            @JsProperty
            void setNestedTicketsList(
                    JsArray<FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType> nestedTicketsList);

            @JsOverlay
            default void setNestedTicketsList(
                    FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType[] nestedTicketsList) {
                setNestedTicketsList(
                        Js.<JsArray<FieldType.ToObjectReturnType.CustomFieldType.NestedTicketsListFieldType>>uncheckedCast(
                                nestedTicketsList));
            }

            @JsProperty
            void setType(String type);
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface TableFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetSchemaHeaderUnionType {
                @JsOverlay
                static FieldType.ToObjectReturnType.TableFieldType.GetSchemaHeaderUnionType of(Object o) {
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
            FieldType.ToObjectReturnType.TableFieldType.GetSchemaHeaderUnionType getSchemaHeader();

            @JsProperty
            String getSize();

            @JsProperty
            boolean isIsStatic();

            @JsProperty
            void setIsStatic(boolean isStatic);

            @JsProperty
            void setSchemaHeader(
                    FieldType.ToObjectReturnType.TableFieldType.GetSchemaHeaderUnionType schemaHeader);

            @JsOverlay
            default void setSchemaHeader(String schemaHeader) {
                setSchemaHeader(
                        Js.<FieldType.ToObjectReturnType.TableFieldType.GetSchemaHeaderUnionType>uncheckedCast(
                                schemaHeader));
            }

            @JsOverlay
            default void setSchemaHeader(Uint8Array schemaHeader) {
                setSchemaHeader(
                        Js.<FieldType.ToObjectReturnType.TableFieldType.GetSchemaHeaderUnionType>uncheckedCast(
                                schemaHeader));
            }

            @JsProperty
            void setSize(String size);
        }

        @JsOverlay
        static FieldType.ToObjectReturnType create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldType.ToObjectReturnType.CustomFieldType getCustom();

        @JsProperty
        Object getFigure();

        @JsProperty
        FieldType.ToObjectReturnType.TableFieldType getTable();

        @JsProperty
        void setCustom(FieldType.ToObjectReturnType.CustomFieldType custom);

        @JsProperty
        void setFigure(Object figure);

        @JsProperty
        void setTable(FieldType.ToObjectReturnType.TableFieldType table);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType0 {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface CustomFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetDataUnionType {
                @JsOverlay
                static FieldType.ToObjectReturnType0.CustomFieldType.GetDataUnionType of(Object o) {
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
            public interface NestedTicketsListFieldType {
                @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
                public interface GetTicketUnionType {
                    @JsOverlay
                    static FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType of(
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
                static FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType create() {
                    return Js.uncheckedCast(JsPropertyMap.of());
                }

                @JsProperty
                FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType getTicket();

                @JsProperty
                void setTicket(
                        FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType ticket);

                @JsOverlay
                default void setTicket(String ticket) {
                    setTicket(
                            Js.<FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                    ticket));
                }

                @JsOverlay
                default void setTicket(Uint8Array ticket) {
                    setTicket(
                            Js.<FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                    ticket));
                }
            }

            @JsOverlay
            static FieldType.ToObjectReturnType0.CustomFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            FieldType.ToObjectReturnType0.CustomFieldType.GetDataUnionType getData();

            @JsProperty
            JsArray<FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType> getNestedTicketsList();

            @JsProperty
            String getType();

            @JsProperty
            void setData(FieldType.ToObjectReturnType0.CustomFieldType.GetDataUnionType data);

            @JsOverlay
            default void setData(String data) {
                setData(
                        Js.<FieldType.ToObjectReturnType0.CustomFieldType.GetDataUnionType>uncheckedCast(data));
            }

            @JsOverlay
            default void setData(Uint8Array data) {
                setData(
                        Js.<FieldType.ToObjectReturnType0.CustomFieldType.GetDataUnionType>uncheckedCast(data));
            }

            @JsProperty
            void setNestedTicketsList(
                    JsArray<FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType> nestedTicketsList);

            @JsOverlay
            default void setNestedTicketsList(
                    FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType[] nestedTicketsList) {
                setNestedTicketsList(
                        Js.<JsArray<FieldType.ToObjectReturnType0.CustomFieldType.NestedTicketsListFieldType>>uncheckedCast(
                                nestedTicketsList));
            }

            @JsProperty
            void setType(String type);
        }

        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface TableFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetSchemaHeaderUnionType {
                @JsOverlay
                static FieldType.ToObjectReturnType0.TableFieldType.GetSchemaHeaderUnionType of(Object o) {
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
            FieldType.ToObjectReturnType0.TableFieldType.GetSchemaHeaderUnionType getSchemaHeader();

            @JsProperty
            String getSize();

            @JsProperty
            boolean isIsStatic();

            @JsProperty
            void setIsStatic(boolean isStatic);

            @JsProperty
            void setSchemaHeader(
                    FieldType.ToObjectReturnType0.TableFieldType.GetSchemaHeaderUnionType schemaHeader);

            @JsOverlay
            default void setSchemaHeader(String schemaHeader) {
                setSchemaHeader(
                        Js.<FieldType.ToObjectReturnType0.TableFieldType.GetSchemaHeaderUnionType>uncheckedCast(
                                schemaHeader));
            }

            @JsOverlay
            default void setSchemaHeader(Uint8Array schemaHeader) {
                setSchemaHeader(
                        Js.<FieldType.ToObjectReturnType0.TableFieldType.GetSchemaHeaderUnionType>uncheckedCast(
                                schemaHeader));
            }

            @JsProperty
            void setSize(String size);
        }

        @JsOverlay
        static FieldType.ToObjectReturnType0 create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FieldType.ToObjectReturnType0.CustomFieldType getCustom();

        @JsProperty
        Object getFigure();

        @JsProperty
        FieldType.ToObjectReturnType0.TableFieldType getTable();

        @JsProperty
        void setCustom(FieldType.ToObjectReturnType0.CustomFieldType custom);

        @JsProperty
        void setFigure(Object figure);

        @JsProperty
        void setTable(FieldType.ToObjectReturnType0.TableFieldType table);
    }

    public static native FieldType deserializeBinary(Uint8Array bytes);

    public static native FieldType deserializeBinaryFromReader(FieldType message, Object reader);

    public static native void serializeBinaryToWriter(FieldType message, Object writer);

    public static native FieldType.ToObjectReturnType toObject(
            boolean includeInstance, FieldType msg);

    public native void clearCustom();

    public native void clearFigure();

    public native void clearTable();

    public native CustomInfo getCustom();

    public native int getFieldCase();

    public native FigureInfo getFigure();

    public native TableInfo getTable();

    public native boolean hasCustom();

    public native boolean hasFigure();

    public native boolean hasTable();

    public native Uint8Array serializeBinary();

    public native void setCustom();

    public native void setCustom(CustomInfo value);

    public native void setFigure();

    public native void setFigure(FigureInfo value);

    public native void setTable();

    public native void setTable(TableInfo value);

    public native FieldType.ToObjectReturnType0 toObject();

    public native FieldType.ToObjectReturnType0 toObject(boolean includeInstance);
}
