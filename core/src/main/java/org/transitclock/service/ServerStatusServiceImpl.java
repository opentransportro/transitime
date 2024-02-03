/* (C)2023 */
package org.transitclock.service;

import java.rmi.RemoteException;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.service.dto.IpcServerStatus;
import org.transitclock.service.contract.ServerStatusInterface;
import org.transitclock.monitoring.AgencyMonitor;
import org.transitclock.utils.SystemTime;

/**
 * Runs on the server side and receives IPC calls and returns results.
 *
 * @author SkiBu Smith
 */
@Service
@Slf4j
public class ServerStatusServiceImpl implements ServerStatusInterface {

    private final String agencyId;

    private ServerStatusServiceImpl() {
        agencyId = AgencyConfig.getAgencyId();
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ServerStatusInterface#get()
     */
    @Override
    public IpcServerStatus get() throws RemoteException {
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);
        return new IpcServerStatus(agencyMonitor.getMonitorResults());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ServerStatusInterface#monitor()
     */
    @Override
    public String monitor() throws RemoteException {
        // Monitor everything having to do with an agency server. Send
        // out any notifications if necessary. Return any resulting
        // error message.
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);

        return agencyMonitor.checkAll();
    }

    @Override
    public Date getCurrentServerTime() throws RemoteException {
        return SystemTime.getDate();
    }
}
