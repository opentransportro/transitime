/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;

import org.ehcache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sean Óg Crudden
 */
@Configuration
public class StopArrivalDepartureCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.stopArrivalDepartureCache",
            org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache.class,
            "Specifies the class used to cache the arrival and departures for a stop.");

    @Bean
    public StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface(CacheManager cacheManager) {
        return new StopArrivalDepartureCache(cacheManager);
    }
}
