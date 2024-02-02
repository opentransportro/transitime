/* (C)2023 */
package org.transitclock.core.dataCache.ehcache;

import java.net.URL;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CacheManagerFactory {

    @Bean
    public CacheManager cacheManager() {
        URL xmlConfigUrl = CacheManagerFactory.class.getClassLoader().getResource("ehcache.xml");
        XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);

        CacheManager cm = CacheManagerBuilder.newCacheManager(xmlConfig);
        cm.init();
        return cm;
    }
}
