/* (C)2023 */
package org.transitclock.api.utils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Agency;
import org.transitclock.ipc.clients.ConfigInterfaceFactory;
import org.transitclock.ipc.interfaces.ConfigInterface;

/**
 * So that can get quick access to TimeZone for agency so that can properly format times and dates
 * for feed.
 *
 * @author Michael
 */
public class AgencyTimezoneCache {
    private static final HashMap<String, TimeZone> timezonesMap = new HashMap<String, TimeZone>();

    private static final Logger logger = LoggerFactory.getLogger(AgencyTimezoneCache.class);

    /**
     * Returns the TimeZone for the agency specified by the agencyId. The timezone is obtained from
     * the core agency server. Therefore it is cached to reduce requests to the server.
     *
     * @param agencyId
     * @return The TimeZone for the agency or null if could not be determined
     */
    public static TimeZone get(String agencyId) {
        // Trying getting timezone from cache
        TimeZone timezone = timezonesMap.get(agencyId);

        // If timezone not already in cache then get it and cache it
        if (timezone == null) {
            ConfigInterface inter = ConfigInterfaceFactory.get(agencyId);
            List<Agency> agencies;
            try {
                agencies = inter.getAgencies();
            } catch (RemoteException e) {
                logger.error("Exception getting timezone for agencyId={}", agencyId, e);
                return null;
            }

            // Use timezone of first agency
            String timezoneStr = agencies.get(0).getTimeZoneStr();
            if (timezoneStr == null) return null;
            timezone = TimeZone.getTimeZone(timezoneStr);

            // Cache the timezone
            timezonesMap.put(agencyId, timezone);
        }

        // Return the result
        return timezone;
    }
}
