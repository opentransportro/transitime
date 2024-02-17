/* (C)2023 */
package org.transitclock.core.predictiongenerator.scheduled.dwell;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author scrudden Returns the model that is to be used to estimate dwell time for a stop.
 */
@Configuration
public class DwellTimeModelFactory {
    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.dwelltime.model",
            org.transitclock.core.predictiongenerator.scheduled.dwell.DwellAverage.class,
            "Specifies the name of the class used to predict dwell.");

    @Bean
    public static DwellModel dwellModel() {
        return ClassInstantiator.instantiate(className.getValue(), DwellModel.class);
    }
}
