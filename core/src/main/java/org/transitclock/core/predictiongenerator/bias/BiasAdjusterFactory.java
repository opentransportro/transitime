/* (C)2023 */
package org.transitclock.core.predictiongenerator.bias;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

@RequiredArgsConstructor
@Configuration
public class BiasAdjusterFactory {
    private static BiasAdjuster singleton = null;

    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.biasabjuster",
            BiasAdjuster.DummyBiasAdjuster.class,
            "Specifies the name of the class used to adjust the bias of a prediction.");

    private final DefaultListableBeanFactory factory;

    @Bean
    public BiasAdjuster biasAdjuster() {
        return (BiasAdjuster) factory.createBean(className.getValue());
    }
}
