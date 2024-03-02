/* (C)2023 */
package org.transitclock.core.predictiongenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.avl.RealTimeSchedAdhProcessor;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.core.predictiongenerator.bias.BiasAdjuster;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
import org.transitclock.core.predictiongenerator.lastvehicle.LastVehiclePredictionGeneratorImpl;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.gtfs.DbConfig;

/**
 * For instantiating a PredictionGenerator object that generates predictions when a new match is
 * generated for a vehicle. The class to be instantiated can be set using the config variable
 * transitclock.core.predictionGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
public class PredictionGeneratorFactory {
    @Value("${transitclock.factory.prediction-generator:org.transitclock.core.predictiongenerator.PredictionGeneratorDefaultImpl}")
    private Class<?> neededClass;

    @Bean
    public PredictionGenerator predictionGenerator(StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                   TripDataHistoryCacheInterface tripDataHistoryCacheInterface,
                                                   DbConfig dbConfig,
                                                   DataDbLogger dataDbLogger,
                                                   TravelTimeDataFilter travelTimeDataFilter,
                                                   HoldingTimeCache holdingTimeCache,
                                                   VehicleDataCache vehicleDataCache,
                                                   StopPathPredictionCache stopPathPredictionCache,
                                                   TravelTimes travelTimes,
                                                   HoldingTimeGenerator holdingTimeGenerator,
                                                   VehicleStateManager vehicleStateManager,
                                                   RealTimeSchedAdhProcessor realTimeSchedAdhProcessor,
                                                   BiasAdjuster biasAdjuster,
                                                   ErrorCache kalmanErrorCache,
                                                   DwellTimeModelCacheInterface dwellTimeModelCacheInterface,
                                                   FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache,
                                                   ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache) {
        Class<?> desiredGenerator = neededClass;
        if (desiredGenerator == org.transitclock.core.predictiongenerator.scheduled.dwell.DwellTimePredictionGeneratorImpl.class) {
            return new org.transitclock.core.predictiongenerator.scheduled.dwell.DwellTimePredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster,
                kalmanErrorCache,
                dwellTimeModelCacheInterface);
        }

        if (desiredGenerator == org.transitclock.core.predictiongenerator.frequency.dwell.rls.DwellTimePredictionGeneratorImpl.class) {
            return new org.transitclock.core.predictiongenerator.frequency.dwell.rls.DwellTimePredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                vehicleDataCache,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster,
                frequencyBasedHistoricalAverageCache,
                kalmanErrorCache,
                dwellTimeModelCacheInterface
            );
        }

        if (desiredGenerator == LastVehiclePredictionGeneratorImpl.class)
            return new LastVehiclePredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                vehicleDataCache,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster
            );


        if (desiredGenerator == org.transitclock.core.predictiongenerator.frequency.traveltime.kalman.KalmanPredictionGeneratorImpl.class)
            return new org.transitclock.core.predictiongenerator.frequency.traveltime.kalman.KalmanPredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                vehicleDataCache,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster,
                frequencyBasedHistoricalAverageCache,
                kalmanErrorCache
            );

        if (desiredGenerator == org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanPredictionGeneratorImpl.class) {
            return new org.transitclock.core.predictiongenerator.scheduled.traveltime.kalman.KalmanPredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster,
                kalmanErrorCache
            );
        }

        if (desiredGenerator == org.transitclock.core.predictiongenerator.frequency.traveltime.average.HistoricalAveragePredictionGeneratorImpl.class)
            return new org.transitclock.core.predictiongenerator.frequency.traveltime.average.HistoricalAveragePredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                vehicleDataCache,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster,
                frequencyBasedHistoricalAverageCache
            );

        if (desiredGenerator == org.transitclock.core.predictiongenerator.scheduled.average.HistoricalAveragePredictionGeneratorImpl.class) {
            return new org.transitclock.core.predictiongenerator.scheduled.average.HistoricalAveragePredictionGeneratorImpl(
                stopArrivalDepartureCacheInterface,
                tripDataHistoryCacheInterface,
                dbConfig,
                dataDbLogger,
                travelTimeDataFilter,
                vehicleDataCache,
                holdingTimeCache,
                stopPathPredictionCache,
                travelTimes,
                holdingTimeGenerator,
                vehicleStateManager,
                realTimeSchedAdhProcessor,
                biasAdjuster,
                scheduleBasedHistoricalAverageCache
            );
        }

        // If the PredictionGenerator hasn't been created yet then do so now
        return new PredictionGeneratorDefaultImpl(stopArrivalDepartureCacheInterface, tripDataHistoryCacheInterface, dbConfig, dataDbLogger, travelTimeDataFilter, holdingTimeCache, stopPathPredictionCache, travelTimes, holdingTimeGenerator, vehicleStateManager, realTimeSchedAdhProcessor, biasAdjuster);
    }
}
