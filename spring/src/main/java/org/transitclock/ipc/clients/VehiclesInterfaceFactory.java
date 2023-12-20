/* (C)2023 */
package org.transitclock.ipc.clients;

import org.transitclock.ipc.interfaces.VehiclesInterface;
import org.transitclock.ipc.rmi.ClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a VehiclesInterface client that can be queried for Vehicle info.
 *
 * @author SkiBu Smith
 */
public class VehiclesInterfaceFactory {

    // Keyed by agencyId
    private static Map<String, VehiclesInterface> vehiclesInterfaceMap = new HashMap<String, VehiclesInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the singleton instance.
     *
     * @param agencyId
     * @return
     */
    public static VehiclesInterface get(String agencyId) {
        VehiclesInterface vehiclesInterface = vehiclesInterfaceMap.get(agencyId);
        if (vehiclesInterface == null) {
            vehiclesInterface = ClientFactory.getInstance(agencyId, VehiclesInterface.class);
            vehiclesInterfaceMap.put(agencyId, vehiclesInterface);
        }

        return vehiclesInterface;
    }
}
