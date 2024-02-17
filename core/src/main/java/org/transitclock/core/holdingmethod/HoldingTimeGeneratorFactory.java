/* (C)2023 */
package org.transitclock.core.holdingmethod;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

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

    private static HoldingTimeGenerator singleton = null;

    @Bean
    public HoldingTimeGenerator holdingTimeGenerator() {
        if (singleton == null && className.getValue() != null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), HoldingTimeGenerator.class);
        }

        return singleton;
    }
}
