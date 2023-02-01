package io.deephaven.server.util;

import io.deephaven.configuration.Configuration;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.logger.Logger;
import io.deephaven.time.DateTimeUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class UpdatePropagationHelper implements Runnable {
    private static final boolean DEBUG =
            Configuration.getInstance().getBooleanForClassWithDefault(UpdatePropagationHelper.class, "debug", false);

    private static final Logger log = LoggerFactory.getLogger(UpdatePropagationHelper.class);

    private final ReentrantLock runLock = new ReentrantLock();
    private final AtomicBoolean needsRun = new AtomicBoolean();

    private final String logPrefix;
    private final Scheduler scheduler;

    private final Runnable onRun;
    private final Consumer<Exception> onError;
    private final long updateIntervalMs;

    private volatile long lastUpdateTime = 0;
    private volatile long lastScheduledUpdateTime = 0;

    public UpdatePropagationHelper(
            final Scheduler scheduler, final Runnable onRun, final Consumer<Exception> onError,
            final long updateIntervalMs) {
        this.logPrefix = "BarrageMessageProducer(" + Integer.toHexString(System.identityHashCode(this)) + "): ";
        this.scheduler = scheduler;
        this.onRun = onRun;
        this.onError = onError;
        this.updateIntervalMs = updateIntervalMs;
    }

    @Override
    public void run() {
        needsRun.set(true);
        while (true) {
            if (!runLock.tryLock()) {
                // if we can't get a lock, the thread that lets it go will check before exiting the method
                return;
            }

            try {
                if (needsRun.compareAndSet(true, false)) {
                    lastUpdateTime = scheduler.currentTimeMillis();
                    if (DEBUG) {
                        log.info().append(logPrefix).append("Starting update job at " + lastUpdateTime).endl();
                    }

                    onRun.run();

                    lastUpdateTime = scheduler.currentTimeMillis();
                    if (DEBUG) {
                        log.info().append(logPrefix).append("Completed Propagation: " + lastUpdateTime);
                    }
                }
            } catch (final Exception exception) {
                onError.accept(exception);
            } finally {
                runLock.unlock();
            }

            if (!needsRun.get()) {
                return;
            }
        }
    }

    public synchronized void schedulePropagation() {
        // copy lastUpdateTime so we are not duped by the re-read
        final long localLastUpdateTime = lastUpdateTime;
        final long now = scheduler.currentTimeMillis();
        final long msSinceLastUpdate = now - localLastUpdateTime;
        if (lastScheduledUpdateTime != 0 && lastScheduledUpdateTime > localLastUpdateTime) {
            // do nothing; update incoming
            if (DEBUG) {
                log.info().append(logPrefix)
                        .append("Not scheduling update, because last update was ").append(localLastUpdateTime)
                        .append(" and now is ").append(now).append(" msSinceLastUpdate=").append(msSinceLastUpdate)
                        .append(" interval=").append(updateIntervalMs).append(" already scheduled to run at ")
                        .append(lastScheduledUpdateTime).endl();
            }
        } else if (msSinceLastUpdate < localLastUpdateTime) {
            // we have updated within the period, so wait until a sufficient gap
            final long nextRunTime = localLastUpdateTime + updateIntervalMs;
            if (DEBUG) {
                log.info().append(logPrefix).append("Last Update Time: ").append(localLastUpdateTime)
                        .append(" next run: ")
                        .append(nextRunTime).endl();
            }
            lastScheduledUpdateTime = nextRunTime;
            scheduleAt(nextRunTime);
        } else {
            // we have not updated recently, so go for it right away
            if (DEBUG) {
                log.info().append(logPrefix)
                        .append("Scheduling update immediately, because last update was ").append(localLastUpdateTime)
                        .append(" and now is ").append(now).append(" msSinceLastUpdate=").append(msSinceLastUpdate)
                        .append(" interval=").append(updateIntervalMs).endl();
            }
            scheduleImmediately();
        }
    }

    public void scheduleImmediately() {
        if (needsRun.compareAndSet(false, true) && !runLock.isLocked()) {
            scheduler.runImmediately(this);
        }
    }

    public void scheduleAt(final long nextRunTimeMillis) {
        scheduler.runAtTime(nextRunTimeMillis, this);
    }
}
