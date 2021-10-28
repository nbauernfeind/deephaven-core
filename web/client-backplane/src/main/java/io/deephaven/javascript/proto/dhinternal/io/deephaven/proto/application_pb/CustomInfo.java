package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb;

import elemental2.core.JsArray;
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
        name = "dhinternal.io.deephaven.proto.application_pb.CustomInfo",
        namespace = JsPackage.GLOBAL)
public class CustomInfo {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface GetDataUnionType {
        @JsOverlay
        static CustomInfo.GetDataUnionType of(Object o) {
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
    public interface SetDataValueUnionType {
        @JsOverlay
        static CustomInfo.SetDataValueUnionType of(Object o) {
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
        public interface GetDataUnionType {
            @JsOverlay
            static CustomInfo.ToObjectReturnType.GetDataUnionType of(Object o) {
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
                static CustomInfo.ToObjectReturnType.NestedTicketsListFieldType.GetTicketUnionType of(
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
            static CustomInfo.ToObjectReturnType.NestedTicketsListFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            CustomInfo.ToObjectReturnType.NestedTicketsListFieldType.GetTicketUnionType getTicket();

            @JsProperty
            void setTicket(
                    CustomInfo.ToObjectReturnType.NestedTicketsListFieldType.GetTicketUnionType ticket);

            @JsOverlay
            default void setTicket(String ticket) {
                setTicket(
                        Js.<CustomInfo.ToObjectReturnType.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }

            @JsOverlay
            default void setTicket(Uint8Array ticket) {
                setTicket(
                        Js.<CustomInfo.ToObjectReturnType.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }
        }

        @JsOverlay
        static CustomInfo.ToObjectReturnType create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        CustomInfo.ToObjectReturnType.GetDataUnionType getData();

        @JsProperty
        JsArray<CustomInfo.ToObjectReturnType.NestedTicketsListFieldType> getNestedTicketsList();

        @JsProperty
        String getType();

        @JsProperty
        void setData(CustomInfo.ToObjectReturnType.GetDataUnionType data);

        @JsOverlay
        default void setData(String data) {
            setData(Js.<CustomInfo.ToObjectReturnType.GetDataUnionType>uncheckedCast(data));
        }

        @JsOverlay
        default void setData(Uint8Array data) {
            setData(Js.<CustomInfo.ToObjectReturnType.GetDataUnionType>uncheckedCast(data));
        }

        @JsProperty
        void setNestedTicketsList(
                JsArray<CustomInfo.ToObjectReturnType.NestedTicketsListFieldType> nestedTicketsList);

        @JsOverlay
        default void setNestedTicketsList(
                CustomInfo.ToObjectReturnType.NestedTicketsListFieldType[] nestedTicketsList) {
            setNestedTicketsList(
                    Js.<JsArray<CustomInfo.ToObjectReturnType.NestedTicketsListFieldType>>uncheckedCast(
                            nestedTicketsList));
        }

        @JsProperty
        void setType(String type);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType0 {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface GetDataUnionType {
            @JsOverlay
            static CustomInfo.ToObjectReturnType0.GetDataUnionType of(Object o) {
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
                static CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType.GetTicketUnionType of(
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
            static CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType.GetTicketUnionType getTicket();

            @JsProperty
            void setTicket(
                    CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType.GetTicketUnionType ticket);

            @JsOverlay
            default void setTicket(String ticket) {
                setTicket(
                        Js.<CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }

            @JsOverlay
            default void setTicket(Uint8Array ticket) {
                setTicket(
                        Js.<CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }
        }

        @JsOverlay
        static CustomInfo.ToObjectReturnType0 create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        CustomInfo.ToObjectReturnType0.GetDataUnionType getData();

        @JsProperty
        JsArray<CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType> getNestedTicketsList();

        @JsProperty
        String getType();

        @JsProperty
        void setData(CustomInfo.ToObjectReturnType0.GetDataUnionType data);

        @JsOverlay
        default void setData(String data) {
            setData(Js.<CustomInfo.ToObjectReturnType0.GetDataUnionType>uncheckedCast(data));
        }

        @JsOverlay
        default void setData(Uint8Array data) {
            setData(Js.<CustomInfo.ToObjectReturnType0.GetDataUnionType>uncheckedCast(data));
        }

        @JsProperty
        void setNestedTicketsList(
                JsArray<CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType> nestedTicketsList);

        @JsOverlay
        default void setNestedTicketsList(
                CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType[] nestedTicketsList) {
            setNestedTicketsList(
                    Js.<JsArray<CustomInfo.ToObjectReturnType0.NestedTicketsListFieldType>>uncheckedCast(
                            nestedTicketsList));
        }

        @JsProperty
        void setType(String type);
    }

    public static native CustomInfo deserializeBinary(Uint8Array bytes);

    public static native CustomInfo deserializeBinaryFromReader(CustomInfo message, Object reader);

    public static native void serializeBinaryToWriter(CustomInfo message, Object writer);

    public static native CustomInfo.ToObjectReturnType toObject(
            boolean includeInstance, CustomInfo msg);

    public native Ticket addNestedTickets();

    public native Ticket addNestedTickets(Ticket value, double index);

    public native Ticket addNestedTickets(Ticket value);

    public native void clearNestedTicketsList();

    public native CustomInfo.GetDataUnionType getData();

    public native String getData_asB64();

    public native Uint8Array getData_asU8();

    public native JsArray<Ticket> getNestedTicketsList();

    public native String getType();

    public native Uint8Array serializeBinary();

    public native void setData(CustomInfo.SetDataValueUnionType value);

    @JsOverlay
    public final void setData(String value) {
        setData(Js.<CustomInfo.SetDataValueUnionType>uncheckedCast(value));
    }

    @JsOverlay
    public final void setData(Uint8Array value) {
        setData(Js.<CustomInfo.SetDataValueUnionType>uncheckedCast(value));
    }

    public native void setNestedTicketsList(JsArray<Ticket> value);

    @JsOverlay
    public final void setNestedTicketsList(Ticket[] value) {
        setNestedTicketsList(Js.<JsArray<Ticket>>uncheckedCast(value));
    }

    public native void setType(String value);

    public native CustomInfo.ToObjectReturnType0 toObject();

    public native CustomInfo.ToObjectReturnType0 toObject(boolean includeInstance);
}
