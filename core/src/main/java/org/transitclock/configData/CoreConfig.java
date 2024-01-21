/* (C)2023 */
package org.transitclock.configData;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.config.*;
import org.transitclock.utils.Time;

/**
 * Handles the core configuration data file. Allows parameters to be read in from the file at
 * startup and, in the future, while program is running. Goal is for the accessing of the data to be
 * fast so don't want to do any work then. But also need to make sure that it is thread safe since
 * might be reading data while it is being updated.
 *
 * @author SkiBu Smith
 */
public class CoreConfig {

    private static final StringConfigValue timezone = new StringConfigValue(
            "transitclock.core.timezone",
            "For setting timezone for application. Ideally would get "
                    + "timezone from the agency db but once a Hibernate "
                    + "session factory is created, such as for reading "
                    + "timezone from db, then it is too late to set the "
                    + "timezone. Therefore this provides ability to set it "
                    + "manually.");
    public static String getTimezone() {
        return timezone.getValue();
    }

    // Database params
    /**
     * How many days data to look back at to fill cache
     *
     * @return
     */
    public static int getDaysPopulateHistoricalCache() {
        return daysPopulateHistoricalCache.getValue();
    }

    public static IntegerConfigValue daysPopulateHistoricalCache = new IntegerConfigValue(
            "transitclock.cache.core.daysPopulateHistoricalCache",
            0,
            "How many days data to read in to populate historical cache on start up.");

    /**
     * When in playback mode or some other situations don't want to store generated data such as
     * arrivals/departures, events, and such to the database because only debugging.
     *
     * @return
     */
    public static boolean storeDataInDatabase() {
        return storeDataInDatabase.getValue();
    }

    public static BooleanConfigValue storeDataInDatabase = new BooleanConfigValue(
            "transitclock.db.storeDataInDatabase",
            true,
            "When in playback mode or some other situations don't "
                    + "want to store generated data such as arrivals/"
                    + "departures, events, and such to the database because "
                    + "only debugging.");

    /**
     * When batching large amount of AVL data through system to generate improved schedule time (as
     * has been done for Zhengzhou) it takes huge amount of time to process everything. To speed
     * things up you can set -Dtransitclock.core.onlyNeedArrivalDepartures=true such that the system
     * will be sped up by not generating nor logging predictions, not logging AVL data nor storing
     * it in db, and not logging nor storing match data in db.
     *
     * @return
     */
    public static boolean onlyNeedArrivalDepartures() {
        return onlyNeedArrivalDepartures.getValue();
    }

    private static BooleanConfigValue onlyNeedArrivalDepartures = new BooleanConfigValue(
            "transitclock.core.onlyNeedArrivalDepartures",
            false,
            "When batching large amount of AVL data through system "
                    + "to generate improved schedule time (as has been done "
                    + "for Zhengzhou) it takes huge amount of time to process "
                    + "everything. To speed things up you can set "
                    + "-Dtransitclock.core.onlyNeedArrivalDepartures=true such "
                    + "that the system will be sped up by not generating nor "
                    + "logging predictions, not logging AVL data nor storing "
                    + "it in db, and not logging nor storing match data in db.");

    /**
     * When in batch mode can flood db with lots of objects. If
     * transitclock.core.pauseIfDbQueueFilling is set to true then when objects are put into the
     * DataDbLogger queue the calling thread will be temporarily suspended so that the separate
     * thread can run to write to the db and thereby empty out the queue.
     *
     * @return
     */
    public static boolean pauseIfDbQueueFilling() {
        return pauseIfDbQueueFilling.getValue();
    }

    private static BooleanConfigValue pauseIfDbQueueFilling = new BooleanConfigValue(
            "transitclock.core.pauseIfDbQueueFilling",
            false,
            "When in batch mode can flood db with lots of objects. If"
                    + "transitclock.core.pauseIfDbQueueFilling is set to true "
                    + "then when objects are put into the DataDbLogger queue "
                    + "the calling thread will be temporarily suspended so "
                    + "that the separate thread can run to write to the db and "
                    + "thereby empty out the queue.");

    /**
     * The semicolon separated list of names of all the modules that should be automatically
     * started.
     */
    public static List<Class> getOptionalModules() {
        return optionalModules.getValue();
    }


    private static final ClassListConfigValue optionalModules = new ClassListConfigValue(
            "transitclock.modules.optionalModulesList",
            new ArrayList<>(),
            "The semicolon separated list of names of all of the modules that should be automatically started.");

    /**
     * How far a location can be from a path segment and still be considered a match.
     */
    public static double getMaxDistanceFromSegment() {
        return maxDistanceFromSegment.getValue();
    }

    private static DoubleConfigValue maxDistanceFromSegment = new DoubleConfigValue(
            "transitclock.core.maxDistanceFromSegment",
            60.0,
            "How far a location can be from a path segment and still "
                    + "be considered a match. Can be overridden on a per route "
                    + "basis via max_distance supplemental column of route GTFS "
                    + "data. When auto assigning, the parameter "
                    + "transitclock.core.maxDistanceFromSegmentForAutoAssigning "
                    + "is used instead.");

