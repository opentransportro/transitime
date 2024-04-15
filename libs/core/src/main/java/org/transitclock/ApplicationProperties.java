package org.transitclock;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "transitclock")
public class ApplicationProperties {

    @Data
    public static class Api {
        // config param: transitclock.api.gtfsRtCacheSeconds
        // How long to cache GTFS Realtime
        private Integer gtfsRtCacheSeconds = 15;

        // config param: transitclock.api.predictionMaxFutureSecs
        // Number of seconds in the future to accept predictions before
        private Integer predictionMaxFutureSecs = 3600;

        // config param: transitclock.api.includeTripUpdateDelay
        // Whether to include delay in the TripUpdate message
        private Boolean includeTripUpdateDelay = false;

    }
    private Api api = new Api();

    @Data
    public static class ArrivalsDepartures {
        // config param: transitclock.arrivalsDepartures.maxStopsWhenNoPreviousMatch
        // If vehicle just became predictable as indicated by no previous match then still want to determine arrival/departure times for earlier stops so that won't miss recording data for them them. But only want to go so far. Otherwise could be generating fake arrival/departure times when vehicle did not actually traverse that stop.
        private Integer maxStopsWhenNoPreviousMatch = 1;

        // config param: transitclock.arrivalsDepartures.maxStopsBetweenMatches
        // If between AVL reports the vehicle appears to traverse many stops then something is likely wrong with the matching. So this parameter is used to limit how many arrivals/departures are created between AVL reports.
        private Integer maxStopsBetweenMatches = 12;

        // config param: transitclock.arrivalsDepartures.allowableDifferenceBetweenAvlTimeSecs
        // If the time of a determine arrival/departure is really different from the AVL time then something must be wrong and the situation will be logged.
        private Integer allowableDifferenceBetweenAvlTimeSecs = 86400;

    }
    private ArrivalsDepartures arrivalsDepartures = new ArrivalsDepartures();

    @Data
    @Slf4j
    public static class Avl {
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
                numThreads = 1;
            }

            if (numThreads > MAX_THREADS) {
                logger.error("Number of threads must be no greater than {} but {} was specified. Therefore using {} threads.", MAX_THREADS, numThreads, MAX_THREADS);
                numThreads = MAX_THREADS;
            }

