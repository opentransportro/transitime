/* (C)2023 */
package org.transitclock.core.holdingmethod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import org.transitclock.ApplicationProperties;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.gtfs.DbConfig;

/**
 * @author Sean Óg Crudden
 */
@Configuration
public class HoldingTimeGeneratorFactory {
    // The name of the class to instantiate
    @Value("${transitclock.core.holdingTimeGeneratorClass:org.transitclock.core.holdingmethod.DummyHoldingTimeGeneratorImpl}")
    private Class<?> className;

    @Bean
    @Lazy
    public HoldingTimeGenerator holdingTimeGenerator(ApplicationProperties properties,
                                                     PredictionDataCache predictionDataCache,
                                                     StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                     DataDbLogger dataDbLogger,
                                                     DbConfig dbConfig,
                                                     VehicleDataCache vehicleDataCache,
                                                     HoldingTimeCache holdingTimeCache,
                                                     VehicleStatusManager vehicleStatusManager) {
        if (className == HoldingTimeGeneratorDefaultImpl.class) {
            return new HoldingTimeGeneratorDefaultImpl(properties.getHolding(),predictionDataCache, stopArrivalDepartureCacheInterface, dataDbLogger, dbConfig, vehicleDataCache, holdingTimeCache, vehicleStatusManager);
        } else if(className == SimpleHoldingTimeGeneratorImpl.class) {
            return new SimpleHoldingTimeGeneratorImpl(properties.getHolding(), predictionDataCache, stopArrivalDepartureCacheInterface, dataDbLogger, dbConfig);
        }

        return new DummyHoldingTimeGeneratorImpl();
    }
}
