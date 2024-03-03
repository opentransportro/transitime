/* (C)2023 */
package org.transitclock.core.holdingmethod;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.holdingTimeGeneratorClass",
            DummyHoldingTimeGeneratorImpl.class,
            "Specifies the name of the class used for generating " + "holding times.");

    @Bean
    public HoldingTimeGenerator holdingTimeGenerator(PredictionDataCache predictionDataCache,
                                                     StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface,
                                                     DataDbLogger dataDbLogger,
                                                     DbConfig dbConfig,
                                                     VehicleDataCache vehicleDataCache,
                                                     HoldingTimeCache holdingTimeCache,
                                                     VehicleStatusManager vehicleStatusManager) {
        if (className.getValue() == HoldingTimeGeneratorDefaultImpl.class) {
            return new HoldingTimeGeneratorDefaultImpl(predictionDataCache, stopArrivalDepartureCacheInterface, dataDbLogger, dbConfig, vehicleDataCache, holdingTimeCache, vehicleStatusManager);
        } else if(className.getValue() == SimpleHoldingTimeGeneratorImpl.class) {
            return new SimpleHoldingTimeGeneratorImpl(predictionDataCache, stopArrivalDepartureCacheInterface, dataDbLogger, dbConfig);
        }

        return new DummyHoldingTimeGeneratorImpl();
    }
}
