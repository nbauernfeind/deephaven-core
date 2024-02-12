/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.engine.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.deephaven.UncheckedDeephavenException;
import io.deephaven.configuration.Configuration;
import io.deephaven.internal.log.LoggerFactory;
import org.jpy.PyDictWrapper;
import org.jpy.PyLib;
import org.jpy.PyObject;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class PythonScopeJpyImpl implements PythonScope<PyObject> {
    private static volatile boolean cacheEnabled =
            Configuration.getInstance().getBooleanForClassWithDefault(PythonScopeJpyImpl.class, "cacheEnabled", false);

    public static void setCacheEnabled(boolean enabled) {
        cacheEnabled = enabled;
    }

    private final PyDictWrapper dict;

    private static final ThreadLocal<Deque<PyDictWrapper>> threadScopeStack = new ThreadLocal<>();
    private static final Cache<PyObject, Object> conversionCache = CacheBuilder.newBuilder().weakValues().build();

    public static PythonScopeJpyImpl ofMainGlobals() {
        return new PythonScopeJpyImpl(PyLib.getMainGlobals().asDict());
    }

    public PythonScopeJpyImpl(PyDictWrapper dict) {
        this.dict = dict;
    }

    private PyDictWrapper currentScope() {
        Deque<PyDictWrapper> scopeStack = threadScopeStack.get();
        if (scopeStack == null || scopeStack.isEmpty()) {
            return this.dict;
        } else {
            return scopeStack.peek();
        }
    }

    @Override
    public Optional<PyObject> getValueRaw(String name) {
        // note: we *may* be returning Optional.of(None)
        // None is a valid PyObject, and can be in scope
        return Optional.ofNullable(currentScope().get(name));
    }

    @Override
    public Stream<PyObject> getKeysRaw() {
        return currentScope().keySet().stream();
    }

    @Override
    public Stream<Entry<PyObject, PyObject>> getEntriesRaw() {
        return currentScope().entrySet().stream();
    }

    @Override
    public boolean containsKey(String name) {
        return currentScope().containsKey(name);
    }

    @Override
    public String convertStringKey(PyObject key) {
        if (!key.isString()) {
            throw new IllegalArgumentException(
                    "Found non-string key! Expecting only string keys. " + key);
        }
        return key.toString();
    }

    @Override
    public Object convertValue(PyObject value) {
        PyLib.Diag.setFlags(PyLib.Diag.getFlags() | PyLib.Diag.F_EXEC);
        if (value.isNone()) {
            return value;
        }
        return convert(value);
    }

    /**
     * Converts a pyObject into an appropriate Java object for use outside of JPy.
     * <p>
     * If we're a List, Dictionary, or Callable, then we wrap them in a java object.
     * <p>
     * If it is a primitive (or a wrapped Java object); we convert it to the java object.
     * <p>
     * Otherwise we return the raw PyObject and the user can do with it what they will.
     *
     * @param pyObject the JPy wrapped PyObject.
     * @return a Java object representing the underlying JPy object.
     */
    public static Object convert(PyObject pyObject) {
        if (!cacheEnabled) {
            return convertInternal(pyObject, false);
        }

        try {
            final Object cached = conversionCache.get(pyObject, () -> convertInternal(pyObject, true));
            return cached instanceof NULL_TOKEN ? null : cached;
        } catch (ExecutionException err) {
            throw new UncheckedDeephavenException("Error converting PyObject to Java object", err);
        }
    }

    private static AtomicBoolean allowedToInit = new AtomicBoolean();
    private static Object convertInternal(PyObject pyObject, boolean fromCache) {
        Object ret = pyObject;
        if (pyObject.isList()) {
            ret = pyObject.asList();
        } else if (pyObject.isDict()) {
            ret = pyObject.asDict();
        } else if (pyObject.isCallable()) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                while (((Unsafe)f.get(null)).shouldBeInitialized(PyCallableWrapperJpyImpl.class)) {
                    boolean hasGil = PyLib.hasGil();
                    LoggerFactory.getLogger(PythonScopeJpyImpl.class).error().append("Thread ").append(Thread.currentThread().toString()).append(" has GIL: ").append(hasGil).endl();
                    if (hasGil && allowedToInit.compareAndSet(false, true)) {
                        synchronized (PythonScopeJpyImpl.class) {
                            PyLib.ensureGil(() -> {
                                PyCallableWrapperJpyImpl.init();
                                return null;
                            });
                        }
                    } else {
                        Thread.sleep(100);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            ret = new PyCallableWrapperJpyImpl(pyObject);
        } else if (pyObject.isConvertible()) {
            ret = pyObject.getObjectValue();
        }

        return ret == null && fromCache ? new NULL_TOKEN() : ret;
    }

    public PyDictWrapper mainGlobals() {
        return dict;
    }

    @Override
    public void pushScope(PyObject pydict) {
        Deque<PyDictWrapper> scopeStack = threadScopeStack.get();
        if (scopeStack == null) {
            scopeStack = new ArrayDeque<>();
            threadScopeStack.set(scopeStack);
        }
        scopeStack.push(pydict.asDict());
    }

    @Override
    public void popScope() {
        Deque<PyDictWrapper> scopeStack = threadScopeStack.get();
        if (scopeStack == null) {
            throw new IllegalStateException("The thread scope stack is empty.");
        }
        PyDictWrapper pydict = scopeStack.pop();
        pydict.close();
    }

    /**
     * Guava caches are not allowed to hold on to null values. Additionally, we can't use a singleton pattern or else
     * the weak-value map will never release null values.
     */
    private static class NULL_TOKEN {
    }
}
