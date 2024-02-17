/* (C)2023 */
package org.transitclock.core.predictiongenerator.frequency.traveltime.average;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.transitclock.ApplicationContext;
import org.transitclock.Core;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.config.data.PredictionConfig;
import org.transitclock.core.Indices;
import org.transitclock.core.SpatialMatch;
import org.transitclock.core.VehicleState;
import org.transitclock.core.dataCache.HistoricalAverage;
import org.transitclock.core.dataCache.StopPathCacheKey;
import org.transitclock.core.dataCache.StopPathPredictionCache;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.predictiongenerator.PredictionComponentElementsGenerator;
import org.transitclock.core.predictiongenerator.lastvehicle.LastVehiclePredictionGeneratorImpl;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.PredictionForStopPath;
import org.transitclock.utils.SystemTime;

/**
 * @author Sean Óg Crudden This provides a prediction based on the average of historical data for
 *     frequency based services. The average will be based on a trip id and a start time. The
 *     average will be based on time segments and previous trips during this time segment on
 *     previous days.. Each segment will be the duration of a single trip.
 */
@Slf4j
public class HistoricalAveragePredictionGeneratorImpl extends LastVehiclePredictionGeneratorImpl implements PredictionComponentElementsGenerator {
    private final String alternative = "LastVehiclePredictionGeneratorImpl";
    @Autowired
    protected FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache;

    /* (non-Javadoc)
     * @see org.transitclock.core.predictiongenerator.KalmanPredictionGeneratorImpl#getTravelTimeForPath(org.transitclock.core.Indices, org.transitclock.db.structs.AvlReport)
     */
    @Override
    public long getTravelTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {

        /*
         * if we have enough data start using historical average otherwise
         * revert to default. This does not mean that this method of
         * prediction is better than the default.
         */
        if (vehicleState.getTripStartTime(vehicleState.getTripCounter()) != null) {

            Integer time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(avlReport.getDate(), 2);

            /* this is what gets the trip from the buckets */
            time = FrequencyBasedHistoricalAverageCache.round(
                    time, CoreConfig.getCacheIncrementsForFrequencyService());

            StopPathCacheKey historicalAverageCacheKey =
                    new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex(), true, time.longValue());

            HistoricalAverage average = frequencyBasedHistoricalAverageCache.getAverage(historicalAverageCacheKey);

            if (average != null && average.getCount() >= PredictionConfig.minDays.getValue()) {
                if (CoreConfig.storeTravelTimeStopPathPredictions.getValue()) {
                    PredictionForStopPath predictionForStopPath = new PredictionForStopPath(
                            vehicleState.getVehicleId(),
                            SystemTime.getDate(),
                            average.getAverage(),
                            indices.getTrip().getId(),
                            indices.getStopPathIndex(),
                            "HISTORICAL AVERAGE",
                            true,
                            time);
                    Core.getInstance().getDbLogger().add(predictionForStopPath);
                    stopPathPredictionCache.putPrediction(predictionForStopPath);
                }

                logger.debug("Using historical average algorithm for prediction : {} for : {}", average, indices);
                // logger.debug("Instead of transitime value : " +
                // super.getTravelTimeForPath(indices, avlReport));
                return (long) average.getAverage();
            }
        }
        // logger.debug("No historical average found, generating prediction using lastvehicle
        // algorithm: " + historicalAverageCacheKey.toString());
        /* default to parent method if not enough data. This will be based on schedule if UpdateTravelTimes has not been called. */
        return super.getTravelTimeForPath(indices, avlReport, vehicleState);
    }

    @Override
    public long expectedTravelTimeFromMatchToEndOfStopPath(AvlReport avlReport, SpatialMatch match) {

        Indices indices = match.getIndices();
        int time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(new Date(match.getAvlTime()), 2);

        /* this is what gets the trip from the buckets */
        time = FrequencyBasedHistoricalAverageCache.round(
                time, CoreConfig.getCacheIncrementsForFrequencyService());

        StopPathCacheKey historicalAverageCacheKey =
                new StopPathCacheKey(indices.getTrip().getId(), indices.getStopPathIndex(), true, (long) time);

        HistoricalAverage average =
                frequencyBasedHistoricalAverageCache.getAverage(historicalAverageCacheKey);

        if (average != null && average.getCount() >= PredictionConfig.minDays.getValue()) {
            double fractionofstoppathlefttotravel = (match.getStopPath().getLength() - match.getDistanceAlongStopPath())
                    / match.getStopPath().getLength();
            double value = average.getAverage() * fractionofstoppathlefttotravel;
            if (CoreConfig.storeTravelTimeStopPathPredictions.getValue()) {
                PredictionForStopPath predictionForStopPath = new PredictionForStopPath(
                        avlReport.getVehicleId(),
                        SystemTime.getDate(),
                        value,
                        indices.getTrip().getId(),
                        indices.getStopPathIndex(),
                        "PARTIAL HISTORICAL AVERAGE",
                        true,
                        time);
                Core.getInstance().getDbLogger().add(predictionForStopPath);
                stopPathPredictionCache.putPrediction(predictionForStopPath);
            }
            return (long) value;
        } else {
            return super.expectedTravelTimeFromMatchToEndOfStopPath(avlReport, match);
        }
    }

    @Override
    public long getStopTimeForPath(Indices indices, AvlReport avlReport, VehicleState vehicleState) {

        if (vehicleState.getTripStartTime(vehicleState.getTripCounter()) != null) {
            int time = FrequencyBasedHistoricalAverageCache.secondsFromMidnight(avlReport.getDate(), 2);

            /* this is what gets the trip from the buckets */
            time = FrequencyBasedHistoricalAverageCache.round(
                    time, CoreConfig.getCacheIncrementsForFrequencyService());

            StopPathCacheKey historicalAverageCacheKey = new StopPathCacheKey(
                    indices.getTrip().getId(), indices.getStopPathIndex(), false, (long) time);

            HistoricalAverage average =
                    frequencyBasedHistoricalAverageCache.getAverage(historicalAverageCacheKey);

            if (average != null && average.getCount() >= PredictionConfig.minDays.getValue()) {
                if (CoreConfig.storeDwellTimeStopPathPredictions.getValue()) {
                    PredictionForStopPath predictionForStopPath = new PredictionForStopPath(
                            vehicleState.getVehicleId(),
                            SystemTime.getDate(),
                            average.getAverage(),
                            indices.getTrip().getId(),
                            indices.getStopPathIndex(),
                            "HISTORICAL AVERAGE",
                            false,
                            time);
                    Core.getInstance().getDbLogger().add(predictionForStopPath);
                    stopPathPredictionCache.putPrediction(predictionForStopPath);
                }

                logger.debug("Using historical average alogrithm for dwell time : {} for : {}", average, indices);
                return (long) average.getAverage();
            }
        }
        return super.getStopTimeForPath(indices, avlReport, vehicleState);
    }
}
