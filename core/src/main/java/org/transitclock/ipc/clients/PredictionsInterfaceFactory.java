/* (C)2023 */
package org.transitclock.ipc.clients;

import java.util.HashMap;
import java.util.Map;
import org.transitclock.ipc.interfaces.PredictionsInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a PredictionsInterface client that can be queried for predictions.
 *
 * @author SkiBu Smith
 */
public class PredictionsInterfaceFactory {

    // Keyed by agencyId
    private static final Map<String, PredictionsInterface> predictionsInterfaceMap =
            new HashMap<String, PredictionsInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the PredictionsInterface for the specified projectId. There is one interface per
     * agencyId.
     *
     * @param agencyId
     * @return
     */
    public static PredictionsInterface get(String agencyId) {
        PredictionsInterface predictionsInterface = predictionsInterfaceMap.get(agencyId);
        if (predictionsInterface == null) {
            predictionsInterface = ClientFactory.getInstance(agencyId, PredictionsInterface.class);
            predictionsInterfaceMap.put(agencyId, predictionsInterface);
        }

        return predictionsInterface;
    }
}
