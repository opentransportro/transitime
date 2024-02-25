/* (C)2023 */
package org.transitclock.config;

import java.net.URL;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class CacheManagerFactory {
    @Bean
    public CacheManager cacheManager(ResourceLoader resourceLoader) {
        URL xmlConfigUrl = CacheManagerFactory.class
                .getClassLoader()
                .getResource("ehcache.xml");
        if (xmlConfigUrl == null) {
            throw new RuntimeException("Could not find ehcache.xml");
        }
        XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);

        CacheManager cm = CacheManagerBuilder.newCacheManager(xmlConfig);
        cm.init();

        return cm;
    }
}
