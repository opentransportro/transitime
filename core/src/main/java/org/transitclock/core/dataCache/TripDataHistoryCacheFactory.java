/* (C)2023 */
package org.transitclock.core.dataCache;

import lombok.RequiredArgsConstructor;
import org.ehcache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.transitclock.ApplicationProperties;
import org.transitclock.ApplicationProperties.Gtfs;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsFilter;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold arrival and departures for a
 *     trip.
 */
@Configuration
@RequiredArgsConstructor
public class TripDataHistoryCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.tripDataHistoryCache",
            org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache.class,
            "Specifies the class used to cache the arrival and departures for a trip.");

    @Bean
    public TripDataHistoryCacheInterface tripDataHistoryCacheInterface(CacheManager cacheManager, ApplicationProperties properties,
                                                                       DbConfig dbConfig) {
        var gtfsFilter = new GtfsFilter(properties.getGtfs().getRouteIdFilterRegEx(), properties.getGtfs().getTripIdFilterRegEx());
        if (className.getValue() == org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache.class) {
            return new org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache(cacheManager, gtfsFilter, dbConfig);
        } else {
            return new org.transitclock.core.dataCache.ehcache.scheduled.TripDataHistoryCache(cacheManager, gtfsFilter, dbConfig);
        }
    }
}
