/* (C)2023 */
package org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.config.data.PredictionConfig;
import org.transitclock.core.*;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.core.predictiongenerator.bias.BiasAdjuster;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
import org.transitclock.core.predictiongenerator.kalman.*;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.PredictionEvent;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.SystemTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Sean Óg Crudden This is a prediction generator that uses a Kalman filter to provide
 *     predictions. It uses historical average while waiting on enough data to support a Kalman
 *     filter.
 */
@Slf4j
public class KalmanPredictionGeneratorImpl extends PredictionGeneratorDefaultImpl {

    private final String alternative = "PredictionGeneratorDefaultImpl";

    protected final ErrorCache kalmanErrorCache;

    public KalmanPredictionGeneratorImpl(DbConfig dbConfig,
                                         StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                         TripDataHistoryCacheInterface tripDataHistoryCacheInterface,
                                         HoldingTimeCache holdingTimeCache,
                                         ErrorCache kalmanErrorCache,
                                         StopPathPredictionCache stopPathPredictionCache,
                                         VehicleStateManager vehicleStateManager,
                                         TravelTimes travelTimes,
                                         DataDbLogger dataDbLogger,
                                         HoldingTimeGenerator holdingTimeGenerator,
                                         BiasAdjuster biasAdjuster,
                                         TravelTimeDataFilter travelTimeDataFilter) {
        super(dbConfig, stopArrivalDepartureCacheInterface, tripDataHistoryCacheInterface, vehicleStateManager, holdingTimeCache, stopPathPredictionCache, travelTimes, dataDbLogger, holdingTimeGenerator, biasAdjuster, travelTimeDataFilter);
        this.kalmanErrorCache = kalmanErrorCache;
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * org.transitclock.core.PredictionGeneratorDefaultImpl#getTravelTimeForPath
     * (org.transitclock.core.Indices, org.transitclock.db.structs.AvlReport)
     */
    @Override
    public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {

        logger.debug("Calling Kalman prediction algorithm for : {}", indices);

        long alternatePrediction = super.getTravelTimeForPath(indices, avlReport, vehicleState);
        var currentVehicleState = vehicleStateManager.getVehicleState(avlReport.getVehicleId());

        try {
            TravelTimeDetails travelTimeDetails = this.getLastVehicleTravelTime(currentVehicleState, indices);

            /*
             * The first vehicle of the day should use schedule or historic data to
             * make prediction. Cannot use Kalman as yesterday's vehicle will have
             * little to say about today's.
             */
            if (travelTimeDetails != null) {
                logger.debug("Kalman has last vehicle info for : {} : {}", indices, travelTimeDetails);
                Date nearestDay = DateUtils.truncate(avlReport.getDate(), Calendar.DAY_OF_MONTH);

                List<TravelTimeDetails> lastDaysTimes = lastDaysTimes(
                        tripDataHistoryCacheInterface,
                        currentVehicleState.getTrip().getId(),
                        currentVehicleState.getTrip().getDirectionId(),
                        indices.getStopPathIndex(),
                        nearestDay,
                        currentVehicleState.getTrip().getStartTime(),
                        PredictionConfig.maxKalmanDaysToSearch.getValue(),
                        PredictionConfig.maxKalmanDays.getValue());

                if (lastDaysTimes != null) {
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

                            VehicleStopDetail destinationDetail = new VehicleStopDetail(null, lastDaysTimes.get(i).getTravelTime(), vehicle);
                            historical_segments_k[lastDaysTimes.size() - i - 1] = new TripSegment(originDetail, destinationDetail);
                        }

                        VehicleStopDetail destinationDetail_0_k_1 =
                                new VehicleStopDetail(null, travelTimeDetails.getTravelTime(), vehicle);

                        TripSegment ts_day_0_k_1 = new TripSegment(originDetail, destinationDetail_0_k_1);
                        TripSegment last_vehicle_segment = ts_day_0_k_1;
                        Indices previousVehicleIndices = new Indices(travelTimeDetails.getArrival());

                        KalmanError last_prediction_error =
                                lastVehiclePredictionError(kalmanErrorCache, previousVehicleIndices);

                        logger.debug("Using error value: {} found with vehicle id {} from: {}",
                                last_prediction_error, travelTimeDetails.getArrival().getVehicleId(),
                                new KalmanErrorCacheKey(previousVehicleIndices));

                        // TODO this should also display the detail of which vehicle it choose as
                        // the last one.
                        logger.debug("Using last vehicle value: {} for : {}", travelTimeDetails, indices);

                        kalmanPredictionResult = kalmanPrediction.predict(
                                last_vehicle_segment, historical_segments_k, last_prediction_error.getError());

                        long predictionTime = (long) kalmanPredictionResult.getResult();

                        logger.debug("Setting Kalman error value: {} for : {}", kalmanPredictionResult.getFilterError(), new KalmanErrorCacheKey(indices));

                        kalmanErrorCache.putErrorValue(indices, kalmanPredictionResult.getFilterError());

                        double percentageDifferecence =
                                Math.abs(100 * ((predictionTime - alternatePrediction) / (double) alternatePrediction));

                        if (((percentageDifferecence * alternatePrediction) / 100) > PredictionConfig.tresholdForDifferenceEventLog.getValue()) {
                            if (percentageDifferecence > PredictionConfig.percentagePredictionMethodDifferenceneEventLog.getValue()) {
                                String description = "Kalman predicts : " + predictionTime + " Super predicts : " + alternatePrediction;

                                logger.warn(description);

                                PredictionEvent.create(
                                        avlReport,
                                        vehicleState.getMatch(),
                                        PredictionEvent.PREDICTION_VARIATION,
                                        description,
                                        travelTimeDetails.getArrival().getStopId(),
                                        travelTimeDetails.getDeparture().getStopId(),
                                        travelTimeDetails.getArrival().getVehicleId(),
                                        travelTimeDetails.getArrival().getTime(),
                                        travelTimeDetails.getDeparture().getTime());
                            }
                        }

                        logger.debug("Using Kalman prediction: {} instead of " + alternative + " prediction: {} for : {}", predictionTime, alternatePrediction, indices);

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
                            dataDbLogger.add(predictionForStopPath);
                            stopPathPredictionCache.putPrediction(predictionForStopPath);
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

        KalmanError result;
        try {
            result = cache.getErrorValue(indices);
            if (result == null) {
                logger.debug("Kalman Error value set to default: {} for key: {}", PredictionConfig.initialErrorValue.getValue(), new KalmanErrorCacheKey(indices));
                result = new KalmanError(PredictionConfig.initialErrorValue.getValue());
            }
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {

        return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
}