    /**
     * How far a location can be from a path segment and still be considered a match when auto
     * assigning.
     *
     * @return
     */
    public static double getMaxDistanceFromSegmentForAutoAssigning() {
        return maxDistanceFromSegmentForAutoAssigning.getValue();
    }

    private static DoubleConfigValue maxDistanceFromSegmentForAutoAssigning = new DoubleConfigValue(
            "transitclock.core.maxDistanceFromSegmentForAutoAssigning",
            60.0,
            "How far a location can be from a path segment and still "
                    + "be considered a match when auto assigning a vehicle. Auto "
                    + "assigning is treated separately because sometimes need "
                    + "to make maxDistanceFromSegment really lenient because "
                    + "vehicles do not always follow same route. This is "
                    + "especially true with deviated fixed route service. But "
                    + "don't want the distance to be too lenient when auto "
                    + "assigning because if too lenient then would match to "
                    + "multiple routes and therefore no assignment would be "
                    + "made because the matches would be ambiguous. Therefore "
                    + "maxDistanceFromSegmentForAutoAssigning should be less"
                    + "than or equal to maxDistanceFromSegment.");

    /**
     * How many bad spatial/temporal matches a predictable vehicle can have in a row before the
     * vehicle is made unpredictable.
     *
     * @return
     */
    public static int getAllowableNumberOfBadMatches() {
        return allowableNumberOfBadMatches.getValue();
    }

    private static IntegerConfigValue allowableNumberOfBadMatches = new IntegerConfigValue(
            "transitclock.core.allowableNumberOfBadMatches",
            2,
            "How many bad spatial/temporal matches a predictable "
                    + "vehicle can have in a row before the vehicle is made "
                    + "unpredictable.");

    /**
     * How far heading in degrees of vehicle can be away from path segment and still be considered a
     * match. Needs to be pretty lenient because stopPaths and heading might not be that accurate.
     *
     * @return
     */
    public static float getMaxHeadingOffsetFromSegment() {
        return maxHeadingOffsetFromSegment.getValue();
    }

    private static FloatConfigValue maxHeadingOffsetFromSegment = new FloatConfigValue(
            "transitclock.core.maxHeadingOffsetFromSegment",
            360.0f,
            "How far heading in degrees of vehicle can be away from "
                    + "path segment and still be considered a match. Needs to "
                    + "be pretty lenient because stopPaths and heading might "
                    + "not be that accurate.");

    /**
     * For initial matching of vehicle to block assignment. If vehicle is closer than this distance
     * from the end of the block then the spatial match will not be used. This is to prevent a
     * vehicle that has already completed its block from wrongly being assigned to that block again.
     *
     * @return
     */
    public static double getDistanceFromEndOfBlockForInitialMatching() {
        return distanceFromEndOfBlockForInitialMatching.getValue();
    }

    private static DoubleConfigValue distanceFromEndOfBlockForInitialMatching = new DoubleConfigValue(
            "transitclock.core.distanceFromEndOfBlockForInitialMatching",
            250.0,
            "For initial matching of vehicle to block assignment. If "
                    + "vehicle is closer than this distance from the end of "
                    + "the block then the spatial match will not be used. This "
                    + "is to prevent a vehicle that has already completed its "
                    + "block from wrongly being assigned to that block again.");

    /**
     * How close vehicle needs to be from the last stop of the block such that the next AVL report
     * should possibly be considered to match to the end of the block. This is important for
     * determining the arrival time at the last stop of the block even if don't get an AVL report
     * near that stop.
     *
     * @return
     */
    public static double getDistanceFromLastStopForEndMatching() {
        return distanceFromLastStopForEndMatching.getValue();
    }

    private static DoubleConfigValue distanceFromLastStopForEndMatching = new DoubleConfigValue(
            "transitclock.core.distanceFromLastStopForEndMatching",
            250.0,
            "How close vehicle needs to be from the last stop of the "
                    + "block such that the next AVL report should possibly be "
                    + "considered to match to the end of the block. This is "
                    + "important for determining the arrival time at the last"
                    + "stop of the block even if don't get an AVL report near "
                    + "that stop.");

    /**
     * For determining if enough time to deadhead to beginning of a trip. If vehicles are far away
     * then they are more likely to be able to travel faster because they could take a freeway or
     * other fast road. But when get closer then will be on regular streets and will travel more
     * slowly. The parameters should be set in a conservative way such that the travel time is
     * underestimated by using slower speeds than will actually encounter. This way the vehicle will
     * arrive after the predicted time which means that passenger won't miss the bus.
     *
     * @return
     */
    public static float getDeadheadingShortVersusLongDistance() {
        return deadheadingShortVersusLongDistance.getValue();
    }

