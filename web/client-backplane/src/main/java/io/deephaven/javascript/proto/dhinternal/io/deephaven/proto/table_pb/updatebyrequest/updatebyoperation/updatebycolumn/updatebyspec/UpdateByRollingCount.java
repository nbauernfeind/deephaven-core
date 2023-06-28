package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.table_pb.updatebyrequest.updatebyoperation.updatebycolumn.updatebyspec;

import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.table_pb.UpdateByWindowScale;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
        isNative = true,
        name = "dhinternal.io.deephaven.proto.table_pb.UpdateByRequest.UpdateByOperation.UpdateByColumn.UpdateBySpec.UpdateByRollingCount",
        namespace = JsPackage.GLOBAL)
public class UpdateByRollingCount {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface ReverseWindowScaleFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface TicksFieldType {
                @JsOverlay
                static UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType.TicksFieldType create() {
                    return Js.uncheckedCast(JsPropertyMap.of());
                }

                @JsProperty
                double getTicks();

                @JsProperty
                void setTicks(double ticks);
            }

            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface TimeFieldType {
                @JsOverlay
                static UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType.TimeFieldType create() {
                    return Js.uncheckedCast(JsPropertyMap.of());
                }

                @JsProperty
                String getColumn();

                @JsProperty
                String getDurationString();

                @JsProperty
                String getNanos();

                @JsProperty
                void setColumn(String column);

                @JsProperty
                void setDurationString(String durationString);

                @JsProperty
                void setNanos(String nanos);
            }

            @JsOverlay
            static UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType.TicksFieldType getTicks();

            @JsProperty
            UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType.TimeFieldType getTime();

            @JsProperty
            void setTicks(
                    UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType.TicksFieldType ticks);

            @JsProperty
            void setTime(
                    UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType.TimeFieldType time);
        }

        @JsOverlay
        static UpdateByRollingCount.ToObjectReturnType create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        Object getForwardWindowScale();

        @JsProperty
        UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType getReverseWindowScale();

        @JsProperty
        void setForwardWindowScale(Object forwardWindowScale);

        @JsProperty
        void setReverseWindowScale(
                UpdateByRollingCount.ToObjectReturnType.ReverseWindowScaleFieldType reverseWindowScale);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType0 {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface ReverseWindowScaleFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface TicksFieldType {
                @JsOverlay
                static UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType.TicksFieldType create() {
                    return Js.uncheckedCast(JsPropertyMap.of());
                }

                @JsProperty
                double getTicks();

                @JsProperty
                void setTicks(double ticks);
            }

            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface TimeFieldType {
                @JsOverlay
                static UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType.TimeFieldType create() {
                    return Js.uncheckedCast(JsPropertyMap.of());
                }

                @JsProperty
                String getColumn();

                @JsProperty
                String getDurationString();

                @JsProperty
                String getNanos();

                @JsProperty
                void setColumn(String column);

                @JsProperty
                void setDurationString(String durationString);

                @JsProperty
                void setNanos(String nanos);
            }

            @JsOverlay
            static UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType.TicksFieldType getTicks();

            @JsProperty
            UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType.TimeFieldType getTime();

            @JsProperty
            void setTicks(
                    UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType.TicksFieldType ticks);

            @JsProperty
            void setTime(
                    UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType.TimeFieldType time);
        }

        @JsOverlay
        static UpdateByRollingCount.ToObjectReturnType0 create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        Object getForwardWindowScale();

        @JsProperty
        UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType getReverseWindowScale();

        @JsProperty
        void setForwardWindowScale(Object forwardWindowScale);

        @JsProperty
        void setReverseWindowScale(
                UpdateByRollingCount.ToObjectReturnType0.ReverseWindowScaleFieldType reverseWindowScale);
    }

    public static native UpdateByRollingCount deserializeBinary(Uint8Array bytes);

    public static native UpdateByRollingCount deserializeBinaryFromReader(
            UpdateByRollingCount message, Object reader);

    public static native void serializeBinaryToWriter(UpdateByRollingCount message, Object writer);

    public static native UpdateByRollingCount.ToObjectReturnType toObject(
            boolean includeInstance, UpdateByRollingCount msg);

    public native void clearForwardWindowScale();

    public native void clearReverseWindowScale();

    public native UpdateByWindowScale getForwardWindowScale();

    public native UpdateByWindowScale getReverseWindowScale();

    public native boolean hasForwardWindowScale();

    public native boolean hasReverseWindowScale();

    public native Uint8Array serializeBinary();

    public native void setForwardWindowScale();

    public native void setForwardWindowScale(UpdateByWindowScale value);

    public native void setReverseWindowScale();

    public native void setReverseWindowScale(UpdateByWindowScale value);

    public native UpdateByRollingCount.ToObjectReturnType0 toObject();

    public native UpdateByRollingCount.ToObjectReturnType0 toObject(boolean includeInstance);
}
