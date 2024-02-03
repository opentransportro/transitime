/* (C)2023 */
package org.transitclock.core.dataCache;

import org.ehcache.CacheManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.gtfs.DbConfig;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold arrival and departures for a
 *     trip.
 */
@Configuration
public class TripDataHistoryCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.tripDataHistoryCache",
            org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache.class,
            "Specifies the class used to cache the arrival and departures for a trip.");

    private final DefaultListableBeanFactory factory;
    private final CacheManager cacheManager;
    private final DbConfig dbConfig;

    public TripDataHistoryCacheFactory(DefaultListableBeanFactory factory, CacheManager cacheManager, DbConfig dbConfig) {
        this.factory = factory;
        this.cacheManager = cacheManager;
        this.dbConfig = dbConfig;
    }

    @Bean
    public TripDataHistoryCacheInterface tripDataHistoryCacheInterface() {
        return (TripDataHistoryCacheInterface) factory.createBean(className.getValue());
    }
}
