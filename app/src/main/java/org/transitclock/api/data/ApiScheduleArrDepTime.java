/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import org.transitclock.service.dto.IpcSchedTimes;
import org.transitclock.utils.Time;

/**
 * Represents a schedule time for a stop. Contains both arrival and departure time and is intended
 * to be used for displaying the details of a trip.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleArrDepTime {

    @XmlAttribute
    private String arrivalTime;

    @XmlAttribute
    private String departureTime;

    @XmlAttribute
    private String stopId;

    @XmlAttribute
    private String stopName;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleArrDepTime() {}

    public ApiScheduleArrDepTime(IpcSchedTimes ipcScheduleTimes) {
        Integer arrivalInt = ipcScheduleTimes.getArrivalTime();
        arrivalTime = arrivalInt == null ? null : Time.timeOfDayStr(arrivalInt);

        Integer departureInt = ipcScheduleTimes.getDepartureTime();
        departureTime = departureInt == null ? null : Time.timeOfDayStr(departureInt);

        stopId = ipcScheduleTimes.getStopId();
        stopName = ipcScheduleTimes.getStopName();
    }
}