            return numThreads;
        }
    }
    private Avl avl = new Avl();

    @Data
    public static class AutoBlockAssigner {
        // config param: transitclock.autoBlockAssigner.autoAssignerEnabled
        // Set to true to enable the auto assignment feature where the system tries to assign vehicle to an available block
        private boolean autoAssignerEnabled = false;

        // config param: transitclock.autoBlockAssigner.ignoreAvlAssignments
        // For when want to test automatic assignments. When set to true then system ignores assignments from AVL feed so vehicles need to be automatically assigned instead
        private boolean ignoreAvlAssignments = false;

        // config param: transitclock.autoBlockAssigner.minDistanceFromCurrentReport
        // AutoBlockAssigner looks at two AVL reports to match vehicle. This parameter specifies how far away those AVL reports need to be sure that the vehicle really is moving and in service. If getting incorrect matches then this value should likely be increased.
        private Double minDistanceFromCurrentReport = 100.0;

        // config param: transitclock.autoBlockAssigner.allowableEarlySeconds
        // How early a vehicle can be in seconds and still be automatically assigned to a block
        private Integer allowableEarlySeconds = 180;

        // config param: transitclock.autoBlockAssigner.allowableLateSeconds
        // How late a vehicle can be in seconds and still be automatically assigned to a block
        private Integer allowableLateSeconds = 300;

        // config param: transitclock.autoBlockAssigner.minTimeBetweenAutoAssigningSecs
        // Minimum time per vehicle that can do auto assigning. Auto assigning is computationally expensive, especially when there are many blocks. Don't need to do it that frequently. Especially important for agencies with high reporting rates. So this param allows one to limit how frequently auto assigner called for vehicle
        private Integer minTimeBetweenAutoAssigningSecs = 30;

    }
    private AutoBlockAssigner autoBlockAssigner = new AutoBlockAssigner();

    @Data
    public static class Core {
        // config param: transitclock.core.agencyId
        // Specifies the ID of the agency. Used for the database name and in the logback configuration to specify the directory where to put the log files.
        private String agencyId = null;

        // config param: transitclock.core.timezone
        // For setting timezone for application. Ideally would get timezone from the agency db but once a Hibernate session factory is created, such as for reading timezone from db, then it is too late to set the timezone. Therefore this provides ability to set it manually.
        private String timezone = null;

        @Data
        public static class Cache {
            // config param: transitclock.core.cache.daysPopulateHistoricalCache
            // How many days data to read in to populate historical cache on start up.
            private Integer daysPopulateHistoricalCache = 0;
        }

        private Cache cache = new Cache();

        // config param: transitclock.core.storeDataInDatabase
        // When in playback mode or some other situations don't want to store generated data such as arrivals/departures, events, and such to the database because only debugging.
        private Boolean storeDataInDatabase = true;

        // config param: transitclock.core.onlyNeedArrivalDepartures
        // When batching large amount of AVL data through system to generate improved schedule time (as has been done for Zhengzhou) it takes huge amount of time to process everything. To speed things up you can set -Dtransitclock.core.onlyNeedArrivalDepartures=true such that the system will be sped up by not generating nor logging predictions, not logging AVL data nor storing it in db, and not logging nor storing match data in db.
        private Boolean onlyNeedArrivalDepartures = false;

        // config param: transitclock.core.pauseIfDbQueueFilling
        // When in batch mode can flood db with lots of objects. Iftransitclock.core.pauseIfDbQueueFilling is set to true then when objects are put into the DataDbLogger queue the calling thread will be temporarily suspended so that the separate thread can run to write to the db and thereby empty out the queue.
        private Boolean pauseIfDbQueueFilling = false;

        // config param: transitclock.core.maxDistanceFromSegment
        // How far a location can be from a path segment and still be considered a match. Can be overridden on a per route basis via max_distance supplemental column of route GTFS data. When auto assigning, the parameter transitclock.core.maxDistanceFromSegmentForAutoAssigning is used instead.
        private Double maxDistanceFromSegment = 60.0;

        // config param: transitclock.core.maxDistanceFromSegmentForAutoAssigning
        // How far a location can be from a path segment and still be considered a match when auto assigning a vehicle. Auto assigning is treated separately because sometimes need to make maxDistanceFromSegment really lenient because vehicles do not always follow same route. This is especially true with deviated fixed route service. But don't want the distance to be too lenient when auto assigning because if too lenient then would match to multiple routes and therefore no assignment would be made because the matches would be ambiguous. Therefore maxDistanceFromSegmentForAutoAssigning should be lessthan or equal to maxDistanceFromSegment.
        private Double maxDistanceFromSegmentForAutoAssigning = 60.0;

        // config param: transitclock.core.allowableNumberOfBadMatches
        // How many bad spatial/temporal matches a predictable vehicle can have in a row before the vehicle is made unpredictable.
        private Integer allowableNumberOfBadMatches = 2;

        // config param: transitclock.core.maxHeadingOffsetFromSegment
        // How far heading in degrees of vehicle can be away from path segment and still be considered a match. Needs to be pretty lenient because stopPaths and heading might not be that accurate.
        private Float maxHeadingOffsetFromSegment = 360.0F;

        // config param: transitclock.core.distanceFromEndOfBlockForInitialMatching
        // For initial matching of vehicle to block assignment. If vehicle is closer than this distance from the end of the block then the spatial match will not be used. This is to prevent a vehicle that has already completed its block from wrongly being assigned to that block again.
        private Double distanceFromEndOfBlockForInitialMatching = 250.0;

        // config param: transitclock.core.distanceFromLastStopForEndMatching
        // How close vehicle needs to be from the last stop of the block such that the next AVL report should possibly be considered to match to the end of the block. This is important for determining the arrival time at the laststop of the block even if don't get an AVL report near that stop.
        private Double distanceFromLastStopForEndMatching = 250.0;

        // config param: transitclock.core.deadheadingShortVersusLongDistance
        // For determining if enough time to deadhead to beginning of a trip. If vehicles are far away then they are more likely to be able to travel faster because they could take a freeway or other fast road. But when get closer then will be on regular streets and will travel more slowly. The parameters should be set in a conservative way such that the travel time is underestimated by using slower speeds than will actually encounter. This way the vehicle will arrive after the predicted time which means that passenger won't miss the bus.
        private Float deadheadingShortVersusLongDistance = 1000.0F;

        // config param: transitclock.core.shortDistanceDeadheadingSpeed
        // Part of determining if enough time to deadhead to layover.
        private Float shortDistanceDeadheadingSpeed = 4.0F;

        // config param: transitclock.core.longDistanceDeadheadingSpeed
        // Part of determining if enough time to deadhead to layover.
        private Float longDistanceDeadheadingSpeed = 10.0F;

        // config param: transitclock.core.maxPredictionTimeForDbSecs
        // For determining if prediction should be stored in db. Set to 0 to never store predictions. A very large number of predictions is created so be careful with this value so that the db doesn't get filled up too quickly.
        private Integer maxPredictionTimeForDbSecs = 1800;

        // config param: transitclock.core.allowableEarlyForLayoverSeconds
        // How early a vehicle can be and still be matched to a layover. Needs to be pretty large because sometimes vehicles will be assigned to a layover quite early, and want to be able to make the vehicle predictable and generate predictions far in advance. Don't want it to be too large, like 90 minutes, though because then could match incorrectly if vehicle simply stays at terminal.
        private Integer allowableEarlyForLayoverSeconds = 3600;

        // config param: transitclock.core.allowableEarlySeconds
        // How early a vehicle can be and still be matched to its block assignment. If when a new AVL report is received for a predictable vehicle and it is found with respect to the real-time schedule adherence to be earlier than this value the vehicle will be made unpredictable.
        private Integer allowableEarlySeconds = 900;

        // config param: transitclock.core.allowableLateSeconds
        // How late a vehicle can be and still be matched to its block assignment. If when a new AVL report is received for a predictable vehicle and it is found with respect to the real-time schedule adherence to be later than this value the vehicle will be made unpredictable.
        private Integer allowableLateSeconds = 5400;

        // config param: transitclock.core.allowableEarlySecondsForInitialMatching
        // How early a vehicle can be in seconds and still be matched to its block assignment.
        private Integer allowableEarlySecondsForInitialMatching = 600;

        // config param: transitclock.core.allowableLateSecondsForInitialMatching
        // How late a vehicle can be in seconds and still be matched to its block assignment.
        private Integer allowableLateSecondsForInitialMatching = 1200;

        // config param: transitclock.core.distanceBetweenAvlsForInitialMatchingWithoutHeading
        // For initial matching vehicle to assignment when there isn't any heading information. In that case also want to match to previous AVL report. This parameter specifies how far, as the crow flies, the previous AVL report to be used from the VehicleState AvlReport history is from the current AvlReport.
        private Double distanceBetweenAvlsForInitialMatchingWithoutHeading = 100.0;

        // config param: transitclock.core.distanceFromLayoverForEarlyDeparture
        // How far along path past a layover stop a vehicle needs to be in order for it to be considered an early departure instead of just moving around within the layover. Needs to be a decent distance since the stop locations are not always accurate.
        private Double distanceFromLayoverForEarlyDeparture = 180.0;

        // config param: transitclock.core.layoverDistance
        // How far vehicle can be away from layover stop and still match to it. For when not deadheading to a trip. This can be useful to determine when a vehicle is pulled out of service when it is expected to be at a layover and start the next trip. Should usually be a pretty large value so that vehicles are not taken out of service just because they drive a bit away from the stop for the layover, which is pretty common.
        private Double layoverDistance = 2000.0;

        // config param: transitclock.core.allowableEarlyTimeForEarlyDepartureSecs
        // How early in seconds a vehicle can have left terminal and have it be considered an early departure instead of just moving around within the layover. Don't want to mistakingly think that vehicles moving around during layover have started their trip early. Therefore this value should be limited to just a few minutes since vehicles usually don't leave early.
        private Integer allowableEarlyTimeForEarlyDepartureSecs = 300;

        // config param: transitclock.core.allowableEarlyDepartureTimeForLoggingEvent
        // How early in seconds a vehicle can depart a terminal before it registers a VehicleEvent indicating a problem.
        private Integer allowableEarlyDepartureTimeForLoggingEvent = 60;

        // config param: transitclock.core.allowableLateDepartureTimeForLoggingEvent
        // How late in seconds a vehicle can depart a terminal before it registers a VehicleEvent indicating a problem.
        private Integer allowableLateDepartureTimeForLoggingEvent = 240;

        // config param: transitclock.core.allowableLateAtTerminalForLoggingEvent
        // If a vehicle is sitting at a terminal and provides another GPS report indicating that it is more than this much later, in seconds, than the configured departure time then a VehicleEvent is created to record the problem.
        private Integer allowableLateAtTerminalForLoggingEvent = 60;

        // config param: transitclock.core.beforeStopDistance
        // How far a vehicle can be ahead of a stop in meters and be considered to have arrived.
        private Double beforeStopDistance = 50.0;

        // config param: transitclock.core.afterStopDistance
        // How far a vehicle can be past a stop in meters and still be considered at the stop.
        private Double afterStopDistance = 50.0;

        // config param: transitclock.core.defaultBreakTimeSec
        // How long driver is expected to have a break for a stop.
        private Integer defaultBreakTimeSec = 0;

        // config param: transitclock.core.earlyToLateRatio
        // How much worse it is for a vehicle to be early as opposed to late when determining schedule adherence.
        private Double earlyToLateRatio = 3.0;

        // config param: transitclock.core.exclusiveBlockAssignments
        // True if block assignments should be exclusive. If set to true then when a vehicle is assigned to a block the system will unassign any other vehicles that were assigned to the block. Important for when AVL system doesn't always provide logout info. Also used by the AutoBlockAssigner to determine which active blocks to possibly assign a vehicle to. For no schedule routes can set to false to allow multiple vehicles be assigned to a route.
        private Boolean exclusiveBlockAssignments = true;

        // config param: transitclock.core.timeForDeterminingNoProgress
        // The interval in msec at which look at vehicle's history to determine if it is not making any progress. A valueof 0 disables this feature. If vehicle is found to not be making progress it is made unpredictable.
        private Integer timeForDeterminingNoProgress = 480000;

        // config param: transitclock.core.minDistanceForNoProgress
        // Minimum distance vehicle is expected to travel over timeForDeterminingNoProgress to indicate vehicle is making progress. If vehicle is found to not be making progress it is made unpredictable.
        private Double minDistanceForNoProgress = 60.0;

        // config param: transitclock.core.timeForDeterminingDelayedSecs
        // The interval in msec at which look at vehicle's history to determine if it is delayed.
        private Integer timeForDeterminingDelayedSecs = 240;

        // config param: transitclock.core.minDistanceForDelayed
        // Minimum distance vehicle is expected to travel over timeForDeterminingDelayed to indicate vehicle is delayed.
        private Double minDistanceForDelayed = 60.0;

        // config param: transitclock.core.matchHistoryMaxSize
        // How many matches are kept in history for vehicle so that can can do things such as look back at history to determine if vehicle has broken down. Should be large enough so can store all matchets generated over timeForDeterminingNoProgress. If GPS rate is high then this value will need to be high as well.
        private Integer matchHistoryMaxSize = 20;

        // config param: transitclock.core.avlHistoryMaxSize
        // How many AVL reports are kept in history for vehicle so that can can do things such as look back at history to determine if vehicle has broken down. Should be large enough so can store all AVL reports received over timeForDeterminingNoProgress. If GPS rate is high then this value will need to be high as well.
        private Integer avlHistoryMaxSize = 20;

        // config param: transitclock.core.eventHistoryMaxSize
        // How many arrival depature event reports are kept in history for vehicle so that can can do things such as look back at history
        private Integer eventHistoryMaxSize = 20;

        // config param: transitclock.core.maxPredictionsTimeSecs
        // How far forward into the future should generate predictions for.
        private Integer maxPredictionsTimeSecs = 1800;

        // config param: transitclock.core.generateHoldingTimeWhenPredictionWithin
        // If the prediction is less than this number of milliseconds from current time then use it to generate a holding time
        private Long generateHoldingTimeWhenPredictionWithin = 0L;

        // config param: transitclock.core.useArrivalPredictionsForNormalStops
        // For specifying whether to use arrival predictions or departure predictions for normal, non-wait time, stops.
        private Boolean useArrivalPredictionsForNormalStops = true;

        // config param: transitclock.core.maxLateCutoffPredsForNextTripsSecs
        // If a vehicle is further behind schedule than this amount then predictions for subsequent trips will be marked as being uncertain. This is useful for when another vehicle might take over the next trip for the block due to vehicle being late.
        private Integer maxLateCutoffPredsForNextTripsSecs = 2147483647;

        // config param: transitclock.core.useExactSchedTimeForWaitStops
        // The predicted time for wait stops includes the historic wait stop time. This means it will be a bit after the configured schedule time. But some might not want to see such adjusted times. Plus just showing the schedule time is more conservative, and therefore usually better. If this value is set to true then the actual schedule time will be used. If false then the schedule time plus the wait stop time will be used.
        private Boolean useExactSchedTimeForWaitStops = true;

        // config param: transitclock.core.useHoldingTimeInPrediction
        // Add holding time to prediction.
        private Boolean useHoldingTimeInPrediction = false;

        // config param: transitclock.core.terminalDistanceForRouteMatching
        // How far vehicle must be away from the terminal before doing initial matching. This is important because when vehicle is at terminal don't know which trip it it should be matched to until vehicle has left the terminal.
        private Double terminalDistanceForRouteMatching = 100.0;

        // config param: transitclock.core.allowableBadAssignments
        // If get a bad assignment, such as no assignment, but no more than allowableBadAssignments then will use the previous assignment. Useful for when assignment part of AVL feed doesn't always provide a valid assignment.
        private Integer allowableBadAssignments = 0;

        // config param: transitclock.core.emailMessagesWhenAssignmentGrabImproper
        // When one vehicle gets assigned by AVL feed but another vehicle already has that assignment then sometimes the assignment to the new vehicle would be incorrect. Could be that vehicle was never logged out or simply got bad assignment. For this situation it can be useful to receive error message via e-mail. But can get too many such e-mails. This property allows one to control those e-mails.
        private Boolean emailMessagesWhenAssignmentGrabImproper = false;

        // config param: transitclock.core.maxDistanceForAssignmentGrab
        // For when another vehicles gets assignment and needs to grab it from another vehicle. The new vehicle must match to route within maxDistanceForAssignmentGrab in order to grab the assignment.
        private Double maxDistanceForAssignmentGrab = 10000.0;

        // config param: transitclock.core.maxMatchDistanceFromAVLRecord
        // For logging distance between spatial match and actual AVL assignment
        private Double maxMatchDistanceFromAVLRecord = 500.0;

        // config param: transitclock.core.ignoreInactiveBlocks
        // If the block isn't active at this time then ignore it. This way don't look at each trip to see if it is active which is important because looking at each trip means all the trip data including travel times needs to be lazy loaded, which can be slow.
        private Boolean ignoreInactiveBlocks = true;

        // config param: transitclock.core.storeTravelTimeStopPathPredictions
        // This is set to true to record all travelTime  predictions for individual stopPaths generated. Useful for comparing performance of differant algorithms. (MAPE comparison). Not for normal use as will generate massive amounts of data.
        private Boolean storeTravelTimeStopPathPredictions = false;

        // config param: transitclock.core.storeDwellTimeStopPathPredictions
        // This is set to true to record all travelTime  predictions for individual dwell times generated. Useful for comparing performance of differant algorithms. (MAPE comparison). Not for normal use as will generate massive amounts of data.
        private Boolean storeDwellTimeStopPathPredictions = false;

        // config param: transitclock.core.spatialMatchToLayoversAllowedForAutoAssignment
        // Allow auto assigner consider spatial matches to layovers. Experimental.
        private Boolean spatialMatchToLayoversAllowedForAutoAssignment = false;

        // config param: transitclock.blockLoading.agressive
        // Set true to eagerly fetch all blocks into memory on startup
        private Boolean agressive = false;

        @Data
        public static class PredictionGenerator {
            @Data
            public static class Bias {
                @Data
                public static class Exponential {
                    // config param: org.transitclock.core.predictiongenerator.bias.exponential.b
                    // Base number to be raised to the power of the horizon minutes. y=a(b^x)+c.
                    private Double b = 1.1;

                    // config param: org.transitclock.core.predictiongenerator.bias.exponential.a
                    // Multiple.y=a(b^x)+c.
                    private Double a = 0.5;

                    // config param: org.transitclock.core.predictiongenerator.bias.exponential.c
                    // Constant. y=a(b^x)+c.
                    private Double c = -0.5;

                    // config param: org.transitclock.core.predictiongenerator.bias.exponential.updown
                    // Is the adjustment up or down? Set +1 or -1.
                    private Integer updown = -1;
                }

                private Exponential exponential = new Exponential();

                @Data
                public static class Linear {
                    // config param: org.transitclock.core.predictiongenerator.bias.linear.rate
                    // Rate at which percentage adjustment changes with horizon.
                    private Double rate = 6.0E-4;

                    // config param: org.transitclock.core.predictiongenerator.bias.linear.updown
                    // Is the adjustment up or down? Set +1 or -1.
                    private Integer updown = -1;
                }

                private Linear linear = new Linear();
            }

            private Bias bias = new Bias();
        }

        private PredictionGenerator predictionGenerator = new PredictionGenerator();



        // config param: transitclock.core.blockactiveForTimeBeforeSecs
        // Now many seconds before the start of a block it will be considered active.
        private Integer blockactiveForTimeBeforeSecs = 0;

        // config param: transitclock.core.blockactiveForTimeAfterSecs
        // Now many seconds after the end of a block it will be considered active.
        private Integer blockactiveForTimeAfterSecs = -1;

        // config param: transitclock.core.maxDwellTime
        // This is a maximum dwell time at a stop to be taken into account for cache or prediction calculations.
        private Integer maxDwellTime = 600000;

        @Data
        public static class Frequency {
            // config param: transitclock.core.frequency.minTravelTimeFilterValue
            // The value to be included in average calculation for Travel times must exceed this value.
            private Integer minTravelTimeFilterValue = 0;

            // config param: transitclock.core.frequency.maxTravelTimeFilterValue
            // The value to be included in average calculation for Travel times must be less than this value.
            private Integer maxTravelTimeFilterValue = 600000;

            // config param: transitclock.core.frequency.minDwellTimeFilterValue
            // The value to be included in average calculation for dwell time must exceed this value.
            private Integer minDwellTimeFilterValue = 0;

            // config param: transitclock.core.frequency.maxDwellTimeFilterValue
            // The value to be included in average calculation for dwell time must be less this value.
            private Integer maxDwellTimeFilterValue = 600000;

            // config param: transitclock.core.frequency.cacheIncrementsForFrequencyService
            // This is the intervals size of the day that the average is applied to.
            private Integer cacheIncrementsForFrequencyService = 10800;
        }

        private Frequency frequency = new Frequency();

        // config param: transitclock.core.cacheReloadStartTimeStr
        // Date and time of when to start reading arrivaldepartures to inform caches.
        private String cacheReloadStartTimeStr = "";

        // config param: transitclock.core.cacheReloadEndTimeStr
        // Date and time of when to end reading arrivaldepartures to inform caches.
        private String cacheReloadEndTimeStr = "";

    }
    private Core core = new Core();

    @Data
    public static class Gtfs {
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
    private Gtfs gtfs = new Gtfs();

    @Data
    public static class Holding {
        // config param: transitclock.holding.storeHoldingTimes
        // This is set to true to record all holding times.
        private Boolean storeHoldingTimes = true;

        // config param: transitclock.holding.maxPredictionsForHoldingTimeCalculation
        // This is the maximim number of arrival predictions to include in holding time calculation
        private Integer maxPredictionsForHoldingTimeCalculation = 3;

        // config param: transitclock.holding.usearrivalevents
        // Generate a holding time on arrival events.
        private Boolean usearrivalevents = true;

        // config param: transitclock.holding.usearrivalpredictions
        // Generate a holding time on arrival predictions.
        private Boolean usearrivalpredictions = true;

        // config param: transitclock.holding.regenerateondeparture
        // Regenerate a holding time for all vehicles at control point when a vehicle departs the control point.
        private Boolean regenerateondeparture = false;

        // config param: transitclock.holding.plannedHeadwayMsec
        // Planned Headway
        private Integer plannedHeadwayMsec = 540000;

        // config param: transitclock.holding.controlStops
        // This is a list of stops to generate holding times for.
        private List<String> controlStops = new ArrayList<>();

    }

    private Holding holding = new Holding();

    @Data
    public static class Monitoring {
        // config param: transitclock.monitoring.maxQueueFraction
        // If database queue fills up by more than this 0.0 - 1.0 fraction then database monitoring is triggered.
        private Double maxQueueFraction = 0.4;

        // config param: transitclock.monitoring.maxQueueFractionGap
        // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now below maxQueueFraction - maxQueueFractionGap
        private Double maxQueueFractionGap = 0.1;

        // config param: transitclock.monitoring.minPredictableBlocks
        // The minimum fraction of currently active blocks that should have a predictable vehicle
        private Double minPredictableBlocks = 0.5;

        // config param: transitclock.monitoring.minPredictableBlocksGap
        // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now above minPredictableBlocks + minPredictableBlocksGap
        private Double minPredictableBlocksGap = 0.25;

        // config param: transitclock.monitoring.minimumPredictableVehicles
        // When looking at small number of vehicles it is too easy to get below minimumPredictableBlocks. So number of predictable vehicles is increased to this amount if below when determining the fraction.
        private Integer minimumPredictableVehicles = 3;

        // config param: transitclock.monitoring.cpuThreshold
        // If CPU load averaged over a minute exceeds this 0.0 - 1.0 value then CPU monitoring is triggered.
        private Double cpuThreshold = 0.99;

        // config param: transitclock.monitoring.cpuThresholdGap
        // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now below cpuThreshold - cpuThresholdGap
        private Double cpuThresholdGap = 0.1;

        // config param: transitclock.monitoring.allowableNoAvlSecs
        // How long in seconds that can not receive valid AVL data before monitoring triggers an alert.
        private Integer allowableNoAvlSecs = 300;

        // config param: transitclock.monitoring.avlFeedEmailRecipients
        // Comma separated list of e-mail addresses indicating who should be e-mail when monitor state changes for AVL feed.
        private String avlFeedEmailRecipients = "monitoring@transitclock.org";

        // config param: transitclock.monitoring.emailRecipients
        // Comma separated list of e-mail addresses indicating who should be e-mailed when monitor state changes.
        private String emailRecipients = null;

        // config param: transitclock.monitoring.retryTimeoutSecs
        // How long in seconds system should wait before rexamining monitor. This way a short lived outage can be ignored. 0 seconds means do not retry.
        private Integer retryTimeoutSecs = 5;

        // config param: transitclock.monitoring.secondsBetweenMonitorinPolling
        // How frequently an monitoring should be run to look for problems.
        private Integer secondsBetweenMonitorinPolling = 120;

        // config param: transitclock.monitoring.usableDiskSpaceThreshold
        // If usable disk space is less than this value then file space monitoring is triggered.
        private Long usableDiskSpaceThreshold = 1073741824L;

        // config param: transitclock.monitoring.usableDiskSpaceThresholdGap
        // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now above usableDiskSpaceThreshold + usableDiskSpaceThresholdGap
        private Long usableDiskSpaceThresholdGap = 104857600L;

        // config param: transitclock.monitoring.availableFreePhysicalMemoryThreshold
        // If available free physical memory is less than this value then free memory monitoring is triggered. This should be relatively small since on Linux the operating system will use most of the memory for buffers and such when it is available. Therefore even when only a small amount of memory is available the system is still OK.
        private Long availableFreePhysicalMemoryThreshold = 10485760L;

        // config param: transitclock.monitoring.availableFreePhysicalMemoryThresholdGap
        // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now above availableFreePhysicalMemoryThreshold + availableFreePhysicalMemoryThresholdGap
        private Long availableFreePhysicalMemoryThresholdGap = 157286400L;

    }

    private Monitoring monitoring = new Monitoring();

    @Data
    public static class PredictionAccuracy {
        // config param: transitclock.predAccuracy.pollingRateMsec
        // How frequently to query predictions for determining prediction accuracy.
        private Integer pollingRateMsec = 240000;

        // config param: transitclock.predAccuracy.maxPredTimeMinutes
        // Maximum time into the future for a pediction for it to be stored in memory for prediction accuracy analysis.
        private Integer maxPredTimeMinutes = 15;

        // config param: transitclock.predAccuracy.maxPredStalenessMinutes
        // Maximum time in minutes a prediction cam be into the past before it is removed from memory because no corresponding arrival/departure time was determined.
        private Integer maxPredStalenessMinutes = 15;

        // config param: transitclock.predAccuracy.stopsPerTrip
        // Number of stops per trip pattern that should collect prediction data for each polling cycle.
        private Integer stopsPerTrip = 5;

        // config param: transitclock.predAccuracy.maxRandomStopSelectionsPerTrip
        // Max number of random stops to look at to get the stopsPerTrip.
        private Integer maxRandomStopSelectionsPerTrip = 100;

        // config param: transitclock.predAccuracy.maxLatenessComparedToPredictionMsec
        // How late in msec a vehicle can arrive/departure a stop compared to the prediction and still have the prediction be considered a match.
        private Integer maxLatenessComparedToPredictionMsec = 1500000;

        // config param: transitclock.predAccuracy.maxEarlynessComparedToPredictionMsec
        // How early in msec a vehicle can arrive/departure a stop compared to the prediction and still have the prediction be considered a match.
        private Integer maxEarlynessComparedToPredictionMsec = 900000;

    }

    private PredictionAccuracy predAccuracy = new PredictionAccuracy();

    @Data
    public static class Prediction {
        // config param: transitclock.prediction.closestvehiclestopsahead
        // Num stops ahead a vehicle must be to be considers in the closest vehicle calculation
        private Integer closestvehiclestopsahead = 5;

        // config param: transitclock.prediction.returnArrivalPredictionForEndOfTrip
        // This set to false will not return arrival predictions of the last stop on a trip.
        private Boolean returnArrivalPredictionForEndOfTrip = false;

        @lombok.Data
        public static class Data {
            @lombok.Data
            public static class Kalman {
                // config param: transitclock.prediction.data.kalman.mindays
                // Min number of days trip data that needs to be available before Kalman prediciton is used instead of default transiTime prediction.
                private Integer mindays = 3;

                // config param: transitclock.prediction.data.kalman.maxdays
                // Max number of historical days trips to include in Kalman prediction calculation.
                private Integer maxdays = 3;

                // config param: transitclock.prediction.data.kalman.maxdaystoseach
                // Max number of days to look back for data. This will also be effected by how old the data in the cache is.
                private Integer maxdaystosearch = 30;

                // config param: transitclock.prediction.data.kalman.initialerrorvalue
                // Initial Kalman error value to use to start filter.
                private Double initialerrorvalue = 100.0;

                // config param: transitclock.prediction.data.kalman.usekalmanforpartialstoppaths
                // Will use Kalman prediction to get to first stop of prediction.
                private Boolean usekalmanforpartialstoppaths = true;


                // config param: transitclock.prediction.data.kalman.percentagePredictionMethodDifferencene
                // If the difference in prediction method estimates is greater than this percentage log a Vehicle Event
                private Integer percentagePredictionMethodDifferencene = 50;

                // config param: transitclock.prediction.data.kalman.tresholdForDifferenceEventLog
                // This is the threshold in milliseconds that the difference has to be over before it will consider the percentage difference.
                private Integer thresholdForDifferenceEventLog = 60000;

                // config param: transitclock.prediction.kalman.useaverage
                // Will use average travel time as opposed to last historical vehicle in Kalman prediction calculation.
                private Boolean useaverage = true;

            }

            @lombok.Data
            public static class Average {
                // config param: transitclock.prediction.data.average.mindays
                // Min number of days trip data that needs to be available before historical average prediciton is used instead of default transiTime prediction.
                private Integer mindays = 1;
            }

            private Kalman kalman = new Kalman();
            private Average average = new Average();
        }

        private Data data = new Data();


        // config param: transitclock.schedBasedPreds.pollingRateMsec
        // How frequently to look for blocks that do not have associated vehicle.
        private Integer pollingRateMsec = 240000;

        // config param: transitclock.schedBasedPreds.processImmediatelyAtStartup
        // Whether should start creating schedule based predictions right at startup. Usually want to give AVL data a polling cycle to generate AVL based predictions so the default is false. But for a purely schedule based system want to set this to true so get the predictions immediately.
        private Boolean processImmediatelyAtStartup = false;

        // config param: transitclock.schedBasedPreds.beforeStartTimeMinutes
        // How many minutes before a block start time should create a schedule based vehicle for that block.
        private Integer beforeStartTimeMinutes = 60;

        // config param: transitclock.schedBasedPreds.afterStartTimeMinutes
        // If predictions created for a block based on the schedule will remove those predictions this specified number of minutes after the block start time. If using schedule based predictions to provide predictions for even when there is no GPS feed then this can be disabled by using a negative value. But if using schedule based predictions until a GPS based vehicle is matched then want the schedule based predictions to be available until it is clear that no GPS based vehicle is in service. This is especially important when using the automatic assignment method because it can take a few minutes.
        private Integer afterStartTimeMinutes = 8;

        // config param: transitclock.schedBasedPreds.cancelTripOnTimeout
        // Whether should mark a ScheduleBasePred as canceled.This won't remove a trip after afterStartTimeMinutes. Instead it will change the state to cancelled.
        private Boolean cancelTripOnTimeout = true;

        @lombok.Data
        public static class Rls {
            // config param: transitclock.prediction.rls.maxDwellTimeAllowedInModel
            // Max dwell time to be considered in dwell RLS algotithm.
            private Long maxDwellTimeAllowedInModel = 120000L;

            // config param: transitclock.prediction.rls.minDwellTimeAllowedInModel
            // Min dwell time to be considered in dwell RLS algotithm.
            private Long minDwellTimeAllowedInModel = 1000L;

            // config param: transitclock.prediction.rls.maxHeadwayAllowedInModel
            // Max headway to be considered in dwell RLS algotithm.
            private Long maxHeadwayAllowedInModel = 3600000L;

            // config param: transitclock.prediction.rls.minHeadwayAllowedInModel
            // Min headway to be considered in dwell RLS algotithm.
            private Long minHeadwayAllowedInModel = 1000L;

            // config param: transitclock.prediction.rls.minSceheduleAdherence
            // If schedule adherence of vehicle is outside this then not considerd in dwell RLS algorithm.
            private Integer minSceheduleAdherence = 600;

            // config param: transitclock.prediction.rls.maxSceheduleAdherence
            // If schedule adherence of vehicle is outside this then not considerd in dwell RLS algorithm.
            private Integer maxSceheduleAdherence = 600;

            // config param: transitclock.prediction.rls.lambda
            // This sets the rate at which the RLS algorithm forgets old values. Value are between 0 and 1. With 0 being the most forgetful.
            private Double lambda = 0.75;
        }

        private Rls rls = new Rls();

        @lombok.Data
        public static class Travel {
            // config param: transitclock.prediction.travel.maxTravelTimeAllowedInModel
            // Max travel time to be considered in algorithm. Milliseconds.
            private Long maxTravelTimeAllowedInModel = 1200000L;

            // config param: transitclock.prediction.travel.minTravelTimeAllowedInModel
            // Min travel time to be considered in algorithm. Milliseconds.
            private Long minTravelTimeAllowedInModel = 1000L;
        }

        private Travel travel = new Travel();


        @lombok.Data
        public static class Dwell {

            @lombok.Data
            public static class Average {

                // config param: transitclock.prediction.dwell.average.samplesize
                // Max number of samples to keep for mean calculation.
                private Integer samplesize = 5;

                // config param: transitclock.prediction.dwell.average.fractionlimit
                // For when determining stop times. Throws out outliers if they are less than 0.7 or greater than 1/0.7 of the average.
                private Double fractionlimit = 0.7;
            }


            private Average average = new Average();
        }

        private Dwell dwell = new Dwell();
    }

    private Prediction prediction = new Prediction();


    @Data
    public static class Service {
        // config param: transitclock.service.minutesIntoMorningToIncludePreviousServiceIds
        // Early in the morning also want to include at service IDs for previous day since a block might have started on that day. But don't want to always include previous day service IDs since that confuses things. Therefore just include them if before this time of the day, in minutes.
        private Integer minutesIntoMorningToIncludePreviousServiceIds = 240;

    }

    private Service service =new Service();

    @Data
    public static class Timeout {
        // config param: transitclock.timeout.pollingRateSecs
        // Specifies in seconds how frequently the TimeoutHandler should actually look for timeouts. Don't want to do this too frequently because because TimeoutHandler.handlePossibleTimeout() is called with every new AVL report and it has to look at every vehicle to see if has been timed out.
        private Integer pollingRateSecs = 30;

        // config param: transitclock.timeout.allowableNoAvlSecs
        // For AVL timeouts. If don't get an AVL report for the vehicle in this amount of time in seconds then the vehicle will be made non-predictable.
        private Integer allowableNoAvlSecs = 360;

        // config param: transitclock.timeout.allowableNoAvlAfterSchedDepartSecs
        // If a vehicle is at a wait stop, such as sitting at a terminal, and doesn't provide an AVL report for this number of seconds then the vehicle is made unpredictable. Important because sometimes vehicles don't report AVL at terminals because they are powered down. But don't want to continue to provide predictions for long after scheduled departure time if vehicle taken out of service.
        private Integer allowableNoAvlAfterSchedDepartSecs = 360;

        // config param: transitclock.timeout.removeTimedOutVehiclesFromVehicleDataCache
        // When timing out vehicles, the default behavior is to make the vehicle unpredictable but leave it in the VehicleDataCache. When set to true, a timeout will also remove the vehicle from the VehicleDataCache. This can be useful in situations where it is not desirable to include timed out vehicles in data feeds, e.g. the GTFS Realtime vehicle positions feed.
        private Boolean removeTimedOutVehiclesFromVehicleDataCache = false;

    }

    private Timeout timeout = new Timeout();

    @Data
    public static class TravelTimes {
        // config param: transitclock.traveltimes.fractionLimitForStopTimes
        // For when determining stop times. Throws out outliers.
        private Double fractionLimitForStopTimes = 0.7;

        // config param: transitclock.traveltimes.fractionLimitForTravelTimes
        // For when determining travel times. Throws out outliers.
        private Double fractionLimitForTravelTimes = 0.7;

        // config param: transitclock.travelTimes.resetEarlyTerminalDepartures
        // For some agencies vehicles won't be departing terminal early. If an early departure is detected for such an agency then will use the schedule time since the arrival time is likely a mistake.
        private Boolean resetEarlyTerminalDepartures = true;

        // config param: transitclock.traveltimes.maxTravelTimeSegmentLength
        // The longest a travel time segment can be. If a stop path is longer than this distance then it will be divided into multiple travel time segments of even length.
        private Double maxTravelTimeSegmentLength = 250.0;

        // config param: transitclock.traveltimes.minSegmentSpeedMps
        // If a travel time segment is determined to have a lower speed than this value in meters/sec then the travel time will be increased to meet this limit. Purpose is to make sure that don't get invalid travel times due to bad data.
        private Double minSegmentSpeedMps = 0.0;

        // config param: transitclock.traveltimes.maxSegmentSpeedMps
        // If a travel time segment is determined to have a higher speed than this value in meters/second then the travel time will be decreased to meet this limit. Purpose is to make sure that don't get invalid travel times due to bad data.
        private Double maxSegmentSpeedMps = 27.0;

    }

    private TravelTimes travelTimes = new TravelTimes();

    @Data
    public static class TripDataCache {
        // config param: transitclock.tripdatacache.tripDataCacheMaxAgeSec
        // How old an arrivaldeparture has to be before it is removed from the cache
        private Integer tripDataCacheMaxAgeSec = 1296000;
    }
    private TripDataCache tripDataCache = new TripDataCache();

    @Data
    public static class Updates {
        // config param: transitclock.updates.pageDbReads
        // page database reads to break up long reads. It may impact performance on MySql
        private Boolean pageDbReads = true;

        // config param: transitclock.updates.pageSize
        // Number of records to read in at a time
        private Integer pageSize = 50000;

    }

    private Updates updates = new Updates();

    @Data
    public static class Web {
        // config param: transitclock.web.mapTileUrl
        // Specifies the URL used by Leaflet maps to fetch map tiles.
        private String mapTileUrl = "http://otile4.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";

        // config param: transitclock.web.mapTileCopyright
        // For displaying as map attributing for the where map tiles from.
        private String mapTileCopyright = "MapQuest";

    }

    private Web web = new Web();
}
