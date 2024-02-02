/* (C)2023 */
package org.transitclock.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * For instantiating a HeadwayGenerator object that generates headway info when a new match is
 * generated for a vehicle. The class to be instantiated can be set using the config variable
 * transitclock.core.headwayGeneratorClass
 *
 * @author SkiBu Smith
 */
@Configuration
public class HeadwayGeneratorFactory {

    // The name of the class to instantiate
    private static final StringConfigValue className = new StringConfigValue(
            "transitclock.core.headwayGeneratorClass",
            "org.transitclock.core.HeadwayGeneratorDefaultImpl",
            "Specifies the name of the class used for generating " + "headway data.");

    private static HeadwayGenerator singleton = null;

    @Bean
    public HeadwayGenerator headwayGenerator() {
        // If the PredictionGenerator hasn't been created yet then do so now
        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), HeadwayGenerator.class);
        }

        return singleton;
    }
}
