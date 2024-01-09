/* (C)2023 */
package org.transitclock;

import lombok.extern.slf4j.Slf4j;

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

    protected final String agencyId;

    /**
     * Constructor. Subclasses must implement a constructor that takes in agencyId and calls this
     * constructor via super(agencyId).
     *
     * @param agencyId
     */
    protected Module(String agencyId) {
        this.agencyId = agencyId;
    }

    protected String getAgencyId() {
        return agencyId;
    }

}
