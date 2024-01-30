/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import java.util.List;
import org.transitclock.monitoring.MonitorResult;

/**
 * Represents server status for Inter Process Communication (IPC)
 *
 * @author SkiBu Smith
 */
public class IpcServerStatus implements Serializable {

    private final List<MonitorResult> monitorResults;

    public IpcServerStatus(List<MonitorResult> monitorResults) {
        this.monitorResults = monitorResults;
    }

    @Override
    public String toString() {
        return "IpcServerStatus [" + "monitorResults=" + monitorResults + "]";
    }

    public List<MonitorResult> getMonitorResults() {
        return monitorResults;
    }
}
