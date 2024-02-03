/* (C)2023 */
package org.transitclock.core.dataCache;

import org.ehcache.CacheManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.ClassInstantiator;

import java.lang.reflect.Constructor;

/**
 * @author Sean Óg Crudden
 */
@Configuration
public class StopArrivalDepartureCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.stopArrivalDepartureCache",
            org.transitclock.core.dataCache.ehcache.StopArrivalDepartureCache.class,
            "Specifies the class used to cache the arrival and departures for a stop.");

    private final DefaultListableBeanFactory factory;

    public StopArrivalDepartureCacheFactory(DefaultListableBeanFactory factory) {
        this.factory = factory;
    }

    @Bean
    public StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface() {
        return (StopArrivalDepartureCacheInterface) factory.createBean(className.getValue());
    }
}
