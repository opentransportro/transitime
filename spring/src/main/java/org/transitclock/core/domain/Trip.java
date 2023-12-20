/* (C)2023 */
package org.transitclock.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.transitclock.utils.Time;
import org.transitclock.gtfs.GtfsTrip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a GTFS trip but also includes travel time information.
 *
 * <p>Serializable since Hibernate requires such.
 *
 * <p>Implements Lifecycle so that can have the onLoad() callback be called when reading in data so
 * that can intern() member strings. In order to do this the String members could not be declared as
 * final since they are updated after the constructor is called.
 *
 * @author SkiBu Smith
 */
@Data
@Document(collection = "trips")
public class Trip implements Serializable {

    @Data
    @AllArgsConstructor
    public static final class Key {
        private final int configRev;
        private String tripId;
        // The startTime needs to be an Id column because GTFS frequencies.txt
        // file can be used to define multiple trips with the same trip ID.
        // It is in number of seconds into the day.
        // Not declared as final because only used for frequency based trips.
        private Integer startTime;

    }

    @Id
    @Delegate
    private Key key;

    // Used by some agencies to identify the trip in the AVL feed
    private String tripShortName;

    // Number of seconds into the day.
    // Not final because only used for frequency based trips.
    private Integer endTime;

    private String directionId;

    private String routeId;

    // Route short name is also needed because some agencies such as SFMTA
    // change the route IDs when making schedule changes. But we need a
    // consistent route identifier for certain things, such as bookmarking
    // prediction pages or for running schedule adherence reports over
    // time. For where need a route identifier that is consistent over time
    // it can be best to use the routeShortName.
    private String routeShortName;

    // So can determine all the stops and stopPaths associated with trip
    @DocumentReference
    private TripPattern tripPattern;

    @DocumentReference
    private TravelTimesForTrip travelTimes;

    // Contains schedule time for each stop as obtained from GTFS
    // stop_times.txt file. Useful for determining schedule adherence.
    private final List<ScheduleTime> scheduledTimesList = new ArrayList<>();

    // For non-scheduled blocks where vehicle runs a trip as a continuous loop
    private final boolean noSchedule;

    // For when times are determined via the GTFS frequency.txt file and
    // exact_times for the trip is set to true. Indicates that the schedule
    // times were determined using the trip frequency and start_time.
    private final boolean exactTimesHeadway;

    // Service ID for the trip
    private String serviceId;

    // The GTFS trips.txt trip_headsign if set. Otherwise will get from the
    // stop_headsign, if set, from the first stop of the trip. Otherwise null.
    private String headsign;

    // From GTFS trips.txt block_id if set. Otherwise the trip_id.
    private String blockId;

    // The GTFS trips.txt shape_id
    private String shapeId;

    @Transient
    private Route route;

    /**
     * Constructs Trip object from GTFS data.
     *
     * <p>Does not set startTime nor endTime. Those are set separately using addScheduleTimes().
     * Also doesn't set travel times. Those are set separately using setTravelTimes().
     *
     * @param configRev
     * @param gtfsTrip The GTFS data describing the trip
     * @param properRouteId The routeId, but can actually be the ID of the parent route.
     * @param routeShortName Needed to provide a route identifier that is consistent over schedule
     *     changes.
     * @param unprocessedHeadsign the headsign from the GTFS trips.txt file, or if that is not
     *     available then the stop_headsign from the GTFS stop_times.txt file.
     * @param titleFormatter So can fix titles associated with trip
     */
    public Trip(
            int configRev,
            GtfsTrip gtfsTrip,
            String properRouteId,
            String routeShortName,
            String unprocessedHeadsign,
            TitleFormatter titleFormatter) {
        this.key = new Key(configRev, gtfsTrip.getTripId(), null);
        this.tripShortName = gtfsTrip.getTripShortName();
        this.directionId = gtfsTrip.getDirectionId();
        this.routeId = properRouteId != null ? properRouteId : gtfsTrip.getRouteId();
        this.routeShortName = routeShortName;
        this.serviceId = gtfsTrip.getServiceId();
        this.headsign = processedHeadsign(unprocessedHeadsign, routeId, titleFormatter);

        // block column is optional in GTFS trips.txt file. Best can do when
        // block ID is not set is to use the trip short name or the trip id
        // as the block. MBTA uses trip short name in the feed so start with
        // that.
        String theBlockId = gtfsTrip.getBlockId();
        if (theBlockId == null) {
            theBlockId = gtfsTrip.getTripShortName();
            if (theBlockId == null) {
                theBlockId = gtfsTrip.getTripId();
            }
        }
        this.blockId = theBlockId;
        this.shapeId = gtfsTrip.getShapeId();
        this.noSchedule = false;

        // Not a frequency based trip with an exact time so remember such
        this.exactTimesHeadway = false;
    }

