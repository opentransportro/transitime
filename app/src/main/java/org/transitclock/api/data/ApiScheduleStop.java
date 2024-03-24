/* (C)2023 */
package org.transitclock.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Contains minimal for a stop for a schedule
 *
 * @author Michael
 */
@Data
public class ApiScheduleStop {
    @JsonProperty
    private String stopId;

    @JsonProperty
    private String stopName;


    public ApiScheduleStop(String stopId, String stopName) {
        this.stopId = stopId;
        this.stopName = stopName;
    }
}
