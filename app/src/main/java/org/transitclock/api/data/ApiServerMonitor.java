/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.monitoring.MonitorResult;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author SkiBu Smith
 */
@Data
public class ApiServerMonitor {

    @JsonProperty
    private String type;

    @JsonProperty
    private String message;


    public ApiServerMonitor(MonitorResult monitorResult) {
        this.type = monitorResult.getType();
        this.message = monitorResult.getMessage();
    }
}
