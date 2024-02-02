/* (C)2023 */
package org.transitclock.core.dataCache;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.transitclock.config.ClassConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold Kalman error values.
 */
@Configuration
public class ErrorCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.errorCacheClass",
            org.transitclock.core.dataCache.ehcache.KalmanErrorCache.class,
            "Specifies the class used to cache the Kalamn error values.");

    private static ErrorCache singleton = null;

    @Bean
    public ErrorCache errorCache(DefaultListableBeanFactory factory) {
        Object bean = factory.createBean(className.getValue());
        return (ErrorCache) bean;
//        if (singleton == null) {
//            singleton = ClassInstantiator.instantiate(className.getValue(), ErrorCache.class);
//        }
//
//        return singleton;
    }
}
