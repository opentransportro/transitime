/* (C)2023 */
package org.transitclock.core;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a ArrivalDepartureGenerator object that generates arrival/departure data when a
 * new match is generated for a vehicle. The class to be instantiated can be set using the config
 * variable transitclock.core.arrivalDepartureGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
@RequiredArgsConstructor
public class ArrivalDepartureGeneratorFactory {

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.arrivalDepartureGeneratorClass",
            org.transitclock.core.ArrivalDepartureGeneratorDefaultImpl.class,
            "Specifies the name of the class used for generating arrival/departure data.");


    private final DefaultListableBeanFactory factory;
    private final ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
    private final FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache;
    private final HoldingTimeCache holdingTimeCache;
    private final VehicleStateManager vehicleStateManager;
    private final TravelTimes travelTimes;
    private final DataDbLogger dataDbLogger;
    private final DbConfig dbConfig;
    private final DwellTimeModelCacheInterface dwellTimeModelCacheInterface;
    private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final HoldingTimeGenerator holdingTimeGenerator;

    @Bean
    public ArrivalDepartureGenerator arrivalDepartureGenerator() {
        return (ArrivalDepartureGenerator) factory.createBean(className.getValue());
    }

}
