/* (C)2023 */
package org.transitclock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.monitoring.AgencyMonitor;
import org.transitclock.service.contract.ServerStatusInterface;
import org.transitclock.service.dto.IpcServerStatus;
import org.transitclock.utils.SystemTime;

import java.util.Date;

/**
 * Runs on the server side and receives IPC calls and returns results.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class ServerStatusServiceImpl implements ServerStatusInterface {
    @Autowired
    private AgencyMonitor agencyMonitor;


    @Override
    public IpcServerStatus get() {
        return new IpcServerStatus(agencyMonitor.getMonitorResults());
    }

    @Override
    public String monitor() {
        return agencyMonitor.checkAll();
    }

    @Override
    public Date getCurrentServerTime() {
        return SystemTime.getDate();
    }
}
