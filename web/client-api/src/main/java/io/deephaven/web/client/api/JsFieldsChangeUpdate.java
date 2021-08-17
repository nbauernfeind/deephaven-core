package io.deephaven.web.client.api;

import elemental2.core.JsArray;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import java.io.Serializable;

@JsType(namespace = "dh")
public class JsFieldsChangeUpdate implements Serializable {
    private JsArray<JsFieldDefinition> created;
    private JsArray<JsFieldDefinition> removed;
    private JsArray<JsFieldDefinition> updated;

    @JsConstructor
    public JsFieldsChangeUpdate() {
    }

    @JsIgnore
    public JsFieldsChangeUpdate(JsArray<JsFieldDefinition> created, JsArray<JsFieldDefinition> removed, JsArray<JsFieldDefinition> updated) {
        this();
        this.created = created;
        this.removed = removed;
        this.updated = updated;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getCreated() {
        return created;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getRemoved() {
        return removed;
    }

    @JsProperty
    public JsArray<JsFieldDefinition> getUpdated() {
        return updated;
    }
}
