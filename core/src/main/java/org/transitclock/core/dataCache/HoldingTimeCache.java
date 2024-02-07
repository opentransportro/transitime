/* (C)2023 */
package org.transitclock.core.dataCache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.domain.structs.HoldingTime;

import java.util.List;

/**
 * @author Sean Óg Crudden
 */
public class HoldingTimeCache {
    private static final String cacheName = "HoldingTimeCache";
    private static final HoldingTimeCache singleton = new HoldingTimeCache();
    private final Cache<HoldingTimeCacheKey, HoldingTime> cache;

    /**
     * Gets the singleton instance of this class.
     *
     * @return
     */
    public static HoldingTimeCache getInstance() {
        return singleton;
    }

    private HoldingTimeCache() {
        CacheManager cm = CacheManagerFactory.getInstance();
        cache = cm.getCache(cacheName, HoldingTimeCacheKey.class, HoldingTime.class);
    }

    public void logCache(Logger logger) {
        logger.debug("Cache content log. Not implemented");
    }

    public void putHoldingTime(HoldingTime holdingTime) {
        HoldingTimeCacheKey key = new HoldingTimeCacheKey(holdingTime);

        cache.put(key, holdingTime);
    }

    public HoldingTime getHoldingTime(HoldingTimeCacheKey key) {
        return cache.get(key);
    }

    public List<HoldingTimeCacheKey> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }
}
