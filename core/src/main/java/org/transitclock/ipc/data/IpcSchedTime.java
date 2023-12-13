/* (C)2023 */
package org.transitclock.ipc.data;

import java.io.Serializable;

/**
 * A schedule time for a particular stop/trip.
 *
 * @author SkiBu Smith
 */
public class IpcSchedTime implements Serializable {

    private final String stopId;
    private final String stopName;
    private final Integer timeOfDay;

    private static final long serialVersionUID = 5022156970470667431L;

    /********************** Member Functions **************************/

    /**
     * @param stopId
     * @param timeOfDay
     */
    public IpcSchedTime(String stopId, String stopName, Integer timeOfDay) {
        super();
        this.stopId = stopId;
        this.stopName = stopName;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public String toString() {
        return "IpcScheduleTime [" + "stopId=" + stopId + ", stopName=" + stopName + ", timeOfDay=" + timeOfDay + "]";
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
