/**
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.engine.table.impl.perf;

import io.deephaven.base.verify.Assert;
import io.deephaven.engine.exceptions.CancellationException;
import io.deephaven.util.SafeCloseable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Query performance instrumentation implementation. Manages a hierarchy of {@link QueryPerformanceNugget} instances.
 * <p>
 * Many methods are synchronized to 1) support external abortion of query and 2) for scenarios where the query is
 * suspended and resumed on another thread.
 */
public class QueryPerformanceRecorderImpl implements QueryPerformanceRecorder {

    private final QueryPerformanceNugget queryNugget;
    private final QueryPerformanceNugget.Factory nuggetFactory;
    private final ArrayList<QueryPerformanceNugget> operationNuggets = new ArrayList<>();
    private final Deque<QueryPerformanceNugget> userNuggetStack = new ArrayDeque<>();

    private QueryState state = QueryState.NOT_STARTED;
    private volatile boolean hasSubQueries;
    private QueryPerformanceNugget catchAllNugget;

    /**
     * Constructs a QueryPerformanceRecorderImpl.
     *
     * @param description a description for the query
     * @param nuggetFactory the factory to use for creating new nuggets
     * @param parent the parent query if it exists
     */
    QueryPerformanceRecorderImpl(
            @NotNull final String description,
            @Nullable final QueryPerformanceRecorder parent,
            @NotNull final QueryPerformanceNugget.Factory nuggetFactory) {
        if (parent == null) {
            queryNugget = nuggetFactory.createForQuery(
                    QueryPerformanceRecorderState.QUERIES_PROCESSED.getAndIncrement(), description,
                    this::releaseNugget);
        } else {
            queryNugget = nuggetFactory.createForSubQuery(
                    parent.getQueryLevelPerformanceData(),
                    QueryPerformanceRecorderState.QUERIES_PROCESSED.getAndIncrement(), description,
                    this::releaseNugget);
        }
        this.nuggetFactory = nuggetFactory;
    }

    /**
     * Abort a query.
     */
    public synchronized void abortQuery() {
        if (state != QueryState.RUNNING) {
            return;
        }
        state = QueryState.INTERRUPTED;
        if (catchAllNugget != null) {
            stopCatchAll(true);
        } else {
            while (!userNuggetStack.isEmpty()) {
                userNuggetStack.peekLast().abort();
            }
        }
        queryNugget.abort();
    }

    /**
     * Return the query's current state
     *
     * @return the query's state or null if it isn't initialized yet
     */
    public synchronized QueryState getState() {
        return state;
    }

    @Override
    public synchronized SafeCloseable startQuery() {
        if (state != QueryState.NOT_STARTED) {
            throw new IllegalStateException("Can't resume a query that has already started");
        }
        queryNugget.markStartTime();
        return resumeInternal();
    }

    @Override
    public synchronized boolean endQuery() {
        if (state != QueryState.RUNNING) {
            // We only allow the query to be RUNNING or INTERRUPTED when we end it; else we are in an illegal state.
            Assert.eq(state, "state", QueryState.INTERRUPTED, "QueryState.INTERRUPTED");
            return false;
        }
        state = QueryState.FINISHED;
        suspendInternal();
        return queryNugget.done();
    }

    /**
     * Suspends a query.
     * <p>
     * This resets the thread local and assumes that this performance nugget may be resumed on another thread.
     */
    public synchronized void suspendQuery() {
        if (state != QueryState.RUNNING) {
            throw new IllegalStateException("Can't suspend a query that isn't running");
        }
        state = QueryState.SUSPENDED;
        suspendInternal();
        queryNugget.onBaseEntryEnd();
    }

    private void suspendInternal() {
        final QueryPerformanceRecorder threadLocalInstance = QueryPerformanceRecorderState.getInstance();
        if (threadLocalInstance != this) {
            throw new IllegalStateException("Can't suspend a query that doesn't belong to this thread");
        }

        Assert.neqNull(catchAllNugget, "catchAllNugget");
        stopCatchAll(false);

        // uninstall this instance from the thread local
        QueryPerformanceRecorderState.resetInstance();
    }

    /**
     * Resumes a suspend query.
     * <p>
     * It is an error to resume a query while another query is running on this thread.
     *
     * @return this
     */
    public synchronized SafeCloseable resumeQuery() {
        if (state != QueryState.SUSPENDED) {
            throw new IllegalStateException("Can't resume a query that isn't suspended");
        }

        return resumeInternal();
    }

    private SafeCloseable resumeInternal() {
        final QueryPerformanceRecorder threadLocalInstance = QueryPerformanceRecorderState.getInstance();
        if (threadLocalInstance != QueryPerformanceRecorderState.DUMMY_RECORDER) {
            throw new IllegalStateException("Can't resume a query while another query is in operation");
        }
        QueryPerformanceRecorderState.THE_LOCAL.set(this);

        queryNugget.onBaseEntryStart();
        state = QueryState.RUNNING;
        Assert.eqNull(catchAllNugget, "catchAllNugget");
        startCatchAll();

        return QueryPerformanceRecorderState::resetInstance;
    }

    private void startCatchAll() {
        catchAllNugget = nuggetFactory.createForCatchAll(queryNugget, operationNuggets.size(), this::releaseNugget);
        catchAllNugget.markStartTime();
        catchAllNugget.onBaseEntryStart();
    }

