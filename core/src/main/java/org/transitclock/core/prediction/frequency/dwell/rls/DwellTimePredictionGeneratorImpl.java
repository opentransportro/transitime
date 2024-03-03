/* (C)2023 */
package org.transitclock.core.prediction.frequency.dwell.rls;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.core.Indices;
import org.transitclock.core.avl.RealTimeSchedAdhProcessor;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.VehicleStatus;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.core.prediction.bias.BiasAdjuster;
import org.transitclock.core.prediction.datafilter.TravelTimeDataFilter;
import org.transitclock.core.prediction.frequency.traveltime.kalman.KalmanPredictionGeneratorImpl;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Headway;
import org.transitclock.gtfs.DbConfig;

/**
 * @author Sean Og Crudden
 *     <p>This is an experiment to see if headway can be used to better predict dwell time. Most of
 *     what I have read tells me it can but in conjunction with APC data and estimation of demand at
 *     stops.
 *     <p>This is for frequency based services.
 */
@Slf4j
public class DwellTimePredictionGeneratorImpl extends KalmanPredictionGeneratorImpl {
    protected final DwellTimeModelCacheInterface dwellTimeModelCacheInterface;

    public DwellTimePredictionGeneratorImpl(StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                            TripDataHistoryCacheInterface tripDataHistoryCacheInterface,
                                            DbConfig dbConfig,
                                            DataDbLogger dataDbLogger,
                                            TravelTimeDataFilter travelTimeDataFilter,
                                            VehicleDataCache vehicleCache, HoldingTimeCache holdingTimeCache, StopPathPredictionCache stopPathPredictionCache, TravelTimes travelTimes, HoldingTimeGenerator holdingTimeGenerator, VehicleStatusManager vehicleStatusManager, RealTimeSchedAdhProcessor realTimeSchedAdhProcessor, BiasAdjuster biasAdjuster, FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache, ErrorCache kalmanErrorCache, DwellTimeModelCacheInterface dwellTimeModelCacheInterface) {
        super(stopArrivalDepartureCacheInterface, tripDataHistoryCacheInterface, dbConfig, dataDbLogger, travelTimeDataFilter, vehicleCache, holdingTimeCache, stopPathPredictionCache, travelTimes, holdingTimeGenerator, vehicleStatusManager, realTimeSchedAdhProcessor, biasAdjuster, frequencyBasedHistoricalAverageCache, kalmanErrorCache);
        this.dwellTimeModelCacheInterface = dwellTimeModelCacheInterface;
    }

    @Override
    public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleStatus vehicleStatus) {
        Long result = null;
        try {
            Headway headway = vehicleStatus.getHeadway();

            if (headway != null) {
                logger.debug("Headway at {} based on avl {} is {}.", indices, avlReport, headway);

                /* Change approach to use a RLS model.
                 */
                if (super.getStopTimeForPath(indices, avlReport, vehicleStatus) > 0) {
                    // TODO Would be more correct to use the start time of the trip.
                    int time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(new Date(avlReport.getTime()), 2);

                    time = FrequencyBasedHistoricalAverageCache.round(time, CoreConfig.getCacheIncrementsForFrequencyService());

                    StopPathCacheKey cacheKey = new StopPathCacheKey(
                            indices.getTrip().getId(), indices.getStopPathIndex(), false, (long) time);

                    if (dwellTimeModelCacheInterface != null)
                        result = dwellTimeModelCacheInterface.predictDwellTime(cacheKey, headway);

                    if (result == null) {
                        logger.debug(
                                "Using scheduled value for dwell time as no RLS data available for" + " {}.", indices);
                        result = super.getStopTimeForPath(indices, avlReport, vehicleStatus);
                    }

                    /* should never have a negative dwell time */
                    if (result < 0) {
                        logger.debug("Predicted negative dwell time {} for {}.", result, indices);
                        result = 0L;
                    }

                } else {
                    logger.debug("Scheduled dwell time is less than 0 for {}.", indices);
                    result = super.getStopTimeForPath(indices, avlReport, vehicleStatus);
                }

            } else {
                result = super.getStopTimeForPath(indices, avlReport, vehicleStatus);
                logger.debug(
                        "Using dwell time {} for {} instead of {}. No headway.",
                        result,
                        indices,
                        super.getStopTimeForPath(indices, avlReport, vehicleStatus));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

        return result;
    }
}
