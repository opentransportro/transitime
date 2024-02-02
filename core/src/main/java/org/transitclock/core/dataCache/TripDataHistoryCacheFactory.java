/* (C)2023 */
package org.transitclock.core.dataCache;

import org.ehcache.CacheManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

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

    public static TripDataHistoryCacheInterface singleton = null;

    @Bean
    @DependsOn({"cacheManager", "dbConfig"})
    public TripDataHistoryCacheInterface tripDataHistoryCacheInterface(DefaultListableBeanFactory factory) {
        Object bean = factory.createBean(className.getValue());
        return (TripDataHistoryCacheInterface) bean;
//        if (singleton == null) {
//            singleton = ClassInstantiator.instantiate(className.getValue(), TripDataHistoryCacheInterface.class);
//        }
//
//        return singleton;
    }
}
