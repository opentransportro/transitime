package org.transitclock.core.prediction;

import org.transitclock.core.VehicleState;
import org.transitclock.service.dto.IpcPrediction;

import java.util.List;

public interface PredictionGenerator {
    /**
     * Generates and returns the predictions for the vehicle.
     *
     * @param vehicleState Contains the new match for the vehicle that the predictions are to be
     *                     based on.
     */
    List<IpcPrediction> generate(VehicleState vehicleState);
}
