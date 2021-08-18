package io.deephaven.web.client.api;

import elemental2.core.JsArray;
import elemental2.core.JsObject;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(namespace = "dh", name = "FieldsChangeUpdate")
public class JsFieldsChangeUpdate {
    private final JsArray<JsFieldDefinition> created;
    private final JsArray<JsFieldDefinition> removed;
    private final JsArray<JsFieldDefinition> updated;

    @JsIgnore
    public JsFieldsChangeUpdate(JsArray<JsFieldDefinition> created, JsArray<JsFieldDefinition> removed, JsArray<JsFieldDefinition> updated) {
        this.created = JsObject.freeze(created);
        this.removed = JsObject.freeze(removed);
        this.updated = JsObject.freeze(updated);
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
