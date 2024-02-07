package org.transitclock.utils.threading;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread {}", t.getName(), e);
    }
}
