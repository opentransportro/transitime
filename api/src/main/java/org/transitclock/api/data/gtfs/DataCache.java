/* (C)2023 */
package org.transitclock.api.data.gtfs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.ApiConfig;
import org.transitclock.utils.Time;

import java.util.concurrent.TimeUnit;

/**
 * For caching GTFS-realtime messages. Useful because the messages are huge and take a lot of
 * resources so if get multiple requests not too far apart then it makes sense to return a cached
 * version.
 *
 * @author SkiBu Smith
 */
public class DataCache {
    private final Cache<String, FeedMessage> cacheMap =  CacheBuilder.newBuilder()
            .expireAfterWrite(ApiConfig.gtfsRtCacheSeconds.getValue(), TimeUnit.SECONDS)
            .build();


    public FeedMessage get(String agencyId) {
        return cacheMap.getIfPresent(agencyId);
    }

    public void put(String agencyId, FeedMessage feedMessage) {
        cacheMap.put(agencyId, feedMessage);
    }
}
