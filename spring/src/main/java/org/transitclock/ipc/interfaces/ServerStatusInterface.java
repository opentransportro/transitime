/* (C)2023 */
package org.transitclock.ipc.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import org.transitclock.ipc.data.IpcServerStatus;

/**
 * RMI interface for determining the status of the server.
 *
 * @author SkiBu Smith
 */
public interface ServerStatusInterface extends Remote {

    /**
     * Gets from the server a IpcStatus object indicating the status of the server.
     *
     * @return
     * @throws RemoteException
     */
    public IpcServerStatus get() throws RemoteException;

    /**
     * Monitors the agency server for problems. If there is a problem then a message indicating such
     * is returned. Sending out notifications is done by the server side.
     *
     * @return Error message if there is one, otherwise null
     * @throws RemoteException
     */
    public String monitor() throws RemoteException;

    /**
     * Gets current server time.
     *
     * @return
     * @throws RemoteException
     */
    public Date getCurrentServerTime() throws RemoteException;
}
