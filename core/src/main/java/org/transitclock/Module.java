/* (C)2023 */
package org.transitclock;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.utils.Time;

/**
 * Modules are run in a separate thread and continuously process data. They are initiated by core
 * prediction system by configuring which modules should be started.
 *
 * <p>Each module should inherit from this class and implement run() and a constructor that takes
 * the agencyId as a string parameter.
 *
 * @author SkiBu Smith
 */
@Slf4j
public abstract class Module implements Runnable {
    public enum ExecutionType {
        ONCE,
        FIXED_DELAY,
        FIXED_RATE
    }

    /**
     * Initial execution delay expressed in MILLISECONDS
     * @return initialDelay
     */
    public int initialExecutionDelay() {
        return 0;
    }

    /**
     * Execution period expressed in MILLISECONDS
     * @return execution period
     */
    public int executionPeriod() {
        return 15 * Time.SEC_IN_MSECS;
    }

    public abstract ExecutionType getExecutionType();
}
