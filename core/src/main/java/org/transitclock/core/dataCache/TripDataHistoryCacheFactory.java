/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.config.ClassConfigValue;
import org.transitclock.utils.ClassInstantiator;

/**
 * @author Sean Óg Crudden Factory that will provide cache to hold arrival and departures for a
 *     trip.
 */
public class TripDataHistoryCacheFactory {
    private static final ClassConfigValue className = new ClassConfigValue(
            "transitclock.core.cache.tripDataHistoryCache",
            org.transitclock.core.dataCache.ehcache.frequency.TripDataHistoryCache.class,
            "Specifies the class used to cache the arrival and departures for a trip.");

    public static TripDataHistoryCacheInterface singleton = null;

    public static TripDataHistoryCacheInterface getInstance() {

        if (singleton == null) {
            singleton = ClassInstantiator.instantiate(className.getValue(), TripDataHistoryCacheInterface.class);
        }

        return singleton;
    }
}
