/* (C)2023 */
package org.transitclock.ipc.clients;

import java.util.HashMap;
import java.util.Map;
import org.transitclock.ipc.interfaces.ServerStatusInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a ServerStatusInterface client that can be queried for server status info via IPC.
 *
 * @author SkiBu Smith
 */
public class ServerStatusInterfaceFactory {

    // Keyed by agencyId
    private static Map<String, ServerStatusInterface> serverStatusInterfaceMap =
            new HashMap<String, ServerStatusInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the singleton instance.
     *
     * @param agencyId
     * @return
     */
    public static ServerStatusInterface get(String agencyId) {
        ServerStatusInterface serverStatusInterface = serverStatusInterfaceMap.get(agencyId);
        if (serverStatusInterface == null) {
            serverStatusInterface = ClientFactory.getInstance(agencyId, ServerStatusInterface.class);
            serverStatusInterfaceMap.put(agencyId, serverStatusInterface);
        }

        return serverStatusInterface;
    }
}
