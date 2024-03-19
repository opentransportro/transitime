/* (C)2023 */
package org.transitclock.core.prediction.datafilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import org.transitclock.ApplicationProperties;
import org.transitclock.config.ClassConfigValue;

@Configuration
public class DwellTimeFilterFactory {
    // The name of the class to instantiate
    @Value("${transitclock.core.predictiongenerator.datafilter.dwelltime:org.transitclock.core.prediction.datafilter.DwellTimeDataFilterImpl}")
    private Class<?> className;

    @Bean
    @Lazy
    public DwellTimeDataFilter dwellTimeDataFilter(ApplicationProperties properties) {
        return new DwellTimeDataFilterImpl(properties.getPrediction());
    }
}
