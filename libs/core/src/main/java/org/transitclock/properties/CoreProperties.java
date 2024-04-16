package org.transitclock.properties;

import lombok.Data;

@Data
public class CoreProperties {
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

            private PredictionGenerator.Bias.Exponential exponential = new PredictionGenerator.Bias.Exponential();

            @Data
            public static class Linear {
                // config param: org.transitclock.core.predictiongenerator.bias.linear.rate
                // Rate at which percentage adjustment changes with horizon.
                private Double rate = 6.0E-4;

                // config param: org.transitclock.core.predictiongenerator.bias.linear.updown
                // Is the adjustment up or down? Set +1 or -1.
                private Integer updown = -1;
            }

            private PredictionGenerator.Bias.Linear linear = new PredictionGenerator.Bias.Linear();
        }

        private PredictionGenerator.Bias bias = new PredictionGenerator.Bias();
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
