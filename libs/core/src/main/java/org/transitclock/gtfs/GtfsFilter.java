package org.transitclock.gtfs;

import java.util.regex.Pattern;

public class GtfsFilter {
    private final Pattern routeIdFilterRegExPattern;
    private final Pattern tripIdFilterRegExPattern;

    public GtfsFilter(String routeIdFilterRegExPattern, String tripIdFilterRegExPattern) {
        if (routeIdFilterRegExPattern != null) {
            this.routeIdFilterRegExPattern = Pattern.compile(routeIdFilterRegExPattern);
        } else {
            this.routeIdFilterRegExPattern = null;
        }

        if (tripIdFilterRegExPattern != null) {
            this.tripIdFilterRegExPattern = Pattern.compile(tripIdFilterRegExPattern);
        } else {
            this.tripIdFilterRegExPattern = null;
        }
    }

    /**
     * Returns true if the tripId isn't supposed to be filtered out, as specified by the
     * transitclock.gtfs.tripIdRegExPattern property.
     *
     * @param tripId
     * @return True if trip not to be filtered out
     */
    public boolean tripNotFiltered(String tripId) {
        if (tripIdFilterRegExPattern == null)
            return true;

        return tripIdFilterRegExPattern.matcher(tripId.trim()).matches();
    }

    /**
     * Returns true if the routeId isn't supposed to be filtered out, as specified by the
     * transitclock.gtfs.routeIdRegExPattern property.
     *
     * @param routeId
     * @return True if route not to be filtered out
     */
    public boolean routeNotFiltered(String routeId) {
        if (routeIdFilterRegExPattern == null)
            return true;

        return routeIdFilterRegExPattern.matcher(routeId.trim()).matches();
    }

}
