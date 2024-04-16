/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.ApplicationProperties;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.gtfs.GtfsFilter;
import org.transitclock.properties.GtfsProperties;

import lombok.RequiredArgsConstructor;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold arrival and departures for a
 *     trip.
 */
@Configuration
@RequiredArgsConstructor
public class TripDataHistoryCacheFactory {
    @Value("${transitclock.core.cache.tripDataHistoryCache:org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache}")
    private Class<?> className;

    @Bean
    public TripDataHistoryCacheInterface tripDataHistoryCacheInterface(CacheManager cacheManager,
                                                                       ApplicationProperties properties,
                                                                       DbConfig dbConfig) {
        GtfsProperties gtfsProperties = properties.getGtfs();
        var gtfsFilter = new GtfsFilter(gtfsProperties.getRouteIdFilterRegEx(), gtfsProperties.getTripIdFilterRegEx());
        if (className == org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache.class) {
            return new org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache(cacheManager, gtfsFilter, dbConfig);
        } else {
            return new org.transitclock.core.dataCache.ehcache.scheduled.TripDataHistoryCache(cacheManager, gtfsFilter, dbConfig);
        }
    }
}
