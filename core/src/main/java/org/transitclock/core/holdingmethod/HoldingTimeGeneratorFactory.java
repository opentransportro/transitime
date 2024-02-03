/* (C)2023 */
package org.transitclock.core.holdingmethod;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 */
@RequiredArgsConstructor
@Configuration
public class HoldingTimeGeneratorFactory {
    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.holdingTimeGeneratorClass",
            HoldingTimeGenerator.DummyHoldingTimeGenerator.class,
            "Specifies the name of the class used for generating " + "holding times.");

    private final DefaultListableBeanFactory factory;
    private final VehicleStateManager vehicleStateManager;
    private final HoldingTimeCache holdingTimeCache;
    private final VehicleDataCache vehicleDataCache;
    private final PredictionDataCache predictionDataCache;
    private final DataDbLogger dataDbLogger;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;

    @Bean
    public HoldingTimeGenerator holdingTimeGenerator() {
        return (HoldingTimeGenerator) factory.createBean(className.getValue());
    }
}
