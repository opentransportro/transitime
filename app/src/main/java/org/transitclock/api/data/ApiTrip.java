/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Specifies how trip data is formatted for the API.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTrip {

    @JsonProperty
    private int configRev;

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
    private String routeId;

    @JsonProperty
    private String routeShortName;

    @JsonProperty
    private ApiTripPattern tripPattern;

    @JsonProperty
    private String serviceId;

    @JsonProperty
    private String headsign;

    @JsonProperty
    private String blockId;

    @JsonProperty
    private String shapeId;

    // Using a Boolean so that will be output only if true
    @JsonProperty
    private Boolean noSchedule;

    @JsonProperty("times")
    private List<ApiScheduleArrDepTime> times;

    /**
     * @param ipcTrip
     * @param includeStopPaths Stop paths are only included in output if this param set to true.
     */
    public ApiTrip(IpcTrip ipcTrip, boolean includeStopPaths) {
        configRev = ipcTrip.getConfigRev();
        id = ipcTrip.getId();
        shortName = ipcTrip.getShortName();
        startTime = Time.timeOfDayStr(ipcTrip.getStartTime());
        endTime = Time.timeOfDayStr(ipcTrip.getEndTime());
        directionId = ipcTrip.getDirectionId();
        routeId = ipcTrip.getRouteId();
        routeShortName = ipcTrip.getRouteShortName();
        tripPattern = new ApiTripPattern(ipcTrip.getTripPattern(), includeStopPaths);
        serviceId = ipcTrip.getServiceId();
        headsign = ipcTrip.getHeadsign();
        blockId = ipcTrip.getBlockId();
        shapeId = ipcTrip.getShapeId();

        noSchedule = ipcTrip.isNoSchedule() ? true : null;

        times = ipcTrip.getScheduleTimes()
                .stream()
                .map(ApiScheduleArrDepTime::new)
                .toList();
    }
}
