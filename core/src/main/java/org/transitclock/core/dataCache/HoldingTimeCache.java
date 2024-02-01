/* (C)2023 */
package org.transitclock.core.dataCache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.transitclock.SingletonContainer;
import org.transitclock.annotations.Component;
import org.transitclock.domain.structs.HoldingTime;

import java.util.List;

/**
 * @author Sean Óg Crudden
 */
@Component
public class HoldingTimeCache {
    private static final String cacheName = "HoldingTimeCache";
    private Cache<HoldingTimeCacheKey, HoldingTime> cache = null;


    public HoldingTimeCache() {
        CacheManager cm = SingletonContainer.getInstance(CacheManager.class);
        cache = cm.getCache(cacheName, HoldingTimeCacheKey.class, HoldingTime.class);
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
