/* (C)2023 */
package org.transitclock.utils.threading;

import java.util.concurrent.ThreadFactory;

/**
 * A thread factory that names the threads. Eases debugging.
 *
 * <p>Based on code from the book "Java Concurrency in Practice" by Brian Goetz
 *
 * @author SkiBu Smith
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String poolName;

    public NamedThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    public Thread newThread(Runnable runnable) {
        return new NamedThread(runnable, poolName);
    }
}
