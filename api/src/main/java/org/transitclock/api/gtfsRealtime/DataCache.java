/* (C)2023 */
package org.transitclock.api.gtfsRealtime;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.util.HashMap;
import java.util.Map;
import org.transitclock.utils.Time;

/**
 * For caching GTFS-realtime messages. Useful because the messages are huge and take a lot of
 * resources so if get multiple requests not too far apart then it makes sense to return a cached
 * version.
 *
 * @author SkiBu Smith
 */
public class DataCache {

    private Map<String, CacheEntry> cacheMap = new HashMap<>();

    private static class CacheEntry {
        private long timeCreated;
        private FeedMessage cachedFeedMessage;
    }

    public FeedMessage get(String agencyId, int maxCacheSeconds) {
        CacheEntry cacheEntry = cacheMap.get(agencyId);
        if (cacheEntry == null) return null;
        if (cacheEntry.timeCreated < System.currentTimeMillis() - maxCacheSeconds * Time.MS_PER_SEC) {
            cacheMap.remove(agencyId);
            return null;
        }
        return cacheEntry.cachedFeedMessage;
    }

    public void put(String agencyId, FeedMessage feedMessage) {
        CacheEntry cacheEntry = new CacheEntry();
        cacheEntry.timeCreated = System.currentTimeMillis();
        cacheEntry.cachedFeedMessage = feedMessage;
        cacheMap.put(agencyId, cacheEntry);
    }
}
