/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.service.dto.IpcBlock;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes a block in terse form, without schedule and trip pattern info
 *
 * @author SkiBu Smith
 */
@Data
public class ApiBlockTerse {

    @JsonProperty
    private int configRev;

    @JsonProperty
    private String id;

    @JsonProperty
    private String serviceId;

    @JsonProperty
    private String startTime;

    @JsonProperty
    private String endTime;

    @JsonProperty
    private List<ApiTripTerse> trips;

    @JsonProperty("routes")
    private List<ApiRoute> routes;


    public ApiBlockTerse(IpcBlock ipcBlock) {
        configRev = ipcBlock.getConfigRev();
        id = ipcBlock.getId();
        serviceId = ipcBlock.getServiceId();
        startTime = Time.timeOfDayStr(ipcBlock.getStartTime());
        endTime = Time.timeOfDayStr(ipcBlock.getEndTime());

        trips = ipcBlock.getTrips()
                .stream()
                .map(ApiTripTerse::new)
                .toList();

        routes = ipcBlock.getRouteSummaries()
                .stream()
                .map(ApiRoute::new)
                .toList();
    }
}
