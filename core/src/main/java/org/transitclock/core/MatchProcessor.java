/* (C)2023 */
package org.transitclock.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.ApplicationContext;
import org.transitclock.Core;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.Headway;
import org.transitclock.domain.structs.Match;
import org.transitclock.domain.structs.Prediction;
import org.transitclock.service.dto.IpcPrediction;
import org.transitclock.utils.Time;

import java.util.List;

/**
 * For generating predictions, arrival/departure times, headways etc. This class is used once a
 * vehicle is successfully matched to an assignment.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class MatchProcessor {
    @Autowired
    private DataDbLogger dbLogger;
    @Autowired
    private PredictionDataCache predictionDataCache;
    @Autowired
    private HeadwayGenerator headwayGenerator;
    @Autowired
    private ArrivalDepartureGenerator arrivalDepartureGenerator;
    @Autowired
    private PredictionGenerator predictionGenerator;

    /**
     * Generates the new predictions for the vehicle based on the new match stored in the vehicle
     * state. Updates vehicle state, the predictions cache, and stores predictions in database.
     *
     * @param vehicleState
     */
    private void processPredictions(VehicleState vehicleState) {
        logger.debug("Processing predictions for vehicleId={}", vehicleState.getVehicleId());

        // Generate the new predictions for the vehicle
        List<IpcPrediction> newPredictions = predictionGenerator.generate(vehicleState);

        // Store the predictions in database if so configured
        if (CoreConfig.getMaxPredictionsTimeForDbSecs() > 0) {
            for (IpcPrediction prediction : newPredictions) {
                // If prediction not too far into the future then ...
                if (prediction.getPredictionTime() - prediction.getAvlTime()
                        < ((long) CoreConfig.getMaxPredictionsTimeForDbSecs() * Time.MS_PER_SEC)) {
                    dbLogger.add(new Prediction(prediction));

                } else {
                    logger.debug(
                            "Difference in predictionTiem and AVLTime is {} and is greater than"
                                    + " getMaxPredictionsTimeForDbSecs {}.",
                            prediction.getPredictionTime() - prediction.getAvlTime(),
                            CoreConfig.getMaxPredictionsTimeForDbSecs() * Time.MS_PER_SEC);
                }
            }
        }

        // Update the predictions cache to use the new predictions for the
        // vehicle
        List<IpcPrediction> oldPredictions = vehicleState.getPredictions();
        predictionDataCache.updatePredictions(oldPredictions, newPredictions);

        // Update predictions for vehicle
        vehicleState.setPredictions(newPredictions);
    }

    /**
     * Generates the headway info based on the new match stored in the vehicle state.
     *
     * @param vehicleState
     */
    private void processHeadways(VehicleState vehicleState) {
        logger.debug("Processing headways for vehicleId={}", vehicleState.getVehicleId());

        Headway headway = headwayGenerator.generate(vehicleState);

        if (headway != null) {
            vehicleState.setHeadway(headway);
            dbLogger.add(headway);
        }
    }

    /**
     * Generates the arrival/departure info based on the new match stored in the vehicle state.
     * Stores the arrival/departure info into database.
     *
     * @param vehicleState
     */
    private void processArrivalDepartures(VehicleState vehicleState) {
        logger.debug("Processing arrivals/departures for vehicleId={}", vehicleState.getVehicleId());

        arrivalDepartureGenerator.generate(vehicleState);
    }

    /**
     * Stores the spatial match in log file and to database so can be processed later to determine
     * expected travel times.
     *
     * @param vehicleState
     */
    private void processSpatialMatch(VehicleState vehicleState) {
        logger.debug("Processing spatial match for vehicleId={}", vehicleState.getVehicleId());

        Match match = new Match(vehicleState);
        logger.debug("{}", match);

        // Store match in database if it is not at a stop. The reason only
        // storing to db if not at a stop is because reason for storing
        // matches is for determining travel times. But when determining
        // travel times using departure and arrival times at the stops.
        // The matches are only used for in between the stops. And in fact,
        // matches at stops only confuse things since they will be before
        // the departure time or after the arrival time. Plus not storing
        // the matches at the stops means there is less data to store.
        if (!match.isAtStop()) {
            dbLogger.add(match);
        }
    }

    /**
     * Called when vehicle is matched successfully. Generates predictions arrival/departure times,
     * headways and such. But if vehicle should be ignored because part of consist then don't do
     * anything.
     *
     * @param vehicleState
     */
    public void generateResultsOfMatch(VehicleState vehicleState) {
        // Make sure everything ok
        if (!vehicleState.isPredictable()) {
            logger.error(
                    "Vehicle was unpredictable when " + "MatchProcessor.generateResultsOfMatch() was called. {}",
                    vehicleState);
            return;
        }

        // If non-lead vehicle in a consist then don't need to do anything here
        // because all the info will be generated by the lead vehicle.
        if (vehicleState.getAvlReport().ignoreBecauseInConsist()) {
            logger.debug(
                    "Not generating results such as predictions for "
                            + "vehicleId={} because it is a non-lead vehicle in "
                            + "a consist.",
                    vehicleState.getVehicleId());
            return;
        }

        logger.debug("Processing results for match for {}", vehicleState);

        // Process predictions, headways, arrivals/departures, and and spatial
        // matches. If don't need matches then don't store them
        if (!CoreConfig.onlyNeedArrivalDepartures()) {
            processPredictions(vehicleState);
            processHeadways(vehicleState);
            processSpatialMatch(vehicleState);
        }
        processArrivalDepartures(vehicleState);
    }
}
