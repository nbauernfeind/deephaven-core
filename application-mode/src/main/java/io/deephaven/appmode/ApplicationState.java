//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.appmode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import io.deephaven.engine.liveness.LivenessReferent;
import io.deephaven.engine.liveness.LivenessScope;
import io.deephaven.engine.updategraph.DynamicNode;

public class ApplicationState extends LivenessScope {

    public interface Factory {

        static Iterable<Factory> loadFromServiceFactory() {
            return ServiceLoader.load(Factory.class);
        }

        ApplicationState create(Listener appStateListener);
    }

    public interface Listener {
        void onNewField(ApplicationState app, Field<?> field);

        void onRemoveField(ApplicationState app, Field<?> field);
    }

    private final Listener listener;
    private final String id;
    private final String name;
    private final Map<String, Field<?>> fields;

    public ApplicationState(Listener listener, String id, String name) {
        this.listener = Objects.requireNonNull(listener);
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.fields = new HashMap<>();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public synchronized int numFieldsExported() {
        return fields.size();
    }

    public synchronized List<Field<?>> listFields() {
        return new ArrayList<>(fields.values());
    }

    public synchronized void clearFields() {
        fields.forEach((name, field) -> listener.onRemoveField(this, field));
        fields.clear();
    }

    public synchronized <T> Field<T> getField(String name) {
        // noinspection unchecked
        return (Field<T>) fields.get(name);
    }

    public synchronized <T> void setField(String name, T value) {
        setField(StandardField.of(name, value));
    }

    public synchronized <T> void setField(String name, T value, String description) {
        setField(StandardField.of(name, value, description));
    }

    public synchronized void setField(Field<?> field) {
        // manage the new value before release the old value
        final Object newValue = field.value();
        if ((newValue instanceof LivenessReferent) && DynamicNode.notDynamicOrIsRefreshing(newValue)) {
            manage((LivenessReferent) newValue);
        }

        // remove and release the old value
        removeField(field.name());

        fields.put(field.name(), field);
        listener.onNewField(this, field);
    }

    public synchronized void setFields(Field<?>... fields) {
        setFields(Arrays.asList(fields));
    }

    public synchronized void setFields(Iterable<Field<?>> fields) {
        for (Field<?> field : fields) {
            setField(field);
        }
    }

    public synchronized void removeField(String name) {
        Field<?> field = fields.remove(name);
        if (field != null) {
            listener.onRemoveField(this, field);
            Object oldValue = field.value();
            if ((oldValue instanceof LivenessReferent) && DynamicNode.notDynamicOrIsRefreshing(oldValue)) {
                unmanage((LivenessReferent) oldValue);
            }
        }
    }

    public synchronized void removeFields(String... names) {
        removeFields(Arrays.asList(names));
    }

    public synchronized void removeFields(Iterable<String> names) {
        for (String name : names) {
            removeField(name);
        }
    }
}
