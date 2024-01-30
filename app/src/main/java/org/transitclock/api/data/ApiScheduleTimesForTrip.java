/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.service.dto.IpcSchedTime;
import org.transitclock.service.dto.IpcSchedTrip;

/**
 * Contains the schedule times for a trip. For when outputting stops horizontally.
 *
 * @author Michael
 */
public class ApiScheduleTimesForTrip {
    @XmlAttribute
    private String tripShortName;

    @XmlAttribute
    private String tripId;

    @XmlAttribute
    private String tripHeadsign;

    @XmlAttribute
    private String blockId;

    @XmlElement(name = "time")
    private List<ApiScheduleTime> times;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleTimesForTrip() {}

    public ApiScheduleTimesForTrip(IpcSchedTrip ipcSchedTrip) {
        this.tripShortName = ipcSchedTrip.getTripShortName();
        this.tripId = ipcSchedTrip.getTripId();
        this.tripHeadsign = ipcSchedTrip.getTripHeadsign();
        this.blockId = ipcSchedTrip.getBlockId();

        times = new ArrayList<ApiScheduleTime>();
        for (IpcSchedTime ipcSchedTime : ipcSchedTrip.getSchedTimes()) {
            times.add(new ApiScheduleTime(ipcSchedTime.getTimeOfDay()));
        }
    }
}
