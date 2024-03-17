package org.transitclock.api.data.gtfs;

import com.google.transit.realtime.GtfsRealtime;
import org.springframework.stereotype.Component;
import org.transitclock.ApplicationProperties;
import org.transitclock.api.utils.AgencyTimezoneCache;
import org.transitclock.service.contract.PredictionsInterface;
import org.transitclock.service.contract.VehiclesInterface;

@Component
public class FeedCacheManager {
    private final DataCache vehicleFeedDataCache;
    private final DataCache tripFeedDataCache;

    public FeedCacheManager(ApplicationProperties properties) {
        this.vehicleFeedDataCache = new DataCache(properties.getApi().getGtfsRtCacheSeconds());
        this.tripFeedDataCache = new DataCache(properties.getApi().getGtfsRtCacheSeconds());
    }

    /**
     * For caching Vehicle Positions feed messages.
     *
     * @param agencyId
     * @return
     */
    public GtfsRealtime.FeedMessage getPossiblyCachedMessage(String agencyId,
                                                                    VehiclesInterface vehiclesInterface,
                                                                    AgencyTimezoneCache agencyTimezoneCache) {
        GtfsRealtime.FeedMessage feedMessage = vehicleFeedDataCache.get(agencyId);
        if (feedMessage != null) return feedMessage;

        synchronized (vehicleFeedDataCache) {

            // Cache may have been filled while waiting.
            feedMessage = vehicleFeedDataCache.get(agencyId);
            if (feedMessage != null) return feedMessage;

            GtfsRtVehicleFeed feed = new GtfsRtVehicleFeed(agencyId, vehiclesInterface, agencyTimezoneCache);
            feedMessage = feed.createMessage();
            vehicleFeedDataCache.put(agencyId, feedMessage);
        }

        return feedMessage;
    }

    /**
     * For caching Vehicle Positions feed messages.
     *
     * @param agencyId
     * @return
     */
    public GtfsRealtime.FeedMessage getPossiblyCachedMessage(ApplicationProperties properties,
                                                             PredictionsInterface predictionsInterface,
                                                             VehiclesInterface vehiclesInterface,
                                                             AgencyTimezoneCache agencyTimezoneCache) {
        GtfsRealtime.FeedMessage feedMessage = tripFeedDataCache.get(properties.getCore().getAgencyId());
        if (feedMessage != null) return feedMessage;

        synchronized (tripFeedDataCache) {

            // Cache may have been filled while waiting.
            feedMessage = tripFeedDataCache.get(properties.getCore().getAgencyId());
            if (feedMessage != null) return feedMessage;

            GtfsRtTripFeed feed = new GtfsRtTripFeed(properties, predictionsInterface, vehiclesInterface, agencyTimezoneCache);
            feedMessage = feed.createMessage();
            tripFeedDataCache.put(properties.getCore().getAgencyId(), feedMessage);
        }

        return feedMessage;
    }
}
