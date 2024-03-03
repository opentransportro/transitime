/* (C)2023 */
package org.transitclock.core.avl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.avl.ad.ArrivalDepartureGenerator;
import org.transitclock.core.prediction.PredictionGenerator;
import org.transitclock.core.VehicleStatus;
import org.transitclock.core.dataCache.PredictionDataCache;
import org.transitclock.core.headwaygenerator.HeadwayGenerator;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.Headway;
import org.transitclock.domain.structs.Match;
import org.transitclock.domain.structs.Prediction;
import org.transitclock.gtfs.DbConfig;
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
    private final DataDbLogger dbLogger;
    private final DbConfig dbConfig;
    private final PredictionDataCache predictionDataCache;
    private final HeadwayGenerator headwayGenerator;
    private final ArrivalDepartureGenerator arrivalDepartureGenerator;
    private final PredictionGenerator predictionGenerator;

    public MatchProcessor(DataDbLogger dbLogger, DbConfig dbConfig, PredictionDataCache predictionDataCache, HeadwayGenerator headwayGenerator, ArrivalDepartureGenerator arrivalDepartureGenerator, PredictionGenerator predictionGenerator) {
        this.dbLogger = dbLogger;
        this.dbConfig = dbConfig;
        this.predictionDataCache = predictionDataCache;
        this.headwayGenerator = headwayGenerator;
        this.arrivalDepartureGenerator = arrivalDepartureGenerator;
        this.predictionGenerator = predictionGenerator;
    }

    /**
     * Generates the new predictions for the vehicle based on the new match stored in the vehicle
     * state. Updates vehicle state, the predictions cache, and stores predictions in database.
     */
    private void processPredictions(VehicleStatus vehicleStatus) {
        logger.debug("Processing predictions for vehicleId={}", vehicleStatus.getVehicleId());

        // Generate the new predictions for the vehicle
        List<IpcPrediction> newPredictions = predictionGenerator.generate(vehicleStatus);

        // Store the predictions in database if so configured
        if (CoreConfig.getMaxPredictionsTimeForDbSecs() > 0) {
            for (IpcPrediction prediction : newPredictions) {
                // If prediction not too far into the future then ...
                if (prediction.getPredictionTime() - prediction.getAvlTime()
                        < ((long) CoreConfig.getMaxPredictionsTimeForDbSecs() * Time.MS_PER_SEC)) {
                    dbLogger.add(new Prediction(dbConfig.getConfigRev(), prediction));

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
        List<IpcPrediction> oldPredictions = vehicleStatus.getPredictions();
        predictionDataCache.updatePredictions(oldPredictions, newPredictions);

        // Update predictions for vehicle
        vehicleStatus.setPredictions(newPredictions);
    }

    /**
     * Generates the headway info based on the new match stored in the vehicle state.
     */
    private void processHeadways(VehicleStatus vehicleStatus) {
        logger.debug("Processing headways for vehicleId={}", vehicleStatus.getVehicleId());

        Headway headway = headwayGenerator.generate(vehicleStatus);

        if (headway != null) {
            vehicleStatus.setHeadway(headway);
            dbLogger.add(headway);
        }
    }

    /**
     * Generates the arrival/departure info based on the new match stored in the vehicle state.
     * Stores the arrival/departure info into database.
     */
    private void processArrivalDepartures(VehicleStatus vehicleStatus) {
        logger.debug("Processing arrivals/departures for vehicleId={}", vehicleStatus.getVehicleId());

        arrivalDepartureGenerator.generate(vehicleStatus);
    }

    /**
     * Stores the spatial match in log file and to database so can be processed later to determine
     * expected travel times.
     */
    private void processSpatialMatch(VehicleStatus vehicleStatus) {
        logger.debug("Processing spatial match for vehicleId={}", vehicleStatus.getVehicleId());

        Match match = new Match(vehicleStatus, dbConfig.getConfigRev());
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
     */
    public void generateResultsOfMatch(VehicleStatus vehicleStatus) {
        // Make sure everything ok
        if (!vehicleStatus.isPredictable()) {
            logger.error(
                    "Vehicle was unpredictable when " + "MatchProcessor.generateResultsOfMatch() was called. {}",
                vehicleStatus);
            return;
        }

        // If non-lead vehicle in a consist then don't need to do anything here
        // because all the info will be generated by the lead vehicle.
        if (vehicleStatus.getAvlReport().ignoreBecauseInConsist()) {
            logger.debug(
                    "Not generating results such as predictions for "
                            + "vehicleId={} because it is a non-lead vehicle in "
                            + "a consist.",
                    vehicleStatus.getVehicleId());
            return;
        }

        logger.debug("Processing results for match for {}", vehicleStatus);

        // Process predictions, headways, arrivals/departures, and and spatial
        // matches. If don't need matches then don't store them
        if (!CoreConfig.onlyNeedArrivalDepartures()) {
            processPredictions(vehicleStatus);
            processHeadways(vehicleStatus);
            processSpatialMatch(vehicleStatus);
        }
        processArrivalDepartures(vehicleStatus);
    }
}
