/* (C)2023 */
package org.transitclock.core.dataCache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden
 */
@Configuration
public class StopArrivalDepartureCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.stopArrivalDepartureCache",
            org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache.class,
            "Specifies the class used to cache the arrival and departures for a stop.");

    private static StopArrivalDepartureCacheInterface singleton = null;

    @Bean
    public StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface() {

        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), StopArrivalDepartureCacheInterface.class);
        }

        return singleton;
    }
}
