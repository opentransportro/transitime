/* (C)2023 */
package org.transitclock.service.contract;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

import org.jvnet.hk2.annotations.Contract;
import org.transitclock.service.dto.IpcServerStatus;

/**
 * RMI interface for determining the status of the server.
 *
 * @author SkiBu Smith
 */
@Contract
public interface ServerStatusInterface {

    /**
     * Gets from the server a IpcStatus object indicating the status of the server.
     *
     * @return
     */
    IpcServerStatus get();

    /**
     * Monitors the agency server for problems. If there is a problem then a message indicating such
     * is returned. Sending out notifications is done by the server side.
     *
     * @return Error message if there is one, otherwise null
     */
    String monitor();

    /**
     * Gets current server time.
     *
     * @return
     */
    Date getCurrentServerTime();
}
