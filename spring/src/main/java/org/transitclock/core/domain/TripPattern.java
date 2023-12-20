/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.transitclock.gtfs.GtfsRoute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A trip pattern, as obtained from stop_times.txt GTFS file. A trip pattern defines what stops are
 * associated with a trip. Trip pattern reduces the amount of data needed to describe an agency
 * since trips usually can share trip patterns.
 */
@Data
@Slf4j
@Document(collection = "TripPatterns")
public class TripPattern implements Serializable {

    @Data
    public static class Key {
        // Which configuration revision used
        private final int configRev;

        // The ID of the trip pattern
        private final String id;
    }

    @Id
    @Delegate
    protected final Key key;


    protected final String shapeId;

    // CascadeType.SAVE_UPDATE so that when the TripPattern is stored the
    // Paths are automatically stored.
//    @Cascade({CascadeType.SAVE_UPDATE})
    @DocumentReference(sort = "listIndex")
    protected final List<StopPath> stopPaths;

    private String headsign;

    private final String directionId;

    private final String routeId;

    private final String routeShortName;

    // So know lat lon range of the trip pattern
    private final Extent extent;

    // So know which trips use this trip pattern
    @Transient
    private List<Trip> trips = new ArrayList<>();

    // For quickly finding a StopPath using a stop ID.
    // Keyed on stop ID. Since this member is transient this
    // class implements LifeCycle interface so that the
    // member can be initialized using onLoad(). Can't use
    // @PostLoad annotation because that only works when using
    // EntityManager but we are using regular Hibernate sessions.
    @Transient
    protected final Map<String, StopPath> stopPathsMap = new HashMap<>();

    // For specifying max size of the trip pattern ID
    public static final int TRIP_PATTERN_ID_LENGTH = 120;
    // For specifying max size of headsign
    public static final int HEADSIGN_LENGTH = 255;

    /**
     * Create a TripPattern. For when processing GTFS data.
     *
     * <p>Note: The name comes from the trip trip_headsign data. If not set then uses name of last
     * stop for trip.
     *
     * @param configRev
     * @param shapeId Part of what identifies the trip pattern
     * @param stopPaths Part of what identifies the trip pattern
     * @param trip For supplying additional info
     * @param gtfsData So can access stop data for determining extent of trip pattern.
     */
    public TripPattern(int configRev, String shapeId, List<StopPath> stopPaths, Trip trip, GtfsData gtfsData) {

        this.shapeId = shapeId;
        this.stopPaths = stopPaths;

        // Because will be writing data to the sandbox rev in the db
        this.key = new Key(configRev, generateTripPatternId(shapeId, stopPaths, trip, gtfsData));

        // Now that have the trip pattern ID set it for each StopPath
        for (StopPath path : stopPaths) {
            path.setTripPatternId(key.id);
        }

        // The trip_headsign in trips.txt and therefore the the trip name can be
        // null. For these cases use the last stop as a destination.
        if (trip.getHeadsign() != null) {
            this.headsign = trip.getHeadsign();
        } else {
            // trip_headsign was null so try using final stop name as the destination
            // as a fallback.
            StopPath lastPath = stopPaths.get(stopPaths.size() - 1);
            String lastStopIdForTrip = lastPath.getStopId();
            Stop lastStopForTrip = gtfsData.getStop(lastStopIdForTrip);
            this.headsign = lastStopForTrip.getName();
        }
        // Make sure headsign not too long for db
        if (this.headsign.length() > HEADSIGN_LENGTH) {
            this.headsign = this.headsign.substring(0, HEADSIGN_LENGTH);
        }

        // Store additional info from this trip
        this.directionId = trip.getDirectionId();
        this.routeId = trip.getRouteId();
        this.routeShortName = getRouteShortName(routeId, gtfsData);

        // Remember that this trip pattern refers to this particular
        // trip. Additional trips will be added as they are processed.
        this.trips.add(trip);

        // Determine extent of trip pattern and store it. Also, create
        // the stopPathsMap and fill it in.
        this.extent = new Extent();
        for (StopPath stopPath : stopPaths) {
            // Determine the stop
            Stop stop = gtfsData.getStop(stopPath.getStopId());
            this.extent.add(stop.getLoc());
            this.stopPathsMap.put(stopPath.getStopId(), stopPath);
        }
    }

