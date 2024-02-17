/* (C)2023 */
package org.transitclock.core.dataCache.ehcache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.transitclock.core.Indices;
import org.transitclock.core.dataCache.ErrorCache;
import org.transitclock.core.dataCache.KalmanError;
import org.transitclock.core.dataCache.KalmanErrorCacheKey;

import java.util.List;

/**
 * @author Sean Óg Crudden
 */
public class KalmanErrorCache implements ErrorCache {
    private static final String cacheName = "KalmanErrorCache";
    private final Cache<KalmanErrorCacheKey, KalmanError> cache;

    public KalmanErrorCache(CacheManager cm) {
        cache = cm.getCache(cacheName, KalmanErrorCacheKey.class, KalmanError.class);
    }

    /* (non-Javadoc)
     * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.Indices)
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized KalmanError getErrorValue(Indices indices) {
        KalmanErrorCacheKey key = new KalmanErrorCacheKey(indices);
        return cache.get(key);
    }

    /* (non-Javadoc)
     * @see org.transitime.core.dataCache.ErrorCache#getErrorValue(org.transitime.core.dataCache.KalmanErrorCacheKey)
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized KalmanError getErrorValue(KalmanErrorCacheKey key) {
        KalmanError result = cache.get(key);
        return result;
    }

    /* (non-Javadoc)
     * @see org.transitime.core.dataCache.ErrorCache#putErrorValue(org.transitime.core.Indices, java.lang.Double)
     */
    @Override
    public synchronized void putErrorValue(Indices indices, Double value) {
        KalmanErrorCacheKey key = new KalmanErrorCacheKey(indices);
        putErrorValue(key, value);
    }

    @Override
    public void putErrorValue(KalmanErrorCacheKey key, Double value) {

        KalmanError error = cache.get(key);

        if (error == null) {
            error = new KalmanError(value);
        } else {
            error.setError(value);
        }

        cache.put(key, error);
    }

    @Override
    public List<KalmanErrorCacheKey> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }
}
