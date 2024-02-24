/* (C)2023 */
package org.transitclock.core.predictiongenerator.datafilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

@Configuration
public class TravelTimeFilterFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.datafilter.traveltime",
            org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilterImpl.class,
            "Specifies the name of the class used to filter travel times.");

    @Bean
    @Lazy
    public TravelTimeDataFilter travelTimeDataFilter() {
        return new TravelTimeDataFilterImpl();
    }
}
