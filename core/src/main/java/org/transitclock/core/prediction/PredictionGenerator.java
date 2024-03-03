package org.transitclock.core.prediction;

import org.transitclock.core.VehicleStatus;
import org.transitclock.service.dto.IpcPrediction;

import java.util.List;

public interface PredictionGenerator {
    /**
     * Generates and returns the predictions for the vehicle.
     *
     * @param vehicleStatus Contains the new match for the vehicle that the predictions are to be
     *                     based on.
     */
    List<IpcPrediction> generate(VehicleStatus vehicleStatus);
}
