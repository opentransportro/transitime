/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import javax.persistence.Embeddable;
import org.transitclock.utils.Time;

/**
 * For keeping track of schedule times from GTFS data. Either arrival time or departure could be
 * null.
 *
 * @author SkiBu Smith
 */
@Embeddable
public class ScheduleTime implements Serializable {

    // Times are in seconds. arrivalTime only set for last
    // stop in trip. Otherwise only departure time is set.
    private final Integer arrivalTime;
    private final Integer departureTime;

    // Because using serialization to store array of ScheduleTimes
    // this class needs to be Serializable.
    private static final long serialVersionUID = 7480539886372288095L;

    /********************** Member Functions **************************/
    public ScheduleTime(Integer arrivalTime, Integer departureTime) {
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    protected ScheduleTime() {
        arrivalTime = null;
        departureTime = null;
    }

    /**
     * Returns departure time if there is one. Otherwise returns arrival time if there is one.
     * Otherwise returns null.
     *
     * @return
     */
    public Integer getTime() {
        if (departureTime != null) return departureTime;
        return arrivalTime;
    }

    /**
     * Time of day in seconds. Will be null if there is no arrival time (even if there is a
     * departure time). There will be no arrival time unless it is last stop in trip.
     *
     * @return
     */
    public Integer getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Time of day in seconds. Will be null if there is no departure time (even if there is an
     * arrival time). There will be no departure time if last stop of trip.
     *
     * @return
     */
    public Integer getDepartureTime() {
        return departureTime;
    }

    @Override
    public String toString() {
        return "ScheduleTime ["
                + (arrivalTime != null ? "a=" + Time.timeOfDayStr(arrivalTime) : "")
                + (arrivalTime != null && departureTime != null ? ", " : "")
                + (departureTime != null ? "d=" + Time.timeOfDayStr(departureTime) : "")
                + "]";
    }
}
