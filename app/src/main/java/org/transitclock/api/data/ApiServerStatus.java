/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.monitoring.MonitorResult;
import org.transitclock.service.dto.IpcServerStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Server status for an agency server
 *
 * @author SkiBu Smith
 */
@Data
public class ApiServerStatus {

    @JsonProperty
    private String agencyId;

    @JsonProperty
    private List<ApiServerMonitor> serverMonitors;


    /**
     * Constructors a ApiServerStatus object from agencyId and IpcServerStatus objects.
     *
     * @param agencyId
     * @param ipcServerStatus
     */
    public ApiServerStatus(String agencyId, IpcServerStatus ipcServerStatus) {
        this.agencyId = agencyId;

        serverMonitors = new ArrayList<ApiServerMonitor>();
        for (MonitorResult monitorResult : ipcServerStatus.getMonitorResults()) {
            this.serverMonitors.add(new ApiServerMonitor(monitorResult));
        }
    }
}
