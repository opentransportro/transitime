/* (C)2023 */
package org.transitclock.ipc.servers;

import java.rmi.RemoteException;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.ipc.data.IpcServerStatus;
import org.transitclock.ipc.interfaces.ServerStatusInterface;
import org.transitclock.monitoring.AgencyMonitor;
import org.transitclock.utils.SystemTime;

/**
 * Runs on the server side and receives IPC calls and returns results.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class ServerStatusServiceImpl implements ServerStatusInterface {

    // Should only be accessed as singleton class
    private static ServerStatusServiceImpl singleton;

    public static ServerStatusInterface instance() {
        return singleton;
    }

    /**
     * @param agencyId
     * @return
     */
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
    private ServerStatusServiceImpl(String projectId) {
        agencyId = projectId;
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
