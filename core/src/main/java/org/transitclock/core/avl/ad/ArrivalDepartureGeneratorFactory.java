/* (C)2023 */
package org.transitclock.core.avl.ad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.ApplicationProperties;
import org.transitclock.core.TravelTimes;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.gtfs.DbConfig;

/**
 * For instantiating a ArrivalDepartureGenerator object that generates arrival/departure data when a
 * new match is generated for a vehicle. The class to be instantiated can be set using the config
 * variable transitclock.core.arrivalDepartureGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
public class ArrivalDepartureGeneratorFactory {
    @Value("${transitclock.factory.arrival-departure-generator:org.transitclock.core.avl.ad.ArrivalDepartureGeneratorDefaultImpl}")
    private Class<?> neededClass;

    @Bean
    public ArrivalDepartureGenerator arrivalDepartureGenerator(ApplicationProperties properties,
                                                               ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache,
                                                               FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache,
                                                               HoldingTimeCache holdingTimeCache,
                                                               VehicleStatusManager vehicleStatusManager,
                                                               HoldingTimeGenerator holdingTimeGenerator,
                                                               TravelTimes travelTimes,
                                                               TripDataHistoryCacheInterface tripDataHistoryCacheInterface,
                                                               StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                               DwellTimeModelCacheInterface dwellTimeModelCacheInterface,
                                                               DataDbLogger dataDbLogger,
                                                               DbConfig dbConfig) {
        // If the PredictionGenerator hasn't been created yet then do so now
        if (neededClass == ArrivalDepartureGeneratorDefaultImpl.class)
            return new ArrivalDepartureGeneratorDefaultImpl(scheduleBasedHistoricalAverageCache, frequencyBasedHistoricalAverageCache, holdingTimeCache, vehicleStatusManager, holdingTimeGenerator, travelTimes, tripDataHistoryCacheInterface, stopArrivalDepartureCacheInterface, dwellTimeModelCacheInterface, dataDbLogger, dbConfig, properties);

        throw new IllegalArgumentException("Requested ArrivalDepartureGenerator is not implemented");
    }
}
