/* (C)2023 */
package org.transitclock.core.dataCache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.domain.structs.PredictionForStopPath;

import java.util.ArrayList;
import java.util.List;

public class StopPathPredictionCache {
    private static final String cacheName = "StopPathPredictionCache";
    private static final StopPathPredictionCache singleton = new StopPathPredictionCache();
    private static final Logger logger = LoggerFactory.getLogger(StopPathPredictionCache.class);

    private final Cache<StopPathCacheKey, StopPredictions> cache;

    public static StopPathPredictionCache getInstance() {
        return singleton;
    }

    private StopPathPredictionCache() {
        CacheManager cm = CacheManagerFactory.getInstance();
        cache = cm.getCache(cacheName, StopPathCacheKey.class, StopPredictions.class);
    }

    public void logCache(Logger logger) {
        logger.debug("Cache content log. Not implemented.");
    }

    @SuppressWarnings("unchecked")
    public synchronized List<PredictionForStopPath> getPredictions(StopPathCacheKey key) {

        StopPredictions result = cache.get(key);
        logCache(logger);
        if (result == null) return null;
        else return result.getPredictions();
    }

    public void putPrediction(PredictionForStopPath prediction) {
        StopPathCacheKey key = new StopPathCacheKey(prediction.getTripId(), prediction.getStopPathIndex());
        putPrediction(key, prediction);
    }

    @SuppressWarnings("unchecked")
    public synchronized void putPrediction(StopPathCacheKey key, PredictionForStopPath prediction) {

        List<PredictionForStopPath> list;
        StopPredictions element = cache.get(key);

        if (element != null && element.getPredictions() != null) {
            list = element.getPredictions();
            cache.remove(key);
        } else {
            list = new ArrayList<>();
        }
        list.add(prediction);

        element.setPredictions(list);

        cache.put(key, element);
    }
}
