package org.transitclock.api.data.gtfs;

import com.google.transit.realtime.GtfsRealtime;
import org.springframework.stereotype.Component;
import org.transitclock.ApplicationProperties;
import org.transitclock.api.utils.AgencyTimezoneCache;
import org.transitclock.service.contract.PredictionsService;
import org.transitclock.service.contract.VehiclesService;

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
                                                                    VehiclesService vehiclesService,
                                                                    AgencyTimezoneCache agencyTimezoneCache) {
        GtfsRealtime.FeedMessage feedMessage = vehicleFeedDataCache.get(agencyId);
        if (feedMessage != null) return feedMessage;

        synchronized (vehicleFeedDataCache) {

            // Cache may have been filled while waiting.
            feedMessage = vehicleFeedDataCache.get(agencyId);
            if (feedMessage != null) return feedMessage;

            GtfsRtVehicleFeed feed = new GtfsRtVehicleFeed(agencyId, vehiclesService, agencyTimezoneCache);
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
                                                             PredictionsService predictionsService,
                                                             VehiclesService vehiclesService,
                                                             AgencyTimezoneCache agencyTimezoneCache) {
        GtfsRealtime.FeedMessage feedMessage = tripFeedDataCache.get(properties.getCore().getAgencyId());
        if (feedMessage != null) return feedMessage;

        synchronized (tripFeedDataCache) {

            // Cache may have been filled while waiting.
            feedMessage = tripFeedDataCache.get(properties.getCore().getAgencyId());
            if (feedMessage != null) return feedMessage;

            GtfsRtTripFeed feed = new GtfsRtTripFeed(properties, predictionsService, vehiclesService, agencyTimezoneCache);
            feedMessage = feed.createMessage();
            tripFeedDataCache.put(properties.getCore().getAgencyId(), feedMessage);
        }

        return feedMessage;
    }
}
