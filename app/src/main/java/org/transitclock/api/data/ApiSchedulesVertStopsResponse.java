/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a collection of ApiScheduleVertStops objects for a route. There is one
 * ApiScheduleVertStops for each direction/service for a route. The stops are listed vertically in
 * the matrix. For when there are a good number of stops but not as many trips, such as for commuter
 * rail.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiSchedulesVertStopsResponse {

    @JsonProperty
    private String routeId;

    @JsonProperty
    private String routeName;

    @JsonProperty
    private List<ApiScheduleVertStops> schedules;


    public ApiSchedulesVertStopsResponse(List<IpcSchedule> schedules) {
        this.routeId = schedules.get(0).getRouteId();
        this.routeName = schedules.get(0).getRouteName();

        this.schedules = new ArrayList<>(schedules.size());
        for (IpcSchedule ipcSchedule : schedules) {
            this.schedules.add(new ApiScheduleVertStops(ipcSchedule));
        }
    }
}
