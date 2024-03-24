/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A shorter version of ApiTrip for when all the detailed info is not needed.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTripTerse {

    @JsonProperty
    private String id;

    @JsonProperty
    private String shortName;

    @JsonProperty
    private String startTime;

    @JsonProperty
    private String endTime;

    @JsonProperty
    private String directionId;

    @JsonProperty
    private String headsign;

    @JsonProperty
    private String routeId;

    @JsonProperty
    private String routeShortName;


    public ApiTripTerse(IpcTrip ipcTrip) {
        id = ipcTrip.getId();
        shortName = ipcTrip.getShortName();
        startTime = Time.timeOfDayStr(ipcTrip.getStartTime());
        endTime = Time.timeOfDayStr(ipcTrip.getEndTime());
        directionId = ipcTrip.getDirectionId();
        headsign = ipcTrip.getHeadsign();
        routeId = ipcTrip.getRouteId();
        routeShortName = ipcTrip.getRouteShortName();
    }

    public String getRouteId() {
        return routeId;
    }
}
