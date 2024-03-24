/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a schedule time for a stop. Intended to be used for displaying a schedule for a route.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiScheduleTime {

    @JsonProperty
    private String timeStr;

    @JsonProperty
    private Integer timeSecs;

    public ApiScheduleTime(Integer time) {
        this.timeStr = time == null ? null : Time.timeOfDayShortStr(time);
        this.timeSecs = time;
    }
}
