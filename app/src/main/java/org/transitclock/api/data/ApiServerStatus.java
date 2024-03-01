/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.monitoring.MonitorResult;
import org.transitclock.service.dto.IpcServerStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Server status for an agency server
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "serverStatus")
public class ApiServerStatus {

    @XmlAttribute
    private String agencyId;

    @XmlElement(name = "serverMonitor")
    private List<ApiServerMonitor> serverMonitors;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiServerStatus() {}

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
