/* (C)2023 */
package org.transitclock.core.avl.ad;

import org.transitclock.core.VehicleState;

/**
 * An interface used for generating arrival/departure times. An interface is used so that can easily
 * swap in other software for generating arrivals/departures.
 *
 * @author SkiBu Smith
 */
public interface ArrivalDepartureGenerator {
    /**
     * Determines arrival/departure times and stores them into db. Also should call
     * vehicleState.setVehicleAtStopInfo(newVehicleAtStopInfo) to specify if vehicle at a stop. This
     * is done in ArrivalDepartureGenerator.generate() since that is where this info is easily
     * determined.
     *
     * @param vehicleState
     * @return List of ArrivalDeparture objects generated
     */
    void generate(VehicleState vehicleState);
}
