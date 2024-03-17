/* (C)2023 */
package org.transitclock.core.prediction.scheduled.average;

import lombok.extern.slf4j.Slf4j;

import org.transitclock.ApplicationProperties;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.Indices;
import org.transitclock.core.avl.RealTimeSchedAdhProcessor;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.VehicleStatus;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.core.prediction.PredictionComponentElementsGenerator;
import org.transitclock.core.prediction.bias.BiasAdjuster;
import org.transitclock.core.prediction.datafilter.TravelTimeDataFilter;
import org.transitclock.core.prediction.lastvehicle.LastVehiclePredictionGeneratorImpl;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.SystemTime;

/**
 * @author Sean Óg Crudden This provides a prediction based on the average of historical data for
 *     schedules based services. The average is taken from the HistoricalAverageCache which is
 *     populated each time an arrival/departure event occurs. The HistoricalAverageCache is updated
 *     using data from the TripDataHistory cache.
 */
@Slf4j
public class HistoricalAveragePredictionGeneratorImpl extends LastVehiclePredictionGeneratorImpl implements PredictionComponentElementsGenerator {
    protected final ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
    private final String alternative = "LastVehiclePredictionGeneratorImpl";

    public HistoricalAveragePredictionGeneratorImpl(StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                    TripDataHistoryCacheInterface tripDataHistoryCacheInterface,
                                                    DbConfig dbConfig,
                                                    DataDbLogger dataDbLogger,
                                                    TravelTimeDataFilter travelTimeDataFilter,
                                                    ApplicationProperties.Prediction properties,
                                                    VehicleDataCache vehicleCache, HoldingTimeCache holdingTimeCache, StopPathPredictionCache stopPathPredictionCache, TravelTimes travelTimes, HoldingTimeGenerator holdingTimeGenerator, VehicleStatusManager vehicleStatusManager, RealTimeSchedAdhProcessor realTimeSchedAdhProcessor, BiasAdjuster biasAdjuster, ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache) {
        super(stopArrivalDepartureCacheInterface, tripDataHistoryCacheInterface, dbConfig, dataDbLogger, travelTimeDataFilter, properties, vehicleCache, holdingTimeCache, stopPathPredictionCache, travelTimes, holdingTimeGenerator, vehicleStatusManager, realTimeSchedAdhProcessor, biasAdjuster);
        this.scheduleBasedHistoricalAverageCache = scheduleBasedHistoricalAverageCache;
    }

    /* (non-Javadoc)
     * @see org.transitclock.core.predictiongenerator.KalmanPredictionGeneratorImpl#getTravelTimeForPath(org.transitclock.core.Indices, org.transitclock.db.structs.AvlReport)
     */
    @Override
    public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleStatus vehicleStatus) {

        logger.debug("Calling historical average algorithm : {}", indices.toString());
        /*
         * if we have enough data start using historical average otherwise
         * revert to default. This does not mean that this method of
         * prediction is better than the default.
         */
        StopPathCacheKey historicalAverageCacheKey =
                new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex());

        HistoricalAverage average = scheduleBasedHistoricalAverageCache.getAverage(historicalAverageCacheKey);

        if (average != null && average.getCount() >= predictionProperties.getData().getAverage().getMindays()) {
            if (CoreConfig.storeTravelTimeStopPathPredictions.getValue()) {
                PredictionForStopPath predictionForStopPath = new PredictionForStopPath(
                        vehicleStatus.getVehicleId(),
                        SystemTime.getDate(),
                        average.getAverage(),
                        indices.getTrip().getId(),
                        indices.getStopPathIndex(),
                        "HISTORICAL AVERAGE",
                        true,
                        null);
                dataDbLogger.add(predictionForStopPath);
                stopPathPredictionCache.putPrediction(predictionForStopPath);
            }

            logger.debug("Using historical average algorithm for prediction : {} instead of {} prediction: {} for : {}", average, alternative, super.getTravelTimeForPath(indices, avlReport, vehicleStatus), indices);

            return (long) average.getAverage();
        }

        // logger.debug("No historical average found, generating prediction using lastvehicle
        // algorithm: " + historicalAverageCacheKey.toString());
        /* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
        return super.getTravelTimeForPath(indices, avlReport, vehicleStatus);
    }

    @Override
    public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleStatus vehicleStatus) {

        StopPathCacheKey historicalAverageCacheKey =
                new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex(), false);

        HistoricalAverage average = scheduleBasedHistoricalAverageCache.getAverage(historicalAverageCacheKey);

        if (average != null && average.getCount() >= predictionProperties.getData().getAverage().getMindays()) {
            logger.debug("Using historical average alogrithm for dwell time prediction : {} instead of {} prediction: {} for : {}", average, alternative, super.getStopTimeForPath(indices, avlReport, vehicleStatus), indices);
            return (long) average.getAverage();
        }

        return super.getStopTimeForPath(indices, avlReport, vehicleStatus);
    }
}
