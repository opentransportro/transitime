/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcSchedTime;
import org.transitclock.service.dto.IpcSchedTrip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains the schedule times for a trip. For when outputting stops horizontally.
 *
 * @author Michael
 */
@Data
public class ApiScheduleTimesForTrip {
    @JsonProperty
    private String tripShortName;

    @JsonProperty
    private String tripId;

    @JsonProperty
    private String tripHeadsign;

    @JsonProperty
    private String blockId;

    @JsonProperty
    private List<ApiScheduleTime> times;

    public ApiScheduleTimesForTrip(IpcSchedTrip ipcSchedTrip) {
        this.tripShortName = ipcSchedTrip.getTripShortName();
        this.tripId = ipcSchedTrip.getTripId();
        this.tripHeadsign = ipcSchedTrip.getTripHeadsign();
        this.blockId = ipcSchedTrip.getBlockId();

        times = new ArrayList<>();
        for (IpcSchedTime ipcSchedTime : ipcSchedTrip.getSchedTimes()) {
            times.add(new ApiScheduleTime(ipcSchedTime.getTimeOfDay()));
        }
    }
}
