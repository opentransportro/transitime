/* (C)2023 */
package org.transitclock.service;

import lombok.extern.slf4j.Slf4j;
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
public class ServerStatusServiceImpl implements ServerStatusInterface {
    private static ServerStatusServiceImpl singleton;

    public static ServerStatusInterface instance() {
        return singleton;
    }

    public static ServerStatusServiceImpl start(String agencyId) {
        if (singleton == null) {
            singleton = new ServerStatusServiceImpl(agencyId);
        }

        return singleton;
    }

    private final String agencyId;
    /**
     * Constructor is private because singleton class
     *
     * @param projectId
     * @param objectName
     */
    public ServerStatusServiceImpl(String projectId) {
        agencyId = projectId;
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ServerStatusInterface#get()
     */
    @Override
    public IpcServerStatus get() {
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);
        return new IpcServerStatus(agencyMonitor.getMonitorResults());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ServerStatusInterface#monitor()
     */
    @Override
    public String monitor() {
        // Monitor everything having to do with an agency server. Send
        // out any notifications if necessary. Return any resulting
        // error message.
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);

        return agencyMonitor.checkAll();
    }

    @Override
    public Date getCurrentServerTime() {
        return SystemTime.getDate();
    }
}