    /**
     * Creates a copy of the Trip object but adjusts the startTime, endTime, and scheduledTimesMap
     * according to the frequenciesBasedStartTime. This is used when the frequencies.txt file
     * specifies exact_times for a trip.
     *
     * @param tripFromStopTimes
     * @param frequenciesBasedStartTime
     */
    public Trip(Trip tripFromStopTimes, int frequenciesBasedStartTime) {
        this.key = new Key(tripFromStopTimes.key.configRev, tripFromStopTimes.key.tripId, tripFromStopTimes.key.startTime + frequenciesBasedStartTime);
        this.tripShortName = tripFromStopTimes.tripShortName;
        this.directionId = tripFromStopTimes.directionId;
        this.routeId = tripFromStopTimes.routeId;
        this.routeShortName = tripFromStopTimes.routeShortName;
        this.serviceId = tripFromStopTimes.serviceId;
        this.headsign = tripFromStopTimes.headsign;
        this.shapeId = tripFromStopTimes.shapeId;
        this.tripPattern = tripFromStopTimes.tripPattern;
        this.travelTimes = tripFromStopTimes.travelTimes;

        // Set the updated start and end times by using the frequencies
        // based start time.
        this.endTime = tripFromStopTimes.endTime + frequenciesBasedStartTime;

        // Since frequencies being used for configuration we will have multiple
        // trips with the same ID. But need a different block ID for each one.
        // Therefore use the original trip's block ID but then append the
        // start time as a string to make it unique.
        this.blockId = tripFromStopTimes.blockId + "_" + Time.timeOfDayStr(this.key.startTime);

        // Set the scheduledTimesMap by using the frequencies based start time
        for (ScheduleTime schedTimeFromStopTimes : tripFromStopTimes.scheduledTimesList) {
            Integer arrivalTime = null;
            if (schedTimeFromStopTimes.getArrivalTime() != null)
                arrivalTime = schedTimeFromStopTimes.getArrivalTime() + frequenciesBasedStartTime;
            Integer departureTime = null;
            if (schedTimeFromStopTimes.getDepartureTime() != null)
                departureTime = schedTimeFromStopTimes.getDepartureTime() + frequenciesBasedStartTime;

            ScheduleTime schedTimeFromFrequency = new ScheduleTime(arrivalTime, departureTime);
            this.scheduledTimesList.add(schedTimeFromFrequency);
        }

        // Since this constructor is only for frequency based trips where
        // exact_times is true set the corresponding members to indicate such
        this.noSchedule = false;
        this.exactTimesHeadway = true;
    }

    /**
     * Creates a copy of the Trip object but adjusts the startTime, endTime, and scheduledTimesMap
     * according to the frequenciesBasedStartTime. This is used when the frequencies.txt specifies a
     * time range for a trip but where exact_times is false. This is for noSchedule routes where
     * vehicle is expected to continuously run on a route without a schedule.
     *
     * @param tripFromStopTimes
     * @param frequenciesBasedStartTime
     * @param frequenciesBasedEndTime
     */
    public Trip(Trip tripFromStopTimes, int frequenciesBasedStartTime, int frequenciesBasedEndTime) {
        this.key = new Key(tripFromStopTimes.key.configRev, tripFromStopTimes.key.tripId, tripFromStopTimes.key.startTime);
        this.tripShortName = tripFromStopTimes.tripShortName;
        this.directionId = tripFromStopTimes.directionId;
        this.routeId = tripFromStopTimes.routeId;
        this.routeShortName = tripFromStopTimes.routeShortName;
        this.serviceId = tripFromStopTimes.serviceId;
        this.headsign = tripFromStopTimes.headsign;
        this.shapeId = tripFromStopTimes.shapeId;
        this.tripPattern = tripFromStopTimes.tripPattern;
        this.travelTimes = tripFromStopTimes.travelTimes;
        this.blockId = tripFromStopTimes.blockId;

        // Set the updated start and end times by using the times from the
        // frequency.txt GTFS file
        this.endTime = frequenciesBasedEndTime;

        // Set the scheduledTimesMap by using the frequencies based start time
        this.scheduledTimesList.addAll(tripFromStopTimes.scheduledTimesList);

        // Since this constructor is only for frequency based trips where
        // exact_times is false set the corresponding members to indicate such
        this.noSchedule = true;
        this.exactTimesHeadway = false;
    }


