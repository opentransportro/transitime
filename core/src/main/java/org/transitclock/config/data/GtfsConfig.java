package org.transitclock.config.data;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.Time;

import java.util.regex.Pattern;

public class GtfsConfig {
    public static final StringConfigValue url =
            new StringConfigValue("transitclock.gtfs.url", "URL where to retrieve the GTFS file.");

    public static final StringConfigValue dirName = new StringConfigValue(
            "transitclock.gtfs.dirName", "Directory on agency server where to place the GTFS file.");

    public static final LongConfigValue intervalMsec = new LongConfigValue(
            "transitclock.gtfs.intervalMsec",
            // Low cost unless file actually downloaded so do pretty
            // frequently so get updates as soon as possible
            4 * Time.MS_PER_HOUR,
            "How long to wait before checking if GTFS file has changed " + "on web");

    // So can process only routes that match a regular expression.
    // Note, see
    // http://stackoverflow.com/questions/406230/regular-expression-to-match-text-that-doesnt-contain-a-word
    // for details on how to filter out matches as opposed to specifying
    // which ones want to keep.
    public static StringConfigValue routeIdFilterRegEx = new StringConfigValue(
            "transitclock.gtfs.routeIdFilterRegEx",
            null, // Default of null means don't do any filtering
            "Route is included only if route_id matches the this regular "
                    + "expression. If only want routes with \"SPECIAL\" in the id then "
                    + "would use \".*SPECIAL.*\". If want to filter out such trips "
                    + "would instead use the complicated \"^((?!SPECIAL).)*$\" or "
                    + "\"^((?!(SPECIAL1|SPECIAL2)).)*$\" "
                    + "if want to filter out two names. The default value "
                    + "of null causes all routes to be included.");

    // So can process only trips that match a regular expression.
    // Default of null means don't do any filtering
    public static StringConfigValue tripIdFilterRegEx = new StringConfigValue(
            "transitclock.gtfs.tripIdFilterRegEx",
            null, // Default of null means don't do any filtering
            "Trip is included only if trip_id matches the this regular "
                    + "expression. If only want trips with \"SPECIAL\" in the id then "
                    + "would use \".*SPECIAL.*\". If want to filter out such trips "
                    + "would instead use the complicated \"^((?!SPECIAL).)*$\" or "
                    + "\"^((?!(SPECIAL1|SPECIAL2)).)*$\" "
                    + "if want to filter out two names. The default value "
                    + "of null causes all trips to be included.");



    public static IntegerConfigValue stopCodeBaseValue = new IntegerConfigValue(
            "transitclock.gtfs.stopCodeBaseValue",
            "If agency doesn't specify stop codes but simply wants to "
                    + "have them be a based number plus the stop ID then this "
                    + "parameter can specify the base value. ");

    public static DoubleConfigValue minDistanceBetweenStopsToDisambiguateHeadsigns = new DoubleConfigValue(
            "transitclock.gtfs.minDistanceBetweenStopsToDisambiguateHeadsigns",
            1000.0,
            "When disambiguating headsigns by appending the too stop "
                    + "name of the last stop, won't disambiguate if the last "
                    + "stops for the trips with the same headsign differ by "
                    + "less than this amount.");

    public static StringConfigValue outputPathsAndStopsForGraphingRouteIds = new StringConfigValue(
            "transitclock.gtfs.outputPathsAndStopsForGraphingRouteIds",
            null, // Default of null means don't output any routes
            "Outputs data for specified routes grouped by trip pattern."
                    + "The resulting data can be visualized on a map by cutting"
                    + "and pasting it in to http://www.gpsvisualizer.com/map_input"
                    + "Separate multiple route ids with commas");



    public static final StringConfigValue GTFS_REALTIME_URI = new StringConfigValue(
            "transitclock.avl.gtfsRealtimeFeedURI",
            null,
            "The URI of the GTFS-realtime feed to use.");




    public static final StringConfigValue gtfsTripUpdateUrl = new StringConfigValue(
            "transitclock.predAccuracy.gtfsTripUpdateUrl",
            "http://127.0.0.1:8091/trip-updates",
            "URL to access gtfs-rt trip updates.");

    /**
     * @return the gtfstripupdateurl
     */
    public static StringConfigValue getGtfstripupdateurl() {
        return gtfsTripUpdateUrl;
    }




    // For determining a trip_short_name from the trip_id if the
    // trip_short_name is not specified in GTFS file.
    // Default of null means simply use trip_id without any modification.
    public static StringConfigValue tripShortNameRegEx = new StringConfigValue(
            "transitclock.gtfs.tripShortNameRegEx",
            null,
            "For agencies where trip short name not specified can use this "
                    + "regular expression to determine the short name from the trip "
                    + "ID by specifying a grouping. For example, to get name before "
                    + "a \"-\" would use something like \"(.*?)-\"");
    public static Pattern tripShortNameRegExPattern = null;

    // For determining proper block_id that corresponds to AVL feed
    // Default of null means simply use block_id without any modification.
    public static StringConfigValue blockIdRegEx = new StringConfigValue(
            "transitclock.gtfs.blockIdRegEx",
            null,
            "For agencies where block ID from GTFS datda needs to be modified "
                    + "to match that of the AVL feed. Can use this "
                    + "regular expression to determine the proper block ID "
                    + " by specifying a grouping. For example, to get name after "
                    + "a \"xx-\" would use something like \"xx-(.*)\"");
    public static Pattern blockIdRegExPattern = null;
}
