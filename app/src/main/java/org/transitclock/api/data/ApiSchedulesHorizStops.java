/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class ApiSchedulesHorizStops {

    @JsonProperty
    private String routeId;

    @JsonProperty
    private String routeName;

    @JsonProperty
    private List<ApiScheduleHorizStops> schedules;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiSchedulesHorizStops() {}

    public ApiSchedulesHorizStops(List<IpcSchedule> schedules) {
        this.routeId = schedules.get(0).getRouteId();
        this.routeName = schedules.get(0).getRouteName();

        this.schedules = new ArrayList<>(schedules.size());
        for (IpcSchedule ipcSchedule : schedules) {
            this.schedules.add(new ApiScheduleHorizStops(ipcSchedule));
        }
    }
}
