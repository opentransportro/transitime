/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcSchedTrip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a single trip for an ApiSchedule
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleTrip {

    @JsonProperty
    private String tripShortName;

    @JsonProperty
    private String tripId;

    @JsonProperty
    private String tripHeadsign;

    @JsonProperty
    private String blockId;

    public ApiScheduleTrip(IpcSchedTrip ipcScheduleTrip) {
        this.tripShortName = ipcScheduleTrip.getTripShortName();
        this.tripId = ipcScheduleTrip.getTripId();
        this.tripHeadsign = ipcScheduleTrip.getTripHeadsign();
        this.blockId = ipcScheduleTrip.getBlockId();
    }
}
