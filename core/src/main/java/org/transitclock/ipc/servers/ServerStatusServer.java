/* (C)2023 */
package org.transitclock.ipc.servers;

import java.rmi.RemoteException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.ipc.data.IpcServerStatus;
import org.transitclock.ipc.interfaces.ServerStatusInterface;
import org.transitclock.ipc.rmi.AbstractServer;
import org.transitclock.monitoring.AgencyMonitor;

/**
 * Runs on the server side and receives IPC calls and returns results.
 *
 * @author SkiBu Smith
 */
public class ServerStatusServer extends AbstractServer implements ServerStatusInterface {

    // Should only be accessed as singleton class
    private static ServerStatusServer singleton;

    private static final Logger logger = LoggerFactory.getLogger(ServerStatusServer.class);

    /********************** Member Functions **************************/

    /**
     * @param agencyId
     * @return
     */
    public static ServerStatusServer start(String agencyId) {
        if (singleton == null) {
            singleton = new ServerStatusServer(agencyId);
        }

        if (!singleton.getAgencyId().equals(agencyId)) {
            logger.error(
                    "Tried calling ServerStatusServer.start() for "
                            + "agencyId={} but the singleton was created for projectId={}",
                    agencyId,
                    singleton.getAgencyId());
            return null;
        }

        return singleton;
    }

    /**
     * Constructor is private because singleton class
     *
     * @param projectId
     * @param objectName
     */
    private ServerStatusServer(String projectId) {
        super(projectId, ServerStatusInterface.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see org.transitclock.ipc.interfaces.ServerStatusInterface#get()
     */
    @Override
    public IpcServerStatus get() throws RemoteException {
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(getAgencyId());
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
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(getAgencyId());
        String resultStr = agencyMonitor.checkAll();

        return resultStr;
    }

    @Override
    public Date getCurrentServerTime() throws RemoteException {
        return new Date(Core.getInstance().getSystemTime());
    }
}
