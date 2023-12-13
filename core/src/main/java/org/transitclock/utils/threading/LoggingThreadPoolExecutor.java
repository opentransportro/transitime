/* (C)2023 */
package org.transitclock.utils.threading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ThreadPoolExecutor that also logs when a thread is created. Very useful for when need to
 * understand thread usage.
 *
 * @author SkiBu Smith
 */
public class LoggingThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingThreadPoolExecutor.class);

    public LoggingThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        logger.info("About to exeute thread %s: start %s", t, r);
    }
}
