package io.deephaven.web.client.api;

import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.FieldInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.field_pb.fieldinfo.FieldType;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.ticket_pb.Ticket;
import io.deephaven.web.shared.ide.VariableType;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.io.Serializable;

@JsType(namespace = "dh")
public class JsFieldDefinition implements Serializable {
    private Ticket ticket;
    private String fieldName;
    private VariableType fieldType;
    private String fieldDescription;

    @JsConstructor
    public JsFieldDefinition() {
    }

    @JsIgnore
    public JsFieldDefinition(FieldInfo fi) {
        this();
        this.ticket = fi.getTicket();
        this.fieldName = fi.getFieldName();
        this.fieldType = getFieldTypeFor(fi.getFieldType());
        this.fieldDescription = fi.getFieldDescription();
    }

    @JsProperty
    public Ticket getTicket() {
        return ticket;
    }

    @JsProperty
    public String getFieldName() {
        return fieldName;
    }

    @JsProperty
    public VariableType getFieldType() {
        return fieldType;
    }

    @JsProperty
    public String getFieldDescription() {
        return fieldDescription;
    }

    private static VariableType getFieldTypeFor(FieldType ft) {
        if (ft.hasTable()) {
            return VariableType.Table;
        }
        if (ft.hasFigure()) {
            return VariableType.Figure;
        }
        return VariableType.OtherWidget;
    }
}
