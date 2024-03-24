/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcSchedTimes;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a schedule time for a stop. Contains both arrival and departure time and is intended
 * to be used for displaying the details of a trip.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleArrDepTime {

    @JsonProperty
    private String arrivalTime;

    @JsonProperty
    private String departureTime;

    @JsonProperty
    private String stopId;

    @JsonProperty
    private String stopName;

    public ApiScheduleArrDepTime(IpcSchedTimes ipcScheduleTimes) {
        Integer arrivalInt = ipcScheduleTimes.getArrivalTime();
        arrivalTime = arrivalInt == null ? null : Time.timeOfDayStr(arrivalInt);

        Integer departureInt = ipcScheduleTimes.getDepartureTime();
        departureTime = departureInt == null ? null : Time.timeOfDayStr(departureInt);

        stopId = ipcScheduleTimes.getStopId();
        stopName = ipcScheduleTimes.getStopName();
    }
}
