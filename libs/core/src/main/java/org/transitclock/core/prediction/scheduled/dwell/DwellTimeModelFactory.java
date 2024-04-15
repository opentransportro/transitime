/* (C)2023 */
package org.transitclock.core.prediction.scheduled.dwell;

import org.transitclock.ApplicationProperties;
import org.transitclock.config.ClassConfigValue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author scrudden Returns the model that is to be used to estimate dwell time for a stop.
 */
@Configuration
public class DwellTimeModelFactory {
    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.dwelltime.model",
            org.transitclock.core.prediction.scheduled.dwell.DwellAverage.class,
            "Specifies the name of the class used to predict dwell.");

    @Bean
    public DwellModel dwellModel(ApplicationProperties properties) {
        if (className.getValue() == DwellAverage.class) {
            return new DwellAverage(properties.getPrediction().getDwell().getAverage());
        }

        return new DwellRLS(properties.getPrediction().getRls());
    }
}