    private static FloatConfigValue deadheadingShortVersusLongDistance = new FloatConfigValue(
            "transitclock.core.deadheadingShortVersusLongDistance",
            1000.0f,
            "For determining if enough time to deadhead to beginning "
                    + "of a trip. If vehicles are far away then they are more "
                    + "likely to be able to travel faster because they could "
                    + "take a freeway or other fast road. But when get closer "
                    + "then will be on regular streets and will travel more "
                    + "slowly. The parameters should be set in a conservative "
                    + "way such that the travel time is underestimated by "
                    + "using slower speeds than will actually encounter. This "
                    + "way the vehicle will arrive after the predicted time "
                    + "which means that passenger won't miss the bus.");

    public static float getShortDistanceDeadheadingSpeed() {
        return shortDistanceDeadheadingSpeed.getValue();
    }

    private static FloatConfigValue shortDistanceDeadheadingSpeed = new FloatConfigValue(
            "transitclock.core.shortDistanceDeadheadingSpeed",
            4.0f, // 4.0m/s is about 8 mph
            "Part of determining if enough time to deadhead to layover.");

    public static float getLongDistanceDeadheadingSpeed() {
        return longDistanceDeadheadingSpeed.getValue();
    }

    private static FloatConfigValue longDistanceDeadheadingSpeed = new FloatConfigValue(
            "transitclock.core.longDistanceDeadheadingSpeed",
            10.0f, // 10.0m/s is about 20mph
            "Part of determining if enough time to deadhead to layover.");

    /** ************************************************************************* For predictions */

    /**
     * For determining if prediction should be stored in db. Set to 0 to never store predictions. A
     * very large number of predictions is created so be careful with this value so that the db
     * doesn't get filled up too quickly.
     *
     * @return
     */
    public static int getMaxPredictionsTimeForDbSecs() {
        return maxPredictionsTimeForDbSecs.getValue();
    }

    private static IntegerConfigValue maxPredictionsTimeForDbSecs = new IntegerConfigValue(
            "transitclock.core.maxPredictionTimeForDbSecs",
            30 * Time.SEC_PER_MIN,
            "For determining if prediction should be stored in db. "
                    + "Set to 0 to never store predictions. A very large "
                    + "number of predictions is created so be careful with "
                    + "this value so that the db doesn't get filled up too "
                    + "quickly.");

    /**
     * How early a vehicle can be and still be matched to a layover. Needs to be pretty large
     * because sometimes vehicles will be assigned to a layover quite early, and want to be able to
     * make the vehicle predictable and generate predictions far in advance. Don't want it to be too
     * large, like 90 minutes, though because then could match incorrectly if vehicle simply stays
     * at terminal.
     *
     * @return
     */
    public static int getAllowableEarlyForLayoverSeconds() {
        return allowableEarlyForLayoverSeconds.getValue();
    }

    private static IntegerConfigValue allowableEarlyForLayoverSeconds = new IntegerConfigValue(
            "transitclock.core.allowableEarlyForLayoverSeconds",
            60 * Time.SEC_PER_MIN,
            "How early a vehicle can be and still be matched to a "
                    + "layover. Needs to be pretty large because sometimes "
                    + "vehicles will be assigned to a layover quite early, "
                    + "and want to be able to make the vehicle predictable and "
                    + "generate predictions far in advance. Don't want it to "
                    + "be too large, like 90 minutes, though because then "
                    + "could match incorrectly if vehicle simply stays at "
                    + "terminal.");

    /**
     * How early a vehicle can be and still be matched to its block assignment. If when a new AVL
     * report is received for a predictable vehicle and it is found with respect to the real-time
     * schedule adherence to be earlier than this value the vehicle will be made unpredictable.
     *
     * @return
     */
    public static int getAllowableEarlySeconds() {
        return allowableEarlySeconds.getValue();
    }

    public static String getAllowableEarlySecondsId() {
        return allowableEarlySeconds.getID();
    }

    private static IntegerConfigValue allowableEarlySeconds = new IntegerConfigValue(
            "transitclock.core.allowableEarlySeconds",
            15 * Time.SEC_PER_MIN,
            "How early a vehicle can be and still be matched to its "
                    + "block assignment. If when a new AVL report is received "
                    + "for a predictable vehicle and it is found with respect "
                    + "to the real-time schedule adherence to be earlier than "
                    + "this value the vehicle will be made unpredictable.");

    /**
     * How late a vehicle can be and still be matched to its block assignment. If when a new AVL
     * report is received for a predictable vehicle and it is found with respect to the real-time
     * schedule adherence to be later than this value the vehicle will be made unpredictable.
     *
     * @return
     */
    public static int getAllowableLateSeconds() {
        return allowableLateSeconds.getValue();
    }

    public static String getAllowableLateSecondsId() {
        return allowableLateSeconds.getID();
    }

    private static IntegerConfigValue allowableLateSeconds = new IntegerConfigValue(
            "transitclock.core.allowableLateSeconds",
            90 * Time.SEC_PER_MIN,
            "How late a vehicle can be and still be matched to its "
                    + "block assignment. If when a new AVL report is received "
                    + "for a predictable vehicle and it is found with respect "
                    + "to the real-time schedule adherence to be later than "
                    + "this value the vehicle will be made unpredictable.");

