/* (C)2023 */
package org.transitclock.ipc.clients;

import java.util.HashMap;
import java.util.Map;
import org.transitclock.ipc.interfaces.HoldingTimeInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a HoldingTimeInterface client that can be sent holding time queries.
 *
 * @author Sean Og Crudden
 */
public class HoldingTimeInterfaceFactory {

    // Keyed by agencyId
    private static final Map<String, HoldingTimeInterface> holdingtimeInterfaceMap =
            new HashMap<String, HoldingTimeInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the singleton instance.
     *
     * @param agencyId
     * @return
     */
    public static HoldingTimeInterface get(String agencyId) {
        HoldingTimeInterface holdingTimeInterface = holdingtimeInterfaceMap.get(agencyId);
        if (holdingTimeInterface == null) {

            holdingTimeInterface = ClientFactory.getInstance(agencyId, HoldingTimeInterface.class);

            holdingtimeInterfaceMap.put(agencyId, holdingTimeInterface);
        }

        return holdingTimeInterface;
    }
}
