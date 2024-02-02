/* (C)2023 */
package org.transitclock.core.dataCache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.transitclock.core.VehicleState;

/**
 * For keeping track of vehicle state. This is used by the main predictor code, not for RMI clients.
 * For RMI clients the VehicleDataCache is used. This way making the system threadsafe is simpler
 * since VehicleDataCache can handle thread safety completely independently.
 *
 * @author SkiBu Smith
 */
@Component
public class VehicleStateManager {

    // Keyed by vehicle ID. Need to use ConcurrentHashMap instead of HashMap
    // since getVehiclesState() returns values() of the map which can be
    // accessed while the map is being modified with new data via another
    // thread. Otherwise could get a ConcurrentModificationException.
    private final Map<String, VehicleState> vehicleMap = new ConcurrentHashMap<>();


    /**
     * Adds VehicleState for the vehicle to the map so that it can be retrieved later.
     *
     * @param state The VehicleState to be added to the map
     */
    private void putVehicleState(VehicleState state) {
        vehicleMap.put(state.getVehicleId(), state);
    }

    /**
     * Returns vehicle state for the specified vehicle. Vehicle state is kept in a map. If
     * VehicleState not yet created for the vehicle then this method will create it. If there was no
     * VehicleState already created for the vehicle then it is created. This way this method never
     * returns null.
     *
     * <p>VehicleState is a large object with multiple collections as members. Since it might be
     * getting modified when there is a new AVL report when this method is called need to
     * synchronize on the returned VehicleState object if accessing any information that is not
     * atomic, such as the avlReportHistory.
     *
     * @param vehicleId
     * @return the VehicleState for the vehicle
     */
    public VehicleState getVehicleState(String vehicleId) {
        VehicleState vehicleState = vehicleMap.get(vehicleId);
        if (vehicleState == null) {
            vehicleState = new VehicleState(vehicleId);
            putVehicleState(vehicleState);
        }
        return vehicleState;
    }

    /**
     * Returns VehicleState for all vehicles.
     *
     * @return Collection of VehicleState objects for all vehicles.
     */
    public Collection<VehicleState> getVehiclesState() {
        return vehicleMap.values();
    }
}
