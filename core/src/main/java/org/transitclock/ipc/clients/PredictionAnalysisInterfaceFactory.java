/* (C)2023 */
package org.transitclock.ipc.clients;

import java.util.HashMap;
import java.util.Map;
import org.transitclock.ipc.interfaces.PredictionAnalysisInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a PredictionsInterface client that can be queried for predictions.
 *
 * @author Sean Og Crudden
 */
public class PredictionAnalysisInterfaceFactory {

    // Keyed by agencyId
    private static final Map<String, PredictionAnalysisInterface> predictionAnalysisInterfaceMap =
            new HashMap<String, PredictionAnalysisInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the PredictionAnalysisInterface for the specified projectId. There is one interface per
     * agencyId.
     *
     * @param agencyId
     * @return
     */
    public static PredictionAnalysisInterface get(String agencyId) {
        PredictionAnalysisInterface predictionAnalysisInterface = predictionAnalysisInterfaceMap.get(agencyId);
        if (predictionAnalysisInterface == null) {
            predictionAnalysisInterface = ClientFactory.getInstance(agencyId, PredictionAnalysisInterface.class);
            predictionAnalysisInterfaceMap.put(agencyId, predictionAnalysisInterface);
        }
        return predictionAnalysisInterface;
    }
}