    /**
     * How early a vehicle can be and still be matched to its block assignment.
     *
     * @return time in seconds
     */
    public static int getAllowableEarlySecondsForInitialMatching() {
        return allowableEarlySecondsForInitialMatching.getValue();
    }

    private static IntegerConfigValue allowableEarlySecondsForInitialMatching = new IntegerConfigValue(
            "transitclock.core.allowableEarlySecondsForInitialMatching",
            10 * Time.SEC_PER_MIN,
            "How early a vehicle can be in seconds and still be " + "matched to its block assignment.");

    /**
     * How late a vehicle can be and still be matched to its block assignment.
     *
     * @return time in seconds
     */
    public static int getAllowableLateSecondsForInitialMatching() {
        return allowableLateSecondsForInitialMatching.getValue();
    }

    private static IntegerConfigValue allowableLateSecondsForInitialMatching = new IntegerConfigValue(
            "transitclock.core.allowableLateSecondsForInitialMatching",
            20 * Time.SEC_PER_MIN,
            "How late a vehicle can be in seconds and still be " + "matched to its block assignment.");

    /**
     * For initial matching vehicle to assignment when there isn't any heading information. In that
     * case also want to match to previous AVL report. This parameter specifies how far, as the crow
     * flies, the previous AVL report to be used from the VehicleState AvlReport history is from the
     * current AvlReport.
     *
     * @return
     */
    public static double getDistanceBetweenAvlsForInitialMatchingWithoutHeading() {
        return distanceBetweenAvlsForInitialMatchingWithoutHeading.getValue();
    }

    private static DoubleConfigValue distanceBetweenAvlsForInitialMatchingWithoutHeading = new DoubleConfigValue(
            "transitclock.core.distanceBetweenAvlsForInitialMatchingWithoutHeading",
            100.0,
            "For initial matching vehicle to assignment when there "
                    + "isn't any heading information. In that case also want "
                    + "to match to previous AVL report. This parameter "
                    + "specifies how far, as the crow flies, the previous AVL "
                    + "report to be used from the VehicleState AvlReport "
                    + "history is from the current AvlReport.");

    /**
     * How far along path past a layover stop a vehicle needs to be in order for it to be considered
     * an early departure instead of just moving around within the layover. Needs to be a decent
     * distance since the stop locations are not always accurate.
     *
     * <p>Related to getAllowableEarlyTimeForEarlyDeparture().
     *
     * @return
     */
    public static double getDistanceFromLayoverForEarlyDeparture() {
        return distanceFromLayoverForEarlyDeparture.getValue();
    }

    private static DoubleConfigValue distanceFromLayoverForEarlyDeparture = new DoubleConfigValue(
            "transitclock.core.distanceFromLayoverForEarlyDeparture",
            180.0,
            "How far along path past a layover stop a vehicle needs "
                    + "to be in order for it to be considered an early "
                    + "departure instead of just moving around within the "
                    + "layover. Needs to be a decent distance since the stop "
                    + "locations are not always accurate.");

    /**
     * How far vehicle can be away from layover stop and still match to it. For when not deadheading
     * to a trip. This can be useful to determine when a vehicle is pulled out of service when it is
     * expected to be at a layover and start the next trip. Should usually be a pretty large value
     * so that vehicles are not taken out of service just because they drive a bit away from the
     * stop for the layover, which is pretty common.
     *
     * @return
     */
    public static double getLayoverDistance() {
        return layoverDistance.getValue();
    }

    private static DoubleConfigValue layoverDistance = new DoubleConfigValue(
            "transitclock.core.layoverDistance",
            2000.0,
            "How far vehicle can be away from layover stop and still "
                    + "match to it. For when not deadheading to a trip. This "
                    + "can be useful to determine when a vehicle is pulled out "
                    + "of service when it is expected to be at a layover and "
                    + "start the next trip. Should usually be a pretty large "
                    + "value so that vehicles are not taken out of service "
                    + "just because they drive a bit away from the stop for "
                    + "the layover, which is pretty common.");

    /**
     * How early in seconds a vehicle can have left terminal and have it be considered an early
     * departure instead of just moving around within the layover. Don't want to mistakingly think
     * that vehicles moving around during layover have started their trip early. Therefore this
     * value should be limited to just a few minutes since vehicles usually don't leave early.
     *
     * <p>Related to getDistanceFromLayoverForEarlyDeparture()
     *
     * @return Time in msec
     */
    public static int getAllowableEarlyTimeForEarlyDepartureSecs() {
        return allowableEarlyTimeForEarlyDepartureSecs.getValue();
    }

    private static IntegerConfigValue allowableEarlyTimeForEarlyDepartureSecs = new IntegerConfigValue(
            "transitclock.core.allowableEarlyTimeForEarlyDepartureSecs",
            5 * Time.SEC_PER_MIN,
            "How early in seconds a vehicle can have left terminal and have it be"
                    + " considered an early departure instead of just moving around within the"
                    + " layover. Don't want to mistakingly think that vehicles moving around"
                    + " during layover have started their trip early. Therefore this value"
                    + " should be limited to just a few minutes since vehicles usually don't"
                    + " leave early.");

