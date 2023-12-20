/* (C)2023 */
package org.transitclock.ipc.clients;

import org.transitclock.ipc.interfaces.ConfigInterface;
import org.transitclock.ipc.rmi.ClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a ConfigInterface client that can be queried for configuration info.
 *
 * @author SkiBu Smith
 */
public class ConfigInterfaceFactory {

    // Keyed by agencyId
    private static Map<String, ConfigInterface> configInterfaceMap = new HashMap<String, ConfigInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the singleton instance.
     *
     * @param agencyId
     * @return
     */
    public static ConfigInterface get(String agencyId) {
        ConfigInterface configInterface = configInterfaceMap.get(agencyId);
        if (configInterface == null) {
            configInterface = ClientFactory.getInstance(agencyId, ConfigInterface.class);
            configInterfaceMap.put(agencyId, configInterface);
        }

        return configInterface;
    }
}