    private void stopCatchAll(final boolean abort) {
        final boolean shouldLog;
        if (abort) {
            shouldLog = catchAllNugget.abort();
        } else {
            shouldLog = catchAllNugget.done();
        }
        if (shouldLog) {
            Assert.eq(operationNuggets.size(), "operationsNuggets.size()",
                    catchAllNugget.getOperationNumber(), "catchAllNugget.getOperationNumber()");
            operationNuggets.add(catchAllNugget);
        }
        catchAllNugget = null;
    }

    /**
     * @param name the nugget name
     * @param inputSize the nugget's input size
     * @return A new QueryPerformanceNugget to encapsulate user query operations. done() must be called on the nugget.
     */
    public synchronized QueryPerformanceNugget getNugget(@NotNull final String name, final long inputSize) {
        Assert.eq(state, "state", QueryState.RUNNING, "QueryState.RUNNING");
        if (Thread.interrupted()) {
            throw new CancellationException("interrupted in QueryPerformanceNugget");
        }
        if (catchAllNugget != null) {
            stopCatchAll(false);
        }

        final QueryPerformanceNugget parent;
        if (userNuggetStack.isEmpty()) {
            parent = queryNugget;
        } else {
            parent = userNuggetStack.peekLast();
            parent.onBaseEntryEnd();
        }

        final QueryPerformanceNugget nugget = nuggetFactory.createForOperation(
                parent, operationNuggets.size(), name, inputSize, this::releaseNugget);
        nugget.markStartTime();
        nugget.onBaseEntryStart();
        operationNuggets.add(nugget);
        userNuggetStack.addLast(nugget);
        return nugget;
    }

    /**
     * This is our onCloseCallback from the nugget.
     *
     * @param nugget the nugget to be released
     * @return If the nugget passes criteria for logging.
     */
    private synchronized boolean releaseNugget(@NotNull final QueryPerformanceNugget nugget) {
        boolean shouldLog = nugget.shouldLogNugget(nugget == catchAllNugget);
        if (!nugget.isUser()) {
            return shouldLog;
        }

        final QueryPerformanceNugget removed = userNuggetStack.removeLast();
        if (nugget != removed) {
            throw new IllegalStateException(
                    "Released query performance nugget " + nugget + " (" + System.identityHashCode(nugget) +
                            ") didn't match the top of the user nugget stack " + removed + " ("
                            + System.identityHashCode(removed) +
                            ") - did you follow the correct try/finally pattern?");
        }

        // accumulate into the parent and resume it
        if (!userNuggetStack.isEmpty()) {
            final QueryPerformanceNugget parent = userNuggetStack.getLast();
            parent.accumulate(nugget);

            if (removed.shouldLogThisAndStackParents()) {
                parent.setShouldLogThisAndStackParents();
            }

            // resume the parent
            parent.onBaseEntryStart();
        }

        if (!shouldLog) {
            // If we have filtered this nugget, by our filter design we will also have filtered any nuggets it encloses.
            // This means it *must* be the last entry in operationNuggets, so we can safely remove it in O(1).
            final QueryPerformanceNugget lastNugget = operationNuggets.remove(operationNuggets.size() - 1);
            if (nugget != lastNugget) {
                throw new IllegalStateException(
                        "Filtered query performance nugget " + nugget + " (" + System.identityHashCode(nugget) +
                                ") didn't match the last operation nugget " + lastNugget + " ("
                                + System.identityHashCode(lastNugget) +
                                ")");
            }
        }

        if (userNuggetStack.isEmpty() && queryNugget != null && state == QueryState.RUNNING) {
            startCatchAll();
        }

        return shouldLog;
    }

    @Override
    public synchronized QueryPerformanceNugget getEnclosingNugget() {
        if (userNuggetStack.isEmpty()) {
            Assert.neqNull(catchAllNugget, "catchAllNugget");
            return catchAllNugget;
        }
        return userNuggetStack.peekLast();
    }

    @Override
    public void setQueryData(final EntrySetter setter) {
        final long evaluationNumber;
        final int operationNumber;
        boolean uninstrumented = false;
        synchronized (this) {
            // we should never be called if we're not running
            Assert.eq(state, "state", QueryState.RUNNING, "QueryState.RUNNING");
            evaluationNumber = queryNugget.getEvaluationNumber();
            operationNumber = operationNuggets.size();
            if (operationNumber > 0) {
                // ensure UPL and QOPL are consistent/joinable.
                if (!userNuggetStack.isEmpty()) {
                    userNuggetStack.getLast().setShouldLogThisAndStackParents();
                } else {
                    uninstrumented = true;
                    Assert.neqNull(catchAllNugget, "catchAllNugget");
                    catchAllNugget.setShouldLogThisAndStackParents();
                }
            }
        }
        setter.set(evaluationNumber, operationNumber, uninstrumented);
    }

    @Override
    public QueryPerformanceNugget getQueryLevelPerformanceData() {
        return queryNugget;
    }

    @Override
    public List<QueryPerformanceNugget> getOperationLevelPerformanceData() {
        return operationNuggets;
    }

    @Override
    public void accumulate(@NotNull final QueryPerformanceRecorder subQuery) {
        hasSubQueries = true;
        queryNugget.accumulate(subQuery.getQueryLevelPerformanceData());
    }

    @Override
    public boolean hasSubQueries() {
        return hasSubQueries;
    }
}
