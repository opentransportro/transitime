/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.transitclock.service.dto.IpcSchedTrip;

/**
 * Represents a single trip for an ApiSchedule
 *
 * @author SkiBu Smith
 */
public class ApiScheduleTrip {

    @XmlAttribute
    private String tripShortName;

    @XmlAttribute
    private String tripId;

    @XmlAttribute
    private String tripHeadsign;

    @XmlAttribute
    private String blockId;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleTrip() {}

    public ApiScheduleTrip(IpcSchedTrip ipcScheduleTrip) {
        this.tripShortName = ipcScheduleTrip.getTripShortName();
        this.tripId = ipcScheduleTrip.getTripId();
        this.tripHeadsign = ipcScheduleTrip.getTripHeadsign();
        this.blockId = ipcScheduleTrip.getBlockId();
    }
}