    /**
     * For refining the headsign. For some agencies like VTA & AC Transit the headsign includes the
     * route number at the beginning. This is indeed the headsign but not really appropriate as a
     * destination indicator. Ideally it might make sense to have an unprocessed headsign and a
     * separate destination indicator but for now lets just use headsign member. Also uses
     * TitleFormatter to deal with capitalization.
     *
     * @param gtfsHeadsign
     * @param routeId
     * @param titleFormatter
     * @return Processed headsign with proper formatting, or null if gtfsHeadsign passed in is null
     */
    private String processedHeadsign(String gtfsHeadsign, String routeId, TitleFormatter titleFormatter) {
        // Prevent NPE since gtfsHeadsign can be null
        if (gtfsHeadsign == null) return null;

        String headsignWithoutRouteInfo;
        if (gtfsHeadsign.startsWith(routeId)) {
            // Headsign starts with route ID so trim that off
            headsignWithoutRouteInfo = gtfsHeadsign.substring(routeId.length()).trim();

            // Handle possibility of having a separator between the route ID
            // and the rest of the headsign.
            if (headsignWithoutRouteInfo.startsWith(":") || headsignWithoutRouteInfo.startsWith("-"))
                headsignWithoutRouteInfo = headsignWithoutRouteInfo.substring(1).trim();
        } else
            // Headsign doesn't start with route ID so use entire string
            headsignWithoutRouteInfo = gtfsHeadsign;

        // Handle capitalization and any other title formatting necessary
        return titleFormatter.processTitle(headsignWithoutRouteInfo);
    }

    /**
     * For adding ScheduleTimes for stops to a Trip. Updates scheduledTimesMap, startTime, and
     * endTime.
     *
     * @param newScheduledTimesList
     * @throws ArrayIndexOutOfBoundsException If not enough space allocated for serialized schedule
     *     times in scheduledTimesMap column
     */
    public void addScheduleTimes(List<ScheduleTime> newScheduledTimesList) {
        // For each schedule time (one per stop path)
        for (ScheduleTime scheduleTime : newScheduledTimesList) {
            // Add the schedule time to the map
            scheduledTimesList.add(scheduleTime);

            // Determine the begin and end time. Assumes that times are added in order
            if (key.startTime == null ||
                    (scheduleTime.getDepartureTime() != null && scheduleTime.getDepartureTime() < key.startTime))
                key.startTime = scheduleTime.getDepartureTime();
            if (endTime == null || (scheduleTime.getArrivalTime() != null && scheduleTime.getArrivalTime() > endTime))
                endTime = scheduleTime.getArrivalTime();
        }
    }


    /**
     * @return the tripId
     */
    public String getId() {
        return key.tripId;
    }

    /**
     * @return the tripShortName
     */
    public String getShortName() {
        return tripShortName;
    }


    /**
     * Returns the routeShortName. If it is null then returns the full route name. Causes exception
     * if Core not available, such as when processing GTFS data.
     *
     * @return the routeShortName
     */
    public String getRouteShortName() {
        return routeShortName != null ? routeShortName : getRouteName();
    }

    /**
     * Returns the Route object for this trip. This object is determined and cached when first
     * accessed. Uses value read in from database using Core, which means that it won't be available
     * when processing GTFS data since that doesn't have core object.
     *
     * @return The route or null if no Core object available
     */
    public Route getRoute() {
        if (route == null) {
            if (Core.isCoreApplication()) {
                DbConfig dbConfig = Core.getInstance().getDbConfig();
                if (dbConfig == null) return null;
                route = dbConfig.getRouteById(routeId);
            } else {
                return null;
            }
        }
        return route;
    }

