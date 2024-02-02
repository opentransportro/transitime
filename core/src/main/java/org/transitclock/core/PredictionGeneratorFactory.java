/* (C)2023 */
package org.transitclock.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a PredictionGenerator object that generates predictions when a new match is
 * generated for a vehicle. The class to be instantiated can be set using the config variable
 * transitclock.core.predictionGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
public class PredictionGeneratorFactory {

    // The name of the class to instantiate
    private static final StringConfigValue className = new StringConfigValue(
            "transitclock.core.predictionGeneratorClass",
            "org.transitclock.core.PredictionGeneratorDefaultImpl",
            "Specifies the name of the class used for generating prediction data.");

    private static PredictionGenerator singleton = null;

    @Bean
    public synchronized PredictionGenerator predictionGenerator() {
        // If the PredictionGenerator hasn't been created yet then do so now
        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), PredictionGenerator.class);
        }

        return singleton;
    }
}
