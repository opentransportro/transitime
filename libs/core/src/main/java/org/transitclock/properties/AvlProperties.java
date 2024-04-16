package org.transitclock.properties;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AvlProperties {
    public static final int MAX_THREADS = 25;
    // config param: transitclock.avl.gtfsRealtimeFeedURI
    // The URI of the GTFS-realtime feed to use.
    private List<String> gtfsRealtimeFeedURI = new ArrayList<>();

    // config param: transitclock.avl.feedPollingRateSecs
    // How frequently an AVL feed should be polled for new data.
    private Integer feedPollingRateSecs = 5;

    // config param: transitclock.avl.feedTimeoutInMSecs
    // For when polling AVL XML feed. The feed logs error if the timeout value is exceeded when performing the XML request.
    private Integer feedTimeoutInMSecs = 10000;

    // config param: transitclock.avl.maxSpeed
    // Max speed between AVL reports for a vehicle. If this value is exceeded then the AVL report is ignored.
    private Double maxSpeed = 31.3;

    // config param: transitclock.avl.alternativemaxspeed
    // Alternative max speed between AVL reports for a vehicle. If this value is exceeded then the AVL report is ignored.
    private Double alternativeMaxSpeed = 15.0;

    // config param: transitclock.avl.maxStopPathsAhead
    // Max stopPaths ahead to look for match.
    private Integer maxStopPathsAhead = 999;

    // config param: transitclock.avl.minSpeedForValidHeading
    // If AVL report speed is below this threshold then the heading is not considered valid.
    private Double minSpeedForValidHeading = 1.5;

    // config param: transitclock.avl.minLatitude
    // For filtering out bad AVL reports. The default values of latitude 15.0 to 55.0 and longitude of -135.0 to -60.0 are for North America, including Mexico and Canada. Can see maps of lat/lon at http://www.mapsofworld.com/lat_long/north-america.html
    private Float minLatitude = 15.0F;

    // config param: transitclock.avl.maxLatitude
    // For filtering out bad AVL reports. The default values of latitude 15.0 to 55.0 and longitude of -135.0 to -60.0 are for North America, including Mexico and Canada. Can see maps of lat/lon at http://www.mapsofworld.com/lat_long/north-america.html
    private Float maxLatitude = 55.0F;

    // config param: transitclock.avl.minLongitude
    // For filtering out bad AVL reports. The default values of latitude 15.0 to 55.0 and longitude of -135.0 to -60.0 are for North America, including Mexico and Canada. Can see maps of lat/lon at http://www.mapsofworld.com/lat_long/north-america.html
    private Float minLongitude = -135.0F;

    // config param: transitclock.avl.maxLongitude
    // For filtering out bad AVL reports. The default values of latitude 15.0 to 55.0 and longitude of -135.0 to -60.0 are for North America, including Mexico and Canada. Can see maps of lat/lon at http://www.mapsofworld.com/lat_long/north-america.html
    private Float maxLongitude = -60.0F;

    // config param: transitclock.avl.unpredictableAssignmentsRegEx
    // So can filter out unpredictable assignments such as for training coaches, service vehicles, or simply vehicles that are not in service and should not be attempted to be made predictable. Returns empty string, the default value if transitclock.avl.unpredictableAssignmentsRegEx is not set.
    private String unpredictableAssignmentsRegEx = "";

    // config param: transitclock.avl.minTimeBetweenAvlReportsSecs
    // Minimum allowable time in seconds between AVL reports for a vehicle. If get a report closer than this number of seconds to the previous one then the new report is filtered out and not processed. Important for when reporting rate is really high, such as every few seconds.
    private Integer minTimeBetweenAvlReportsSecs = 5;

    // config param: transitclock.avl.url
    // The URL of the AVL feed to poll.
    private List<String> urls = null;

    // config param: transitclock.avl.authenticationUser
    // If authentication used for the feed then this specifies the user.
    private String authenticationUser = null;

    // config param: transitclock.avl.authenticationPassword
    // If authentication used for the feed then this specifies the password.
    private String authenticationPassword = null;

    // config param: transitclock.avl.shouldProcessAvl
    // Usually want to process the AVL data when it is read in so that predictions and such are generated. But if debugging then can set this param to false.
    private Boolean shouldProcessAvl = true;

    // config param: transitclock.avl.processInRealTime
    // For when getting batch of AVL data from a CSV file. When true then when reading in do at the same speed as when the AVL was created. Set to false it you just want to read in as fast as possible.
    private Boolean processInRealTime = false;

    // config param: transitclock.avl.queueSize
    // How many items to go into the blocking AVL queue before need to wait for queue to have space. Should be approximately 50% more than the number of reports that will be read during a single AVL polling cycle. If too big then wasteful. If too small then not all the data will be rejected by the ThreadPoolExecutor.
    private Integer queueSize = 2000;

    // config param: transitclock.avl.numThreads
    // How many threads to be used for processing the AVL data. For most applications just using a single thread is probably sufficient and it makes the logging simpler since the messages will not be interleaved. But for large systems with lots of vehicles then should use multiple threads, such as 3-15 so that more of the cores are used.
    private Integer numThreads = 1;

    public Integer getNumThreads() {
        if (numThreads < 1) {
            logger.error("Number of threads must be at least 1 but {} was " + "specified. Therefore using 1 thread.", numThreads);
            return  1;
        }

        if (numThreads > MAX_THREADS) {
            logger.error("Number of threads must be no greater than {} but {} was specified. Therefore using {} threads.", MAX_THREADS, numThreads, MAX_THREADS);
            return MAX_THREADS;
        }

        return numThreads;
    }
}
