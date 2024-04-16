package org.transitclock.properties;

import lombok.Data;

@Data
public class GtfsProperties {
    // config param: transitclock.gtfs.capitalize
    // Sometimes GTFS titles have all capital letters or other capitalization issues. If set to true then will properly capitalize titles when process GTFS data. But note that this can require using regular expressions to fix things like acronyms that actually should be all caps.
    private Boolean capitalize = false;

    @Data
    public static class AutoUpdate {
        // config param: transitclock.gtfs.url
        // URL where to retrieve the GTFS file.
        private boolean enabled = false;
        // config param: transitclock.gtfs.url
        // URL where to retrieve the GTFS file.
        private String url = null;

        // config param: transitclock.gtfs.dirName
        // Directory on agency server where to place the GTFS file.
        private String dirName = "/var/transitclock/gtfs";

        // config param: transitclock.gtfs.intervalMsec
        // How long to wait before checking if GTFS file has changed on web
        private Long intervalMsec = 14400000L;
    }

    private AutoUpdate autoUpdate = new AutoUpdate();


    // config param: transitclock.gtfs.routeIdFilterRegEx
    // Route is included only if route_id matches the this regular expression. If only want routes with "SPECIAL" in the id then would use ".*SPECIAL.*". If want to filter out such trips would instead use the complicated "^((?!SPECIAL).)*$" or "^((?!(SPECIAL1|SPECIAL2)).)*$" if want to filter out two names. The default value of null causes all routes to be included.
    private String routeIdFilterRegEx = null;

    // config param: transitclock.gtfs.tripIdFilterRegEx
    // Trip is included only if trip_id matches the this regular expression. If only want trips with "SPECIAL" in the id then would use ".*SPECIAL.*". If want to filter out such trips would instead use the complicated "^((?!SPECIAL).)*$" or "^((?!(SPECIAL1|SPECIAL2)).)*$" if want to filter out two names. The default value of null causes all trips to be included.
    private String tripIdFilterRegEx = null;

    // config param: transitclock.gtfs.stopCodeBaseValue
    // If agency doesn't specify stop codes but simply wants to have them be a based number plus the stop ID then this parameter can specify the base value.
    private Integer stopCodeBaseValue = null;

    // config param: transitclock.gtfs.minDistanceBetweenStopsToDisambiguateHeadsigns
    // When disambiguating headsigns by appending the too stop name of the last stop, won't disambiguate if the last stops for the trips with the same headsign differ by less than this amount.
    private Double minDistanceBetweenStopsToDisambiguateHeadsigns = 1000.0;

    // config param: transitclock.gtfs.outputPathsAndStopsForGraphingRouteIds
    // Outputs data for specified routes grouped by trip pattern.The resulting data can be visualized on a map by cuttingand pasting it in to http://www.gpsvisualizer.com/map_inputSeparate multiple route ids with commas
    private String outputPathsAndStopsForGraphingRouteIds = null;


    // config param: transitclock.predAccuracy.gtfsTripUpdateUrl
    // URL to access gtfs-rt trip updates.
    private String gtfsTripUpdateUrl = "http://127.0.0.1:8091/trip-updates";

    // config param: transitclock.gtfs.tripShortNameRegEx
    // For agencies where trip short name not specified can use this regular expression to determine the short name from the trip ID by specifying a grouping. For example, to get name before a "-" would use something like "(.*?)-"
    private String tripShortNameRegEx = null;

    // config param: transitclock.gtfs.blockIdRegEx
    // For agencies where block ID from GTFS datda needs to be modified to match that of the AVL feed. Can use this regular expression to determine the proper block ID  by specifying a grouping. For example, to get name after a "xx-" would use something like "xx-(.*)"
    private String blockIdRegEx = null;

}
