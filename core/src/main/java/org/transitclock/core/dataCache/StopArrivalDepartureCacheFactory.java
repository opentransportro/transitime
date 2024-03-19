/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache;

import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sean Óg Crudden
 */
@Configuration
public class StopArrivalDepartureCacheFactory {
    @Value("${transitclock.core.cache.stopArrivalDepartureCache:org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache}")
    private Class<?> className;

    @Bean
    public StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface(CacheManager cacheManager) {
        if (className == StopArrivalDepartureCache.class) {
            return new StopArrivalDepartureCache(cacheManager);
        }

        throw new IllegalArgumentException("Unknown StopArrivalDepartureCacheInterface: " + className);
    }
}
