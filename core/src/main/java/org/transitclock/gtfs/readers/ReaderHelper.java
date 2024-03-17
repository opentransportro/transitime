package org.transitclock.gtfs.readers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.transitclock.ApplicationProperties;

public class ReaderHelper {
    private final Pattern tripShortNameRegExPattern;
    private final Pattern blockIdRegExPattern;

    public ReaderHelper(ApplicationProperties.Gtfs gtfsProperties) {
        if(gtfsProperties.getTripShortNameRegEx() != null) {
            tripShortNameRegExPattern = Pattern.compile(gtfsProperties.getTripShortNameRegEx());
        } else {
            tripShortNameRegExPattern = null;
        }

        if (gtfsProperties.getBlockIdRegEx() != null) {
            blockIdRegExPattern = Pattern.compile(gtfsProperties.getBlockIdRegEx());
        } else {
            blockIdRegExPattern = null;
        }
    }

    /**
     * Many agencies don't specify a trip_short_name. For these use the trip_id or if the
     * transitclock.gtfs.tripShortNameRegEx is set to determine a group then use that group in the
     * tripId. For example, if tripId is "345-long unneeded description" and the regex is set to
     * "(.*?)-" then returned trip short name will be 345.
     *
     * @param tripShortName
     * @param tripId
     * @return The tripShortName if it is not null. Other returns a the first group specified by the
     *     regex on tripId, or the tripId if no regex defined or if no match.
     */
    public String computeTripShortName(String tripShortName, String tripId) {
        // If tripShortName provided then use it
        if (tripShortName != null) return tripShortName;

        // The tripShortName wasn't provided. If a regular expression specified
        // then use it. If regex not specified then return tripId.
        if (tripShortNameRegExPattern == null)
            return tripId;

        // Create the matcher
        Matcher m = tripShortNameRegExPattern.matcher(tripId);

        // If insufficient match then return tripId
        if (!m.find()) return tripId;
        if (m.groupCount() < 1) return tripId;

        // Return the first group. Note: group #0 is the entire string. Need to
        // use group #1 for the group match.
        return m.group(1);
    }

    /**
     * In case block IDs from GTFS needs to be modified to match block IDs from AVL feed. Uses the
     * property transitclock.gtfs.blockIdRegEx if it is not null.
     *
     * @param originalBlockId
     * @return the processed block ID
     */
    public String computeBlockId(String originalBlockId) {
        // If nothing to do then return original value
        if (originalBlockId == null || blockIdRegExPattern == null)
            return originalBlockId;

        // Create the matcher
        Matcher m = blockIdRegExPattern.matcher(originalBlockId);

        // If insufficient match return original value
        if (!m.find() || m.groupCount() < 1) return originalBlockId;

        // Return the first group. Note: group #0 is the entire string. Need to
        // use group #1 for the group match.
        return m.group(1);
    }
}
