/* (C)2023 */
package org.transitclock.ipc.data;

import lombok.ToString;

import java.io.Serializable;

/**
 * A schedule time for a particular stop/trip.
 *
 * @author SkiBu Smith
 */
@ToString
public class IpcSchedTime implements Serializable {

    private final String stopId;
    private final String stopName;
    private final Integer timeOfDay;

    /**
     * @param stopId
     * @param timeOfDay
     */
    public IpcSchedTime(String stopId, String stopName, Integer timeOfDay) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.timeOfDay = timeOfDay;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public Integer getTimeOfDay() {
        return timeOfDay;
    }
}