    /**
     * Gets the route_short_name from the GTFS route data. If the route_short_name was not specified
     * in the GTFS data then will use the route_long_name. This way the route short name returned
     * will always be something appropriate.
     *
     * @param routeId
     * @param gtfsData
     * @return The route_short_name if set, otherwise the route_long_name
     */
    private static String getRouteShortName(String routeId, GtfsData gtfsData) {
        // Determine GtfsRoute for the route ID
        GtfsRoute gtfsRoute = gtfsData.getGtfsRoute(routeId);
        if (gtfsRoute == null) return null;

        // Use route_short_name. If not defined then use route_long_name
        String name = gtfsRoute.getRouteShortName();
        if (name == null) name = gtfsRoute.getRouteLongName();

        return name;
    }



    /**
     * Determines the ID of the TripPattern. It is of course important that the trip pattern be
     * unique. It also needs to be consistent even if the order of the stop_times file changes or
     * trips are added or removed. Plus it needs to be understandable. Therefore it consists of
     * appending the shapeId (if not-null), the from stop ID, the to stop ID, and a hash of the
     * concatenation of all the stop IDs that make up the trip pattern. It will be something like
     * "shape_932012_stops_stop1_to_stop2_hash". Long, but it is readable, unique, and consistent.
     *
     * <p>It is important to not just always use "stop1_to_stop2" since some agencies might for the
     * same stops have different stopPaths for connecting them. Therefore should use the shapeId
     * from the Trip passed in.
     *
     * @param shapeId Used for the trip pattern id if it is not null
     * @param stopPaths If shapeId null then used as part of ID
     * @param trip In case things get complicated with determining ID and need to log message
     * @param gtfsData In case things get complicated with determining ID
     * @return Unique generated trip pattern ID for the specified trip
     */
    private static String generateTripPatternId(
            String shapeId, List<StopPath> stopPaths, Trip trip, GtfsData gtfsData) {
        // The ID to be constructed and returned
        String tripPatternId = "";

        // If shapeId defined then start with it
        if (shapeId != null) {
            tripPatternId = "shape_" + shapeId + "_";
        }

        // Add the from and to stop IDs
        if (stopPaths != null && stopPaths.size() > 1) {
            StopPath path1 = stopPaths.get(0);
            StopPath path2 = stopPaths.get(stopPaths.size() - 1);
            tripPatternId += path1.getStopId() + "_to_" + path2.getStopId() + "_";
        } else {
            return tripPatternId;
        }

        // Determine hash in hexadecimal format of list of stops
        StringBuilder sb = new StringBuilder();
        for (StopPath stopPath : stopPaths) {
            sb.append(stopPath.getStopId());
        }
        String hexOfStopsHash = String.format("%x", sb.toString().hashCode());
        tripPatternId += hexOfStopsHash;

        // Make sure not too long for the database column. Include possibility
        // of needing to include the "_variation N" to make it unique. Otherwise
        // wouldn't notice problem until actually written to db.
        int extraNeededForVariation = "_variation".length() + 2;
        if (tripPatternId.length() + extraNeededForVariation > TRIP_PATTERN_ID_LENGTH)
            tripPatternId =
                    tripPatternId.substring(tripPatternId.length() + extraNeededForVariation - TRIP_PATTERN_ID_LENGTH);

        // Still need to make sure that tripPatternIds are unique. This should
        // never be a problem due to the rather unique way the trip pattern IDs
        // are determined but still want to be absolutely safe since using a
        // hash of the list of stop IDs and a hash isn't guaranteed to be
        // unique.
        boolean problemWithTripPatternId = false;
        int variationCounter = 1;
        String originalTripPatternId = tripPatternId;
        while (gtfsData.isTripPatternIdAlreadyUsed(tripPatternId)) {
            tripPatternId = originalTripPatternId + "_variation" + variationCounter++;
            problemWithTripPatternId = true;
        }

        if (problemWithTripPatternId)
            logger.info(
                    "There was an issue with creating trip "
                            + "pattern for tripId={} for routeId={} in "
                            + "TripPattern.generateTripPatternId(). "
                            + "There already was a trip pattern with the desired name. "
                            + "This likely means that a trip pattern is defined with the "
                            + "same shapeId (which is used for the trip pattern ID) but "
                            + "with different stop list indicating the trips are not "
                            + "consistently defined. Therefore using the special "
                            + "tripPatternId={}.",
                    trip.getId(),
                    trip.getRouteId(),
                    tripPatternId);

        return tripPatternId;
    }

    public void addTrip(Trip trip) {
        trips.add(trip);
    }

    /**
     * For when don't want to display the entire contents of TripPattern, which can be pretty large
     * because contains list of stops and trips.
     *
     * @return A short version of the TripPattern object
     */
    public String toShortString() {
        return "Headsign \""
                + headsign
                + "\""
                + " direction "
                + directionId
                + " from stop "
                + stopPaths.get(0).getStopId()
                + " to stop "
                + stopPaths.get(stopPaths.size() - 1).getStopId();
    }

