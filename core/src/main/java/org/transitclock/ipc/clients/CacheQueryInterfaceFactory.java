/* (C)2023 */
package org.transitclock.ipc.clients;

import java.util.HashMap;
import java.util.Map;
import org.transitclock.ipc.interfaces.CacheQueryInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a CacheQueryInterface client that can be sent cache queries.
 *
 * @author Sean Og Crudden
 */
public class CacheQueryInterfaceFactory {

    // Keyed by agencyId
    private static Map<String, CacheQueryInterface> cachequeryInterfaceMap = new HashMap<String, CacheQueryInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the singleton instance.
     *
     * @param agencyId
     * @return
     */
    public static CacheQueryInterface get(String agencyId) {
        CacheQueryInterface cachequeryInterface = cachequeryInterfaceMap.get(agencyId);
        if (cachequeryInterface == null) {
            cachequeryInterface = ClientFactory.getInstance(agencyId, CacheQueryInterface.class);
            cachequeryInterfaceMap.put(agencyId, cachequeryInterface);
        }

        return cachequeryInterface;
    }
}
