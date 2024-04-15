/* (C)2023 */
package org.transitclock.service.contract;

import org.transitclock.service.dto.IpcServerStatus;

import java.util.Date;

/**
 * RMI interface for determining the status of the server.
 *
 * @author SkiBu Smith
 */
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
