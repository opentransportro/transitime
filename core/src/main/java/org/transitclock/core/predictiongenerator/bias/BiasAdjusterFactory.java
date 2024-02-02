/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

@Configuration
public class BiasAdjusterFactory {
    private static BiasAdjuster singleton = null;

    // The name of the class to instantiate
    private static final StringConfigValue className = new StringConfigValue(
            "transitclock.core.predictiongenerator.biasabjuster",
            null,
            "Specifies the name of the class used to adjust the bias of a predction.");

    @Bean
    public BiasAdjuster biasAdjuster() {
        if (className.getValue() != null && !className.getValue().isEmpty()) {
            if (singleton == null)
                singleton = ClassInstantiator.instantiate(className.getValue(), BiasAdjuster.class);
            return singleton;
        }
        return null;
    }
}