    /**
     * How early in seconds a vehicle can departure a terminal before it registers a VehicleEvent
     * indicating a problem.
     *
     * @return
     */
    public static int getAllowableEarlyDepartureTimeForLoggingEvent() {
        return allowableEarlyDepartureTimeForLoggingEvent.getValue();
    }

    private static IntegerConfigValue allowableEarlyDepartureTimeForLoggingEvent = new IntegerConfigValue(
            "transitclock.core.allowableEarlyDepartureTimeForLoggingEvent",
            60,
            "How early in seconds a vehicle can depart a terminal "
                    + "before it registers a VehicleEvent indicating a problem.");

    /**
     * How late in seconds a vehicle can departure a terminal before it registers a VehicleEvent
     * indicating a problem.
     *
     * @return
     */
    public static int getAllowableLateDepartureTimeForLoggingEvent() {
        return allowableLateDepartureTimeForLoggingEvent.getValue();
    }

    private static IntegerConfigValue allowableLateDepartureTimeForLoggingEvent = new IntegerConfigValue(
            "transitclock.core.allowableLateDepartureTimeForLoggingEvent",
            4 * Time.SEC_PER_MIN,
            "How late in seconds a vehicle can depart a terminal "
                    + "before it registers a VehicleEvent indicating a problem.");

    /**
     * If a vehicle is just sitting at a terminal and provides another GPS report indicating that it
     * is more than this much later, in seconds, than the configured departure time then a
     * VehicleEvent is created to record the problem.
     *
     * @return
     */
    public static int getAllowableLateAtTerminalForLoggingEvent() {
        return allowableLateAtTerminalForLoggingEvent.getValue();
    }

    private static IntegerConfigValue allowableLateAtTerminalForLoggingEvent = new IntegerConfigValue(
            "transitclock.core.allowableLateAtTerminalForLoggingEvent",
            1 * Time.SEC_PER_MIN,
            "If a vehicle is sitting at a terminal and provides "
                    + "another GPS report indicating that it is more than this "
                    + "much later, in seconds, than the configured departure "
                    + "time then a VehicleEvent is created to record the "
                    + "problem.");

    /**
     * How far a vehicle can be before a stop in meters and be considered to have arrived.
     *
     * @return
     */
    public static double getBeforeStopDistance() {
        return beforeStopDistance.getValue();
    }

    private static DoubleConfigValue beforeStopDistance = new DoubleConfigValue(
            "transitclock.core.beforeStopDistance",
            50.0,
            "How far a vehicle can be ahead of a stop in meters and " + "be considered to have arrived.");

    /**
     * How far a vehicle can be past a stop in meters and still be considered at the stop.
     *
     * @return
     */
    public static double getAfterStopDistance() {
        return afterStopDistance.getValue();
    }

    private static DoubleConfigValue afterStopDistance = new DoubleConfigValue(
            "transitclock.core.afterStopDistance",
            50.0,
            "How far a vehicle can be past a stop in meters and " + "still be considered at the stop.");

    /**
     * Returns how long driver is expected to have a break for a stop.
     *
     * @return
     */
    public static int getDefaultBreakTimeSec() {
        return defaultBreakTimeSec.getValue();
    }

    private static IntegerConfigValue defaultBreakTimeSec = new IntegerConfigValue(
            "transitclock.core.defaultBreakTimeSec", 0, "How long driver is expected to have a break for a stop.");

    /**
     * How much worse it is for a vehicle to be early as opposed to late when determining schedule
     * adherence.
     *
     * @return
     */
    public static double getEarlyToLateRatio() {
        return earlyToLateRatio.getValue();
    }

    private static DoubleConfigValue earlyToLateRatio = new DoubleConfigValue(
            "transitclock.core.earlyToLateRatio",
            3.0,
            "How much worse it is for a vehicle to be early as "
                    + "opposed to late when determining schedule adherence.");

    /**
     * True if block assignments should be exclusive. If set to true then when a vehicle is assigned
     * to a block the system will unassign any other vehicles that were assigned to the block.
     * Important for when AVL system doesn't always provide logout info.
     *
     * @return
     */
    public static boolean exclusiveBlockAssignments() {
        return exclusiveBlockAssignments.getValue();
    }

    private static BooleanConfigValue exclusiveBlockAssignments = new BooleanConfigValue(
            "transitclock.core.exclusiveBlockAssignments",
            true,
            "True if block assignments should be exclusive. If set to "
                    + "true then when a vehicle is assigned to a block the "
                    + "system will unassign any other vehicles that were "
                    + "assigned to the block. Important for when AVL system "
                    + "doesn't always provide logout info. Also used by the "
                    + "AutoBlockAssigner to determine which active blocks to "
                    + "possibly assign a vehicle to. For no schedule routes "
                    + "can set to false to allow multiple vehicles be assigned "
                    + "to a route.");

