/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcRouteSummary;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes a block
 *
 * @author SkiBu Smith
 */
@Data
public class ApiBlock {

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
    private List<ApiTrip> trips;

    @JsonProperty
    private List<ApiRoute> routes;

    public ApiBlock(IpcBlock ipcBlock) {
        configRev = ipcBlock.getConfigRev();
        id = ipcBlock.getId();
        serviceId = ipcBlock.getServiceId();
        startTime = Time.timeOfDayStr(ipcBlock.getStartTime());
        endTime = Time.timeOfDayStr(ipcBlock.getEndTime());

        trips = ipcBlock.getTrips()
                .stream()
                .map(t -> new ApiTrip(t, false))
                .toList();

        routes = ipcBlock.getRouteSummaries()
                .stream()
                .map(ApiRoute::new)
                .toList();
    }
}
