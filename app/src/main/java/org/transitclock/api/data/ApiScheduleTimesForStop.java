/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains the schedule times for a stop for each trip for the route/direction/service. For when
 * outputting stops vertically.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleTimesForStop {

    @JsonProperty
    private String stopId;

    @JsonProperty
    private String stopName;

    @JsonProperty
    private List<ApiScheduleTime> times;

    public ApiScheduleTimesForStop(String stopId, String stopName) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.times = new ArrayList<>();
    }

    public void add(Integer time) {
        this.times.add(new ApiScheduleTime(time));
    }
}
