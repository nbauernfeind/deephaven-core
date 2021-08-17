package io.deephaven.db.appmode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplicationState {

    public interface Factory {
        ApplicationState create();
    }

    private final String id;
    private final String name;
    private final Map<String, Field<?>> fields;
    // todo: listener on updates

    public ApplicationState(String id, String name) {
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
        fields.clear();
    }

    public synchronized <T> Field<T> getField(String name) {
        //noinspection unchecked
        return (Field<T>) fields.get(name);
    }

    public synchronized <T> void setField(String name, T value) {
        setField(Field.of(name, value));
    }

    public synchronized <T> void setField(String name, T value, String description) {
        setField(Field.of(name, value, description));
    }

    public synchronized void setField(Field<?> field) {
        fields.put(field.name(), field);
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
        fields.remove(name);
    }

    public synchronized void removeFields(String... names) {
        removeFields(Arrays.asList(names));
    }

    public synchronized void removeFields(Iterable<String> names) {
        for (String name : names) {
            removeField(name);
        }
    }

    public void shutdown() {
        // todo: listener
    }
}
