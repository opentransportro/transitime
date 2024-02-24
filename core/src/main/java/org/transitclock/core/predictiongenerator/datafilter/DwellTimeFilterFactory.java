/* (C)2023 */
package org.transitclock.core.predictiongenerator.datafilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

@Configuration
public class DwellTimeFilterFactory {
    // The name of the class to instantiate
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.predictiongenerator.datafilter.dwelltime",
            org.transitclock.core.predictiongenerator.datafilter.DwellTimeDataFilterImpl.class,
            "Specifies the name of the class used to filter dwell times.");

    @Bean
    @Lazy
    public DwellTimeDataFilter dwellTimeDataFilter() {
        return new DwellTimeDataFilterImpl();
    }
}
