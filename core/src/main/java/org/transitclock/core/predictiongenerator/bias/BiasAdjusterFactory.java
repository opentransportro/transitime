/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

import javax.sound.sampled.Line;

@Configuration
public class BiasAdjusterFactory {
    private static BiasAdjuster singleton = null;

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.biasabjuster",
            DummyBiasAdjuster.class,
            "Specifies the name of the class used to adjust the bias of a predction.");

    @Bean
    public BiasAdjuster biasAdjuster() {
        if(className.getValue() == ExponentialBiasAdjuster.class) {
            return new ExponentialBiasAdjuster();
        } else if (className.getValue() == LinearBiasAdjuster.class) {
            return new LinearBiasAdjuster();
        }

        return new DummyBiasAdjuster();
    }
}
