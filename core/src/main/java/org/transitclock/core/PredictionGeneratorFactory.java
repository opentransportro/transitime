/* (C)2023 */
package org.transitclock.core;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.holdingmethod.HoldingTimeGenerator;
import org.transitclock.core.predictiongenerator.bias.BiasAdjuster;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
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
@RequiredArgsConstructor
public class PredictionGeneratorFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictionGeneratorClass",
            org.transitclock.core.PredictionGeneratorDefaultImpl.class,
            "Specifies the name of the class used for generating prediction data.");

    private final DefaultListableBeanFactory factory;
    private final DbConfig dbConfig;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
    private final HoldingTimeCache holdingTimeCache;
    private final ErrorCache kalmanErrorCache;
    private final StopPathPredictionCache stopPathPredictionCache;
    private final VehicleStateManager vehicleStateManager;
    private final TravelTimes travelTimes;
    private final DataDbLogger dataDbLogger;
    private final HoldingTimeGenerator holdingTimeGenerator;
    private final BiasAdjuster biasAdjuster;
    private final TravelTimeDataFilter travelTimeDataFilter;

    @Bean
    public PredictionGenerator predictionGenerator() {
        return (PredictionGenerator) factory.createBean(className.getValue());
    }
}