    public static int getTimeForDeterminingNoProgress() {
        return timeForDeterminingNoProgress.getValue();
    }

    private static IntegerConfigValue timeForDeterminingNoProgress = new IntegerConfigValue(
            "transitclock.core.timeForDeterminingNoProgress",
            8 * Time.MS_PER_MIN,
            "The interval in msec at which look at vehicle's history "
                    + "to determine if it is not making any progress. A value"
                    + "of 0 disables this feature. If "
                    + "vehicle is found to not be making progress it is "
                    + "made unpredictable.");

    public static double getMinDistanceForNoProgress() {
        return minDistanceForNoProgress.getValue();
    }

    private static DoubleConfigValue minDistanceForNoProgress = new DoubleConfigValue(
            "transitclock.core.minDistanceForNoProgress",
            60.0,
            "Minimum distance vehicle is expected to travel over "
                    + "timeForDeterminingNoProgress to indicate vehicle is "
                    + "making progress. If "
                    + "vehicle is found to not be making progress it is "
                    + "made unpredictable.");

    /**
     * transitclock.core.timeForDeterminingDelayedSecs
     *
     * @return
     */
    public static int getTimeForDeterminingDelayedSecs() {
        return timeForDeterminingDelayedSecs.getValue();
    }

    private static IntegerConfigValue timeForDeterminingDelayedSecs = new IntegerConfigValue(
            "transitclock.core.timeForDeterminingDelayedSecs",
            4 * Time.SEC_PER_MIN,
            "The interval in msec at which look at vehicle's history " + "to determine if it is delayed.");

    /**
     * transitclock.core.minDistanceForDelayed
     *
     * @return
     */
    public static double getMinDistanceForDelayed() {
        return minDistanceForDelayed.getValue();
    }

    private static DoubleConfigValue minDistanceForDelayed = new DoubleConfigValue(
            "transitclock.core.minDistanceForDelayed",
            60.0,
            "Minimum distance vehicle is expected to travel over "
                    + "timeForDeterminingDelayed to indicate vehicle is "
                    + "delayed.");

    public static int getMatchHistoryMaxSize() {
        return matchHistoryMaxSize.getValue();
    }

    private static IntegerConfigValue matchHistoryMaxSize = new IntegerConfigValue(
            "transitclock.core.matchHistoryMaxSize",
            20,
            "How many matches are kept in history for vehicle so that "
                    + "can can do things such as look back at history to "
                    + "determine if vehicle has broken down. Should be large "
                    + "enough so can store all matchets generated over "
                    + "timeForDeterminingNoProgress. If GPS rate is high then "
                    + "this value will need to be high as well.");

    public static int getAvlHistoryMaxSize() {
        return avlHistoryMaxSize.getValue();
    }

    private static IntegerConfigValue avlHistoryMaxSize = new IntegerConfigValue(
            "transitclock.core.avlHistoryMaxSize",
            20,
            "How many AVL reports are kept in history for vehicle so "
                    + "that can can do things such as look back at history to "
                    + "determine if vehicle has broken down. Should be large "
                    + "enough so can store all AVL reports received over "
                    + "timeForDeterminingNoProgress. If GPS rate is high then "
                    + "this value will need to be high as well.");

    private static IntegerConfigValue eventHistoryMaxSize = new IntegerConfigValue(
            "transitclock.core.eventHistoryMaxSize",
            20,
            "How many arrival depature event reports are kept in history for vehicle so "
                    + "that can can do things such as look back at history");

    public static int getEventHistoryMaxSize() {
        return eventHistoryMaxSize.getValue();
    }

    public static String getPidFileDirectory() {
        return pidFileDirectory.getValue();
    }

    private static StringConfigValue pidFileDirectory = new StringConfigValue(
            "transitclock.core.pidDirectory",
            "/usr/local/transitclock/",
            "Directory where pid file should be written. The pid file "
                    + "can be used by monit to make sure that core process is "
                    + "always running.");



    //// prediction generator
    public static final IntegerConfigValue maxPredictionsTimeSecs = new IntegerConfigValue(
            "transitclock.core.maxPredictionsTimeSecs",
            30 * Time.SEC_PER_MIN,
            "How far forward into the future should generate predictions for.");

    public static LongConfigValue generateHoldingTimeWhenPredictionWithin = new LongConfigValue(
            "transitclock.core.generateHoldingTimeWhenPredictionWithin",
            0L,
            "If the prediction is less than this number of milliseconds from current time"
                    + " then use it to generate a holding time");

    public static BooleanConfigValue useArrivalPredictionsForNormalStops = new BooleanConfigValue(
            "transitclock.core.useArrivalPredictionsForNormalStops",
            true,
            "For specifying whether to use arrival predictions or "
                    + "departure predictions for normal, non-wait time, stops.");

    public static IntegerConfigValue maxLateCutoffPredsForNextTripsSecs = new IntegerConfigValue(
            "transitclock.core.maxLateCutoffPredsForNextTripsSecs",
            Integer.MAX_VALUE,
            "If a vehicle is further behind schedule than this amount "
                    + "then predictions for subsequent trips will be marked as "
                    + "being uncertain. This is useful for when another vehicle "
                    + "might take over the next trip for the block due to "
                    + "vehicle being late.");

