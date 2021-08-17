package io.deephaven.web.client.api;

import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb.FieldInfo;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.application_pb.fieldinfo.FieldType;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.ticket_pb.Ticket;
import io.deephaven.web.client.api.console.JsVariableDefinition;
import io.deephaven.web.shared.ide.VariableType;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.io.Serializable;

@JsType(namespace = "dh")
public class JsFieldDefinition implements Serializable {
    private Ticket ticket;
    private String fieldDescription;
    private JsVariableDefinition definition;

    @JsConstructor
    public JsFieldDefinition() {
    }

    @JsIgnore
    public JsFieldDefinition(FieldInfo fi) {
        this();
        this.ticket = fi.getTicket();
        this.definition = new JsVariableDefinition(fi.getFieldName(), getFieldTypeFor(fi.getFieldType()).toString());
        this.fieldDescription = fi.getFieldDescription();
    }

    @JsProperty
    public Ticket getTicket() {
        return ticket;
    }

    @JsProperty
    public JsVariableDefinition getDefinition() {
        return definition;
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
