package org.transitclock.utils.threading;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.transitclock.utils.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtendedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public ExtendedScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public ExtendedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public ExtendedScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public ExtendedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        logger.trace("Runnable {} assigned to {}", r, t);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?> future) {
            try {
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            logger.error("Uncaught exception happened while running {}. Message: {}", r, ExceptionUtils.getRootCause(t).getMessage(), t);
        }
    }

}