    public static BooleanConfigValue useExactSchedTimeForWaitStops = new BooleanConfigValue(
            "transitclock.core.useExactSchedTimeForWaitStops",
            true,
            "The predicted time for wait stops includes the historic "
                    + "wait stop time. This means it will be a bit after the "
                    + "configured schedule time. But some might not want to "
                    + "see such adjusted times. Plus just showing the schedule "
                    + "time is more conservative, and therefore usually better. "
                    + "If this value is set to true then the actual schedule "
                    + "time will be used. If false then the schedule time plus "
                    + "the wait stop time will be used.");

    public static BooleanConfigValue useHoldingTimeInPrediction =
            new BooleanConfigValue("transitclock.core.useHoldingTimeInPrediction", false, "Add holding time to prediction.");



    public static int getMaxPredictionsTimeSecs() {
        return maxPredictionsTimeSecs.getValue();
    }



    /// avl processor
    public static double getTerminalDistanceForRouteMatching() {
        return terminalDistanceForRouteMatching.getValue();
    }

    public static final DoubleConfigValue terminalDistanceForRouteMatching = new DoubleConfigValue(
            "transitclock.core.terminalDistanceForRouteMatching",
            100.0,
            "How far vehicle must be away from the terminal before doing "
                    + "initial matching. This is important because when vehicle is at "
                    + "terminal don't know which trip it it should be matched to until "
                    + "vehicle has left the terminal.");

    public static final IntegerConfigValue allowableBadAssignments = new IntegerConfigValue(
            "transitclock.core.allowableBadAssignments",
            0,
            "If get a bad assignment, such as no assignment, but no "
                    + "more than allowableBadAssignments then will use the "
                    + "previous assignment. Useful for when assignment part "
                    + "of AVL feed doesn't always provide a valid assignment.");

    public static BooleanConfigValue emailMessagesWhenAssignmentGrabImproper = new BooleanConfigValue(
            "transitclock.core.emailMessagesWhenAssignmentGrabImproper",
            false,
            "When one vehicle gets assigned by AVL feed but another "
                    + "vehicle already has that assignment then sometimes the "
                    + "assignment to the new vehicle would be incorrect. Could "
                    + "be that vehicle was never logged out or simply got bad "
                    + "assignment. For this situation it can be useful to "
                    + "receive error message via e-mail. But can get too many "
                    + "such e-mails. This property allows one to control those "
                    + "e-mails.");

    public static final DoubleConfigValue maxDistanceForAssignmentGrab = new DoubleConfigValue(
            "transitclock.core.maxDistanceForAssignmentGrab",
            10000.0,
            "For when another vehicles gets assignment and needs to "
                    + "grab it from another vehicle. The new vehicle must "
                    + "match to route within maxDistanceForAssignmentGrab in "
                    + "order to grab the assignment.");

    public static final DoubleConfigValue maxMatchDistanceFromAVLRecord = new DoubleConfigValue(
            "transitclock.core.maxMatchDistanceFromAVLRecord",
            500.0,
            "For logging distance between spatial match and actual AVL assignment ");

    public static final BooleanConfigValue ignoreInactiveBlocks = new BooleanConfigValue(
            "transitclock.core.ignoreInactiveBlocks",
            true,
            "If the block isn't active at this time then ignore it. This way "
                    + "don't look at each trip to see if it is active which is important "
                    + "because looking at each trip means all the trip data including "
                    + "travel times needs to be lazy loaded, which can be slow.");

    public static double getMaxMatchDistanceFromAVLRecord() {
        return maxMatchDistanceFromAVLRecord.getValue();
    }


    /// prediction
    public static BooleanConfigValue storeTravelTimeStopPathPredictions = new BooleanConfigValue(
            "transitclock.core.storeTravelTimeStopPathPredictions",
            false,
            "This is set to true to record all travelTime  predictions for individual"
                    + " stopPaths generated. Useful for comparing performance of differant"
                    + " algorithms. (MAPE comparison). Not for normal use as will generate"
                    + " massive amounts of data.");

    public static BooleanConfigValue storeDwellTimeStopPathPredictions = new BooleanConfigValue(
            "transitclock.core.storeDwellTimeStopPathPredictions",
            false,
            "This is set to true to record all travelTime  predictions for individual dwell"
                    + " times generated. Useful for comparing performance of differant"
                    + " algorithms. (MAPE comparison). Not for normal use as will generate"
                    + " massive amounts of data.");

    /// spatial matcher

    public static BooleanConfigValue spatialMatchToLayoversAllowedForAutoAssignment = new BooleanConfigValue(
            "transitclock.core.spatialMatchToLayoversAllowedForAutoAssignment",
            false,
            "Allow auto assigner consider spatial matches to layovers. Experimental.");



