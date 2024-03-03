/* (C)2023 */
package org.transitclock.core.prediction.datafilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.transitclock.config.ClassConfigValue;

@Configuration
public class TravelTimeFilterFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.datafilter.traveltime",
            org.transitclock.core.prediction.datafilter.TravelTimeDataFilterImpl.class,
            "Specifies the name of the class used to filter travel times.");

    @Bean
    @Lazy
    public TravelTimeDataFilter travelTimeDataFilter() {
        return new TravelTimeDataFilterImpl();
    }
}
