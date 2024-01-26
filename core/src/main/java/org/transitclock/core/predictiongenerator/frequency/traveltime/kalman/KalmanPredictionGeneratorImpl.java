/* (C)2023 */
package org.transitclock.core.predictiongenerator.frequency.traveltime.kalman;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.transitclock.applications.Core;
import org.transitclock.configData.CoreConfig;
import org.transitclock.configData.PredictionConfig;
import org.transitclock.core.Indices;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.TravelTimeDetails;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitclock.core.predictiongenerator.frequency.traveltime.average.HistoricalAveragePredictionGeneratorImpl;
import org.transitclock.core.predictiongenerator.kalman.*;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.PredictionForStopPath;
import org.transitclock.db.structs.VehicleEvent;
import org.transitclock.utils.SystemTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Óg Crudden This is a prediction generator that uses a Kalman filter to provide
 *     predictions for a frequency based service.
 */
@Slf4j
public class KalmanPredictionGeneratorImpl extends HistoricalAveragePredictionGeneratorImpl
        implements PredictionComponentElementsGenerator {

    private final String alternative = "LastVehiclePredictionGeneratorImpl";

    /*
     * (non-Javadoc)
     *
     * @see
     * org.transitclock.core.PredictionGeneratorDefaultImpl#getTravelTimeForPath
     * (org.transitclock.core.Indices, org.transitclock.db.structs.AvlReport)
     */
    @Override
    public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {

        logger.debug("Calling frequency based Kalman prediction algorithm for : {}", indices.toString());

        long alternatePrediction = super.getTravelTimeForPath(indices, avlReport, vehicleState);

        Integer time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(avlReport.getDate(), 2);

        time = FrequencyBasedHistoricalAverageCache.round(
                time, CoreConfig.getCacheIncrementsForFrequencyService());

        TripDataHistoryCacheInterface tripCache = TripDataHistoryCacheFactory.getInstance();

        ErrorCache kalmanErrorCache = ErrorCacheFactory.getInstance();

        VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();

        VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());

        try {
            TravelTimeDetails travelTimeDetails = this.getLastVehicleTravelTime(currentVehicleState, indices);

            /*
             * The first vehicle of the day should use schedule or historic data to
             * make prediction. Cannot use Kalman as yesterdays vehicle will have
             * little to say about todays.
             */
            if (travelTimeDetails != null) {

                logger.debug("Kalman has last vehicle info for : {} : {}", indices, travelTimeDetails);

                Date nearestDay = DateUtils.truncate(avlReport.getDate(), Calendar.DAY_OF_MONTH);

                List<TravelTimeDetails> lastDaysTimes = lastDaysTimes(
                        tripCache,
                        currentVehicleState.getTrip().getId(),
                        currentVehicleState.getTrip().getDirectionId(),
                        indices.getStopPathIndex(),
                        nearestDay,
                        time,
                        PredictionConfig.maxKalmanDaysToSearch.getValue(),
                        PredictionConfig.maxKalmanDays.getValue());

                if (lastDaysTimes != null && !lastDaysTimes.isEmpty()) {
                    logger.debug("Kalman has {} historical values for : {}", lastDaysTimes.size(), indices);
                }
                /*
                 * if we have enough data start using Kalman filter otherwise revert
                 * to extended class for prediction.
                 */
                if (lastDaysTimes != null && lastDaysTimes.size() >= PredictionConfig.minKalmanDays.getValue()) {

                    logger.debug("Generating Kalman prediction for : {}", indices);

                    try {

                        KalmanPrediction kalmanPrediction = new KalmanPrediction();

                        KalmanPredictionResult kalmanPredictionResult;

                        Vehicle vehicle = new Vehicle(avlReport.getVehicleId());

                        VehicleStopDetail originDetail = new VehicleStopDetail(null, 0, vehicle);
                        TripSegment[] historical_segments_k = new TripSegment[lastDaysTimes.size()];
                        for (int i = 0; i < lastDaysTimes.size() && i < PredictionConfig.maxKalmanDays.getValue(); i++) {

                            logger.debug("Kalman is using historical value : {} for : {}", lastDaysTimes.get(i), indices);

                            VehicleStopDetail destinationDetail = new VehicleStopDetail(
                                    null, lastDaysTimes.get(i).getTravelTime(), vehicle);
                            historical_segments_k[i] = new TripSegment(originDetail, destinationDetail);
                        }

                        VehicleStopDetail destinationDetail_0_k_1 =
                                new VehicleStopDetail(null, travelTimeDetails.getTravelTime(), vehicle);

                        TripSegment ts_day_0_k_1 = new TripSegment(originDetail, destinationDetail_0_k_1);

                        TripSegment last_vehicle_segment = ts_day_0_k_1;

                        Indices previousVehicleIndices = new Indices(travelTimeDetails.getArrival());

                        KalmanError last_prediction_error =
                                lastVehiclePredictionError(kalmanErrorCache, previousVehicleIndices);

                        logger.debug("Using error value: {} found with vehicle id {} from: {}", last_prediction_error, travelTimeDetails.getArrival().getVehicleId(), new KalmanErrorCacheKey(previousVehicleIndices));

                        kalmanPredictionResult = kalmanPrediction.predict(
                                last_vehicle_segment, historical_segments_k, last_prediction_error.getError());

                        long predictionTime = (long) kalmanPredictionResult.getResult();

                        logger.debug("Setting Kalman error value: {} for : {}", kalmanPredictionResult.getFilterError(), new KalmanErrorCacheKey(indices));

                        kalmanErrorCache.putErrorValue(indices, kalmanPredictionResult.getFilterError());

                        logger.debug("Using Kalman prediction: {} instead of {} prediction: {} for : {}", predictionTime, alternative, alternatePrediction, indices);

                        double percentageDifferecence =
                                100 * ((predictionTime - alternatePrediction) / (double) alternatePrediction);

                        if (Math.abs(percentageDifferecence) > PredictionConfig.percentagePredictionMethodDifferenceneEventLog.getValue()) {
                            String description = "Predictions for "
                                    + indices
                                    + " have more that a "
                                    + PredictionConfig.percentagePredictionMethodDifferenceneEventLog.getValue()
                                    + " difference. Kalman predicts : "
                                    + predictionTime
                                    + " Super predicts : "
                                    + alternatePrediction;
                            VehicleEvent.create(
                                    vehicleState.getAvlReport(),
                                    vehicleState.getMatch(),
                                    VehicleEvent.PREDICTION_VARIATION,
                                    description,
                                    true, // predictable
                                    false, // becameUnpredictable
                                    null); // supervisor
                        }

                        if (CoreConfig.storeTravelTimeStopPathPredictions.getValue()) {
                            PredictionForStopPath predictionForStopPath = new PredictionForStopPath(
                                    vehicleState.getVehicleId(),
                                    SystemTime.getDate(),
                                    (double) Long.valueOf(predictionTime).intValue(),
                                    indices.getTrip().getId(),
                                    indices.getStopPathIndex(),
                                    "KALMAN",
                                    true,
                                    null);
                            Core.getInstance().getDbLogger().add(predictionForStopPath);
                            StopPathPredictionCache.getInstance().putPrediction(predictionForStopPath);
                        }
                        return predictionTime;

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return alternatePrediction;
    }

    @Override
    public long expectedTravelTimeFromMatchToEndOfStopPath(AvlReport avlReport, SpatialMatch match) {

        if (PredictionConfig.useKalmanForPartialStopPaths.getValue()) {
            VehicleStateManager vehicleStateManager = VehicleStateManager.getInstance();

            VehicleState currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());

            long fulltime = this.getTravelTimeForPath(match.getIndices(), avlReport, currentVehicleState);

            double distanceAlongStopPath = match.getDistanceAlongStopPath();

            double stopPathLength = match.getStopPath().getLength();

            long remainingtime = (long) (fulltime * ((stopPathLength - distanceAlongStopPath) / stopPathLength));

            logger.debug(
                    "Using Kalman for first stop path {} with value {} instead of {}.",
                    match.getIndices(),
                    remainingtime,
                    super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match));

            return remainingtime;
        } else {
            return super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match);
        }
    }

    private KalmanError lastVehiclePredictionError(ErrorCache cache, Indices indices) {

        KalmanError result = cache.getErrorValue(indices);
        if (result == null) {
            logger.debug("Kalman Error value set to default: {} for key: {}", PredictionConfig.initialErrorValue.getValue(), new KalmanErrorCacheKey(indices));
            result = new KalmanError(PredictionConfig.initialErrorValue.getValue());
        }
        return result;
    }
}