    public static BooleanConfigValue blockLoading = new BooleanConfigValue(
            "transitclock.blockLoading.agressive",
            false,
            "Set true to eagerly fetch all blocks into memory on startup");



    /// ExponentialBiasAdjuster
    public static DoubleConfigValue baseNumber = new DoubleConfigValue(
            "org.transitclock.core.predictiongenerator.bias.exponential.b",
            1.1,
            "Base number to be raised to the power of the horizon minutes. y=a(b^x)+c.");

    public static DoubleConfigValue multiple = new DoubleConfigValue(
            "org.transitclock.core.predictiongenerator.bias.exponential.a", 0.5, "Multiple.y=a(b^x)+c.");

    public static DoubleConfigValue constant = new DoubleConfigValue(
            "org.transitclock.core.predictiongenerator.bias.exponential.c", -0.5, "Constant. y=a(b^x)+c.");

    public static IntegerConfigValue updown = new IntegerConfigValue(
            "org.transitclock.core.predictiongenerator.bias.exponential.updown",
            -1,
            "Is the adjustment up or down? Set +1 or -1.");


    public static IntegerConfigValue maxDwellTimeAllowedInModel = new IntegerConfigValue(
            "org.transitclock.core.dataCache.jcs.maxDwellTimeAllowedInModel",
            2 * Time.MS_PER_MIN,
            "Max dwell time to be considered in dwell RLS algotithm.");
    public static LongConfigValue maxHeadwayAllowedInModel = new LongConfigValue(
            "org.transitclock.core.dataCache.jcs.maxHeadwayAllowedInModel",
            1 * Time.MS_PER_HOUR,
            "Max headway to be considered in dwell RLS algotithm.");

    public static DoubleConfigValue lambda = new DoubleConfigValue(
            "org.transitclock.core.dataCache.jcs.lambda",
            0.75,
            "This sets the rate at which the RLS algorithm forgets old values. Value are"
                    + " between 0 and 1. With 0 being the most forgetful.");




    public static final DoubleConfigValue rateChangePercentage = new DoubleConfigValue(
            "org.transitclock.core.predictiongenerator.bias.linear.rate",
            0.0006,
            "Rate at which percentage adjustment changes with horizon.");
    public static final IntegerConfigValue linearUpdown = new IntegerConfigValue(
            "org.transitclock.core.predictiongenerator.bias.linear.updown",
            -1,
            "Is the adjustment up or down? Set +1 or -1.");


    /// blocksinfo
    public static IntegerConfigValue blockactiveForTimeBeforeSecs = new IntegerConfigValue(
            "transitclock.core.blockactiveForTimeBeforeSecs",
            0,
            "Now many seconds before the start of a block it will be considered active.");
    public static IntegerConfigValue blockactiveForTimeAfterSecs = new IntegerConfigValue(
            "transitclock.core.blockactiveForTimeAfterSecs",
            -1,
            "Now many seconds after the end of a block it will be considered active.");


    // dwelltimedetails
    public static final IntegerConfigValue maxDwellTime = new IntegerConfigValue(
            "transitclock.core.maxDwellTime",
            10 * Time.MS_PER_MIN,
            "This is a maximum dwell time at a stop to be taken into account for cache or"
                    + " prediction calculations.");


    /// frequencybasedhistoricalaveragecache
    public static IntegerConfigValue minTravelTimeFilterValue = new IntegerConfigValue(
            "transitclock.core.frequency.minTravelTimeFilterValue",
            0,
            "The value to be included in average calculation for Travel times must exceed" + " this value.");

    public static IntegerConfigValue maxTravelTimeFilterValue = new IntegerConfigValue(
            "transitclock.core.frequency.maxTravelTimeFilterValue",
            600000,
            "The value to be included in average calculation for Travel times must be less" + " than this value.");

    public static IntegerConfigValue minDwellTimeFilterValue = new IntegerConfigValue(
            "transitclock.core.frequency.minDwellTimeFilterValue",
            0,
            "The value to be included in average calculation for dwell time must exceed" + " this value.");

    public static IntegerConfigValue maxDwellTimeFilterValue = new IntegerConfigValue(
            "transitclock.core.frequency.maxDwellTimeFilterValue",
            600000,
            "The value to be included in average calculation for dwell time must be less" + " this value.");

    public static IntegerConfigValue cacheIncrementsForFrequencyService = new IntegerConfigValue(
            "transitclock.core.frequency.cacheIncrementsForFrequencyService",
            180 * 60,
            "This is the intervals size of the day that the average is applied to. ");

    public static int getCacheIncrementsForFrequencyService() {
        return cacheIncrementsForFrequencyService.getValue();
    }




    public static final StringConfigValue cacheReloadStartTimeStr = new StringConfigValue(
            "transitclock.core.cacheReloadStartTimeStr",
            "",
            "Date and time of when to start reading arrivaldepartures to inform caches.");

    public static final StringConfigValue cacheReloadEndTimeStr = new StringConfigValue(
            "transitclock.core.cacheReloadEndTimeStr",
            "",
            "Date and time of when to end reading arrivaldepartures to inform caches.");
}