    /**
     * Returns route name. Gets it from the Core database configuration. If Core database
     * configuration not available such as when processing GTFS data then will return null. Will
     * cause exception if core not available and gtfs data not loaded in db yet since the active
     * revisions will not be set properly yet.
     *
     * @return The route name or null if Core object not available
     */
    public String getRouteName() {
        Route route = getRoute();
        if (route == null) {
            return null;
        }

        return route.getName();
    }

    /**
     * For modifying the headsign. Useful for when reading in GTFS data and determine that the
     * headsign should be modified because it is for a different last stop or such.
     *
     * @param headsign
     */
    public void setHeadsign(String headsign) {
        this.headsign = headsign.length() <= TripPattern.HEADSIGN_LENGTH
                ? headsign
                : headsign.substring(0, TripPattern.HEADSIGN_LENGTH);
    }

    /**
     * Returns the Block that the Trip is associated with. Only valid when running the core
     * application where can use Core.getInstance(). Otherwise returns null.
     *
     * @return
     */
    public Block getBlock() {
        // If not part of the core project where DbConfig is available
        // then just return null.
        Core core = Core.getInstance();
        if (core == null) return null;
        DbConfig dbConfig = core.getDbConfig();
        if (dbConfig == null) return null;

        // Part of core project so return the Block
        return dbConfig.getBlock(serviceId, blockId);
    }

    /**
     * Returns the index of the trip in the block. Uses DbConfig from the core project to determine
     * the Block. If not part of the core project the Block info is not available and -1 is
     * returned.
     *
     * @return The index of the trip in the block or -1 if block info not available.
     */
    public int getIndexInBlock() {
        // If block info no available then simply return -1
        Block block = getBlock();
        if (block == null) return -1;

        // Block info available so return the trip index
        return block.getTripIndex(this);
    }

    /**
     * Returns the ScheduleTime object for the stopPathIndex. Will return null if there are no
     * schedule times associated with that stop for this trip. Useful for determining schedule
     * adherence.
     *
     * @param stopPathIndex
     * @return
     */
    public ScheduleTime getScheduleTime(int stopPathIndex) {
        return scheduledTimesList.get(stopPathIndex);
    }

    /**
     * @return list of schedule times for the trip
     */
    public List<ScheduleTime> getScheduleTimes() {
        return scheduledTimesList;
    }

    /**
     * Returns the travel time info for the path specified by the stopPathIndex.
     *
     * @param stopPathIndex
     * @return
     */
    public TravelTimesForStopPath getTravelTimesForStopPath(int stopPathIndex) {
        return travelTimes.getTravelTimesForStopPath(stopPathIndex);
    }

    /**
     * Returns length of the trip from the first terminal to the last.
     *
     * @return
     */
    public double getLength() {
        return getTripPattern().getLength();
    }

    /**
     * Returns the stop ID of the last stop of the trip. This is the destination for the trip.
     *
     * @return ID of last stop
     */
    public String getLastStopId() {
        return getTripPattern().getLastStopIdForTrip();
    }

    /**
     * Returns the List of the stop paths for the trip pattern
     *
     * @return
     */
    public List<StopPath> getStopPaths() {
        return tripPattern.getStopPaths();
    }

    /**
     * Returns the StopPath for the stopPathIndex specified
     *
     * @param stopPathIndex
     * @return the path specified or null if index out of range
     */
    public StopPath getStopPath(int stopPathIndex) {
        return tripPattern.getStopPath(stopPathIndex);
    }

    /**
     * Returns the StopPath specified by the stopId.
     *
     * @param stopId
     * @return The specified StopPath, or null if the stop is not part of this trip pattern.
     */
    public StopPath getStopPath(String stopId) {
        return tripPattern.getStopPath(stopId);
    }

    /**
     * Returns number of stop paths defined for this trip.
     *
     * @return Number of stop paths
     */
    public int getNumberStopPaths() {
        return getTripPattern().getStopPaths().size();
    }
}
