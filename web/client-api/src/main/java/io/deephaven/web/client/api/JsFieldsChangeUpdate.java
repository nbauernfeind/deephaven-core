package io.deephaven.web.client.api;

import elemental2.core.JsArray;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.io.Serializable;

@JsType(namespace = "dh")
public class JsFieldsChangeUpdate implements Serializable {
    private JsArray<JsFieldDefinition> newFields;
    private JsArray<JsFieldDefinition> removedFields;

    @JsConstructor
    public JsFieldsChangeUpdate() {
    }

    @JsIgnore
    public JsFieldsChangeUpdate(JsArray<JsFieldDefinition> newFields, JsArray<JsFieldDefinition> removedFields) {
        this.newFields = newFields;
        this.removedFields = removedFields;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getNewFields() {
        return newFields;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getRemovedFields() {
        return removedFields;
    }
}
