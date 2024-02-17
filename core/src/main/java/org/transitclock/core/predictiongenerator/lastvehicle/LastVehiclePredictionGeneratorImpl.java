/* (C)2023 */
package org.transitclock.core.predictiongenerator.lastvehicle;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.transitclock.ApplicationContext;
import org.transitclock.Core;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.Indices;
import org.transitclock.core.PredictionGeneratorDefaultImpl;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.StopPathPredictionCache;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.core.dataCache.VehicleStateManager;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.service.dto.IpcVehicleComplete;
import org.transitclock.utils.SystemTime;

/**
 * @author Sean Óg Crudden This provides a prediction based on the time it took the previous vehicle
 *     on the same route to cover the same ground. This is another step to get to Kalman
 *     implementation.
 *     <p>TODO Debug as this has yet to be tried and tested. Could do a combination with historical
 *     average so that it improves quickly rather than just waiting on having enough data to support
 *     average or Kalman. So do a progression from LastVehicle --> Historical Average --> Kalman.
 *     Might be interesting to look at the rate of improvement of prediction as well as the end
 *     result.
 *     <p>Does this by changing which class each extends. How can we make configurable?
 *     <p>This works for both schedules based and frequency based services out of the box. Not so
 *     for historical average or Kalman filter.
 */
@Slf4j
public class LastVehiclePredictionGeneratorImpl extends PredictionGeneratorDefaultImpl {
    @Autowired
    protected VehicleDataCache vehicleCache;
    @Autowired
    protected VehicleStateManager vehicleStateManager;

    @Override
    protected IpcPrediction generatePredictionForStop(
            AvlReport avlReport,
            Indices indices,
            long predictionTime,
            boolean useArrivalTimes,
            boolean affectedByWaitStop,
            boolean isDelayed,
            boolean lateSoMarkAsUncertain,
            int tripCounter,
            Integer scheduleDeviation) {
        // TODO Auto-generated method stub
        return super.generatePredictionForStop(
                avlReport,
                indices,
                predictionTime,
                useArrivalTimes,
                affectedByWaitStop,
                isDelayed,
                lateSoMarkAsUncertain,
                tripCounter,
                scheduleDeviation);
    }

    private final String alternative = "PredictionGeneratorDefaultImpl";

    @Override
    public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
        List<VehicleState> vehiclesOnRoute = new ArrayList<>();
        VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());

        for (IpcVehicleComplete vehicle :
                emptyIfNull(vehicleCache.getVehiclesForRoute(currentVehicleState.getRouteId()))) {
            VehicleState vehicleOnRouteState = vehicleStateManager.getVehicleState(vehicle.getId());
            vehiclesOnRoute.add(vehicleOnRouteState);
        }

        try {
            TravelTimeDetails travelTimeDetails = null;
            if ((travelTimeDetails = this.getLastVehicleTravelTime(currentVehicleState, indices)) != null) {
                logger.debug("Using last vehicle algorithm for prediction : {} for : {}", travelTimeDetails, indices);

                if (CoreConfig.storeTravelTimeStopPathPredictions.getValue()) {
                    PredictionForStopPath predictionForStopPath = new PredictionForStopPath(
                            vehicleState.getVehicleId(),
                            SystemTime.getDate(),
                            (double) Long.valueOf(travelTimeDetails.getTravelTime()).intValue(),
                            indices.getTrip().getId(),
                            indices.getStopPathIndex(),
                            "LAST VEHICLE",
                            true,
                            null);

                    Core.getInstance().getDbLogger().add(predictionForStopPath);
                    stopPathPredictionCache.putPrediction(predictionForStopPath);
                }

                return travelTimeDetails.getTravelTime();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
        return super.getTravelTimeForPath(indices, avlReport, currentVehicleState);
    }

    @Override
    public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {
        // Looking at last vehicle value would be a bad idea for dwell time, so no implementation
        // here.
        return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
}