    /**
     * A short version of the Trip string. Only includes the name and a list of the trip ids.
     *
     * @return
     */
    public String toStringListingTripIds() {
        String s = "Trip Pattern [" + "key=" + key + ", headsign=" + headsign + ", trips=[";
        for (Trip trip : trips) {
            s += trip.getId() + ",";
        }
        s += "] ]";
        return s;
    }

    /**
     * A short version of the Trip string. Only includes the name and a list of the stop ids.
     *
     * @return
     */
    public String toStringListingStopIds() {
        String s = "Trip Pattern [" + "key=" + key + ", headsign=" + headsign + ", stopIds=[";
        for (StopPath stopPath : stopPaths) {
            s += stopPath.getStopId() + ",";
        }
        s += "] ]";
        return s;
    }

    /**
     * Returns true if this trip pattern includes the specified stopId.
     *
     * @param stopId
     * @return
     */
    public boolean servesStop(String stopId) {
        // Look through this trip pattern to see if it includes specified stop
        for (StopPath stopPath : stopPaths) {
            if (stopPath.getStopId().equals(stopId)) return true;
        }

        // That stop is not in the trip pattern
        return false;
    }

    /**
     * Returns the StopPath for this TripPattern as specified by the stopId parameter. Uses a map so
     * is reasonably fast. Synchronized to make sure that only a single thread can initialize the
     * transient map.
     *
     * @param stopId
     * @return The StopPath specified by the stop ID, or null if this TripPattern does not contain
     *     that stop.
     */
    public synchronized StopPath getStopPath(String stopId) {
        // Return the StopPath specified by the stop ID
        return stopPathsMap.get(stopId);
    }

    /**
     * Returns true if for this TripPattern that stopId2 is after stopId1. If either stopId1 or
     * stopId2 are not in the trip pattern then false is returned.
     *
     * @param stopId1
     * @param stopId2
     * @return True if stopId2 is after stopId1
     */
    public boolean isStopAtOrAfterStop(String stopId1, String stopId2) {
        // Short cut
        if (stopId1.equals(stopId2)) return true;

        // Go through list of stops for trip pattern
        boolean stopId1Found = false;
        for (StopPath stopPath : stopPaths) {
            if (stopPath.getStopId().equals(stopId1)) {
                stopId1Found = true;
            }

            if (stopId1Found && stopPath.getStopId().equals(stopId2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns list of stop IDs for the stop paths for this trip pattern.
     *
     * @return
     */
    public List<String> getStopIds() {
        List<String> list = new ArrayList<String>(stopPaths.size());
        for (StopPath stopPath : stopPaths) list.add(stopPath.getStopId());
        return list;
    }

    /**
     * Returns the stop ID of the last stop of the trip. This is the destination for the trip.
     *
     * @return ID of last stop
     */
    public String getLastStopIdForTrip() {
        return stopPaths.get(stopPaths.size() - 1).getStopId();
    }

    /**
     * Returns length of the trip from the first terminal to the last.
     *
     * @return
     */
    public double getLength() {
        double length = 0.0;
        for (int i = 1; i < stopPaths.size(); ++i) {
            length += stopPaths.get(i).getLength();
        }
        return length;
    }

    /**
     * @param index
     * @return The specified StopPath or null if index out of range
     */
    public StopPath getStopPath(int index) {
        if (index < 0 || index >= stopPaths.size()) return null;

        return stopPaths.get(index);
    }

    /**
     * Returns the number of stopPaths/stops configured.
     *
     * @return
     */
    public int getNumberStopPaths() {
        return stopPaths.size();
    }

    /**
     * Gets the stopId of the specified stop
     *
     * @param i
     * @return
     */
    public String getStopId(int i) {
        return stopPaths.get(i).getStopId();
    }

    /**
     * Gets the name of the specified stop as obtained by a Core predictor. Cannot be used with
     * other applications.
     *
     * @param i
     * @return
     */
    public String getStopName(int i) {
        return stopPaths.get(i).getStopName();
    }

    /**
     * Gets the pathId of the specified stop
     *
     * @param i
     * @return
     */
    public String getStopPathId(int i) {
        return stopPaths.get(i).getStopPathId();
    }

    /**
     * For modifying the headsign. Useful for when reading in GTFS data and determine that the
     * headsign should be modified because it is for a different last stop or such.
     *
     * @param headsign
     */
    public void setHeadsign(String headsign) {
        this.headsign = headsign.length() <= HEADSIGN_LENGTH ? headsign : headsign.substring(0, HEADSIGN_LENGTH);
    }
}
