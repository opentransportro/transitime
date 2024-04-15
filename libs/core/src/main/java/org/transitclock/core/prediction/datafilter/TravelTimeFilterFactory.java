/* (C)2023 */
package org.transitclock.core.prediction.datafilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import org.transitclock.ApplicationProperties;
import org.transitclock.config.ClassConfigValue;

@Configuration
public class TravelTimeFilterFactory {

    @Value("${transitclock.core.predictiongenerator.datafilter.traveltime:org.transitclock.core.prediction.datafilter.TravelTimeDataFilterImpl}")
    private Class<?> className;

    @Bean
    @Lazy
    public TravelTimeDataFilter travelTimeDataFilter(ApplicationProperties properties) {
        return new TravelTimeDataFilterImpl(properties.getPrediction());
    }
}
