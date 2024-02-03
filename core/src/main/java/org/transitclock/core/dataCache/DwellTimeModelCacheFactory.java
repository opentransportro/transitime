/* (C)2023 */
package org.transitclock.core.dataCache;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold dwell time model class instances
 *     for each stop.
 */
@Configuration
public class DwellTimeModelCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.dwellTimeModelCache",
            org.transitclock.core.dataCache.ehcache.scheduled.DwellTimeModelCache.class,
            "Specifies the class used to cache RLS data for a stop.");

    private final DefaultListableBeanFactory factory;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;

    public DwellTimeModelCacheFactory(DefaultListableBeanFactory factory, StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface) {
        this.factory = factory;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
    }

    @Bean
    public DwellTimeModelCacheInterface dwellTimeModelCacheInterface() {
        return (DwellTimeModelCacheInterface) factory.createBean(className.getValue());
    }
}
