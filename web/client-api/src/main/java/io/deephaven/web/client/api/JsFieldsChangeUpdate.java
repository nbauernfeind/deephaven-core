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
    private JsArray<JsFieldDefinition> modifiedFields;

    @JsConstructor
    public JsFieldsChangeUpdate() {
    }

    @JsIgnore
    public JsFieldsChangeUpdate(JsArray<JsFieldDefinition> newFields, JsArray<JsFieldDefinition> removedFields, JsArray<JsFieldDefinition> modifiedFields) {
        this();
        this.newFields = newFields;
        this.removedFields = removedFields;
        this.modifiedFields = modifiedFields;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getNewFields() {
        return newFields;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getRemovedFields() {
        return removedFields;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getModifiedFields() {
        return modifiedFields;
    }
}
