/* (C)2023 */
package org.transitclock.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.transitclock.domain.structs.Agency;
import org.transitclock.service.contract.ConfigInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * So that can get quick access to TimeZone for agency so that can properly format times and dates
 * for feed.
 *
 * @author Michael
 */
@Slf4j
@Component
public class AgencyTimezoneCache {
    private final Map<String, TimeZone> timezonesMap = new HashMap<>();
    @Autowired
    private ConfigInterface configInterface;
    /**
     * Returns the TimeZone for the agency specified by the agencyId. The timezone is obtained from
     * the core agency server. Therefore it is cached to reduce requests to the server.
     *
     * @param agencyId
     * @return The TimeZone for the agency or null if could not be determined
     */
    public TimeZone get(String agencyId) {
        // Trying getting timezone from cache
        TimeZone timezone = timezonesMap.get(agencyId);

        // If timezone not already in cache then get it and cache it
        if (timezone == null) {
            List<Agency> agencies = configInterface.getAgencies();

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
