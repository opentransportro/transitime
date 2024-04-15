/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;

import org.transitclock.domain.structs.ScheduleTime;
import org.transitclock.domain.structs.Stop;
import org.transitclock.utils.Time;

/**
 * Configuration information for a schedule times for IPC.
 *
 * @author SkiBu Smith
 */
public class IpcSchedTimes implements Serializable {

    private final Integer arrivalTime;
    private final Integer departureTime;
    private final String stopId;
    private final String stopName;

    public IpcSchedTimes(ScheduleTime dbScheduleTime, Stop stop) {
        this.arrivalTime = dbScheduleTime.getArrivalTime();
        this.departureTime = dbScheduleTime.getDepartureTime();
        this.stopId = stop.getId();
        this.stopName = stop.getName();
    }

    @Override
    public String toString() {
        return "IpcScheduleTimes ["
                + "arrivalTime="
                + Time.timeOfDayStr(arrivalTime)
                + ", departureTime="
                + Time.timeOfDayStr(departureTime)
                + ", stopId="
                + stopId
                + ", stopName="
                + stopName
                + "]";
    }

    public Integer getArrivalTime() {
        return arrivalTime;
    }

    public Integer getDepartureTime() {
        return departureTime;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }
}
