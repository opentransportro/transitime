/* (C)2023 */
package org.transitclock.core.dataCache.ehcache;

import java.net.URL;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

public class CacheManagerFactory {

    public static CacheManager singleton = null;

    public static synchronized CacheManager getInstance() {
        if (singleton == null) {
            URL xmlConfigUrl = CacheManagerFactory.class
                    .getClassLoader()
                    .getResource("ehcache.xml");
            if (xmlConfigUrl == null) {
                throw new RuntimeException("Could not find ehcache.xml");
            }
            XmlConfiguration xmlConfig = new XmlConfiguration(xmlConfigUrl);

            singleton = CacheManagerBuilder.newCacheManager(xmlConfig);
            singleton.init();
        }

        return singleton;
    }
}
