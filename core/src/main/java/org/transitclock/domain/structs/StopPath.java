/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.classic.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.Core;
import org.transitclock.config.data.CoreConfig;

/**
 * A StopPath is a set of points that defines how a vehicle gets from one stop to another. The stops
 * do not necessarily lie directly on the segments since the segments are likely to be street center
 * line data while the stops are usually on the sidewalk.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Entity
@Data
@DynamicUpdate
@Table(name = "stop_paths")
public class StopPath implements Serializable, Lifecycle {

    @Id
    @Column(name = "trip_pattern_id", length = TripPattern.TRIP_PATTERN_ID_LENGTH)
    private String tripPatternId;
    // Using size of
    // 2 * DEFAULT_ID_SIZE since stop path names are stop1_to_stop2 so can
    // be twice as long as other IDs. And when using GTFS Editor the IDs
    // are quite long, a bit longer than 40 characters.
    @Id
    @Column(name = "stop_path_id", length = 120)
    private final String stopPathId;

    @Id
    @Column(name = "config_rev")
    private final int configRev;

    @Column(name = "stop_id", length = 60)
    private final String stopId;

    // The stop_sequence for the trip from the GTFS stop_times.txt file
    @Column(name = "gtfs_stop_seq")
    private final int gtfsStopSeq;

    // Needed for schedule adherence so can return scheduled departure time
    // for most stops but the scheduled arrival time for the last stop for
    // a trip.
    @Column(name = "last_stop_in_trip")
    private final boolean lastStopInTrip;

    // route ID from GTFS data
    @Column(name = "route_id", length = 60)
    private final String routeId;

    // Indicates that vehicle can leave route path before departing this stop
    // since the driver is taking a break.
    @Column(name = "layover_stop", nullable = false)
    private final boolean layoverStop;

    // Indicates that vehicle is not supposed to depart the stop until the
    // scheduled departure time.
    @Column(name = "wait_stop", nullable = false)
    private final boolean waitStop;

    // If should generate special ScheduleAdherence data for this stop
    @Column(name = "schedule_adherence_stop", nullable = false)
    private final boolean scheduleAdherenceStop;

    // How long a break driver is entitled to have at the stop, even if it
    // means that the driver should depart after the scheduled departure time.
    @Column(name = "break_time")
    private final Integer breakTime;

    // sacrifice performance for reportability -- use a child table instead of java serialization
    @ElementCollection
    @CollectionTable(name="stoppath_locations",joinColumns= {
            @JoinColumn(name = "stoppath_trip_pattern_id", referencedColumnName = "trip_pattern_id"),
            @JoinColumn(name = "stoppath_stop_path_id", referencedColumnName = "stop_path_id"),
            @JoinColumn(name = "stoppath_config_rev", referencedColumnName = "config_rev")
    })
    @OrderColumn(name = "list_index")
    private List<Location> locations;

    // Having the path length readily accessible via the database is handy
    // since that way can easily do queries to determine travel speeds and
    // such.
    @Column(name = "path_length")
    private double pathLength;

    @Column(name = "max_distance")
    private Double maxDistance;

    @Column(name = "max_speed")
    private Double maxSpeed;
    // So can have easy access to vectors representing the segments so
    // can easily determine heading. Declared transient because this
    // info is generated by other members after the object has been
    // loaded from the database.
    @Transient
    @EqualsAndHashCode.Exclude
    private List<VectorWithHeading> vectors = null;

    /** This is used just to get better location of the bustop where bestmatch is done. */
    @Transient
    @EqualsAndHashCode.Exclude
    private Double shapeDistanceTraveled;

    public StopPath(
            int configRev,
            String pathId,
            String stopId,
            int gtfsStopSeq,
            boolean lastStopInTrip,
            String routeId,
            boolean layoverStop,
            boolean waitStop,
            boolean scheduleAdherenceStop,
            Integer breakTime,
            Double maxDistance,
            Double maxSpeed,
            Double shapeDistanceTraveled) {
        this.configRev = configRev;
        this.stopPathId = pathId;
        this.stopId = stopId;
        this.gtfsStopSeq = gtfsStopSeq;
        this.lastStopInTrip = lastStopInTrip;
        this.routeId = routeId;
        this.locations = null;
        this.layoverStop = layoverStop;
        this.waitStop = waitStop;
        this.scheduleAdherenceStop = scheduleAdherenceStop;
        // If valid break time was passed in then use it. Otherwise
        // use the default value.
        this.breakTime = breakTime;
        this.maxDistance = maxDistance;
        this.maxSpeed = maxSpeed;
        this.shapeDistanceTraveled = shapeDistanceTraveled;
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected StopPath() {
        this.configRev = -1;
        this.stopPathId = null;
        this.stopId = null;
        this.gtfsStopSeq = -1;
        this.lastStopInTrip = false;
        this.routeId = null;
        this.tripPatternId = null;
        this.locations = null;
        this.layoverStop = false;
        this.waitStop = false;
        this.scheduleAdherenceStop = false;
        this.breakTime = null;
        this.maxDistance = null;
        this.maxSpeed = null;
    }

    public static List<StopPath> getPaths(Session session, int configRev) throws HibernateException {
        return session.createQuery("FROM StopPath WHERE configRev = :configRev", StopPath.class)
                .setParameter("configRev", configRev)
                .list();
    }

    /**
     * For consistently naming the path Id. It is based on the current stop ID and the previous stop
     * Id. If previousStopId is null then will return "to_" + stopId. If not null will return
     * previousStopId + "_to_" + stopId.
     *
     * @param previousStopId
     * @param stopId
     * @return
     */
    public static String determinePathId(String previousStopId, String stopId) {
        if (previousStopId == null) {
            return "to_" + stopId;
        } else {
            return previousStopId + "_to_" + stopId;
        }
    }

    /**
     * Returns the distance to travel along the path. Summation of all path segments.
     *
     * @return
     */
    public double length() {
        // Make sure locations were set before trying to access them
        if (locations == null) {
            logger.error("For stopPathId={} trying to access locations when they have not been set.", stopPathId);
            return Double.NaN;
        }

        double totalLength = 0.0;
        for (int i = 0; i < locations.size() - 1; ++i) {
            totalLength += (new Vector(locations.get(i), locations.get(i + 1))).length();
        }
        return totalLength;
    }

    /**
     * For when seeing if TripPatternBase is unique in a collection. When this is done the StopPath
     * isn't yet fully complete so only compare the key members that signify if a StopPath is
     * unique.
     */
    public int basicHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + configRev;
        result = prime * result + ((stopPathId == null) ? 0 : stopPathId.hashCode());
        result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
        result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
        return result;
    }

    /**
     * For when seeing if TripPatternBase is unique in a collection. When this is done the StopPath
     * isn't yet fully complete so only compare the key members that signify if a StopPath is
     * unique.
     */
    public boolean basicEquals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        StopPath other = (StopPath) obj;
        if (configRev != other.configRev) return false;
        if (stopPathId == null) {
            if (other.stopPathId != null) return false;
        } else if (!stopPathId.equals(other.stopPathId)) return false;
        if (routeId == null) {
            if (other.routeId != null) return false;
        } else if (!routeId.equals(other.routeId)) return false;
        if (stopId == null) {
            if (other.stopId != null) return false;
        } else if (!stopId.equals(other.stopId)) return false;
        return gtfsStopSeq == other.gtfsStopSeq;
    }

    /**
     * @return the stopPathId
     */
    public String getId() {
        return stopPathId;
    }

    /**
     * Provides the name of the stop as obtained by a Core predictor. Cannot be used with other
     * applications.
     *
     * @return the name of the stop
     */
    public String getStopName() {
        return Core.getInstance().getDbConfig().getStop(stopId).getName();
    }

    /**
     * Locations are not available when StopPath is first created so need to be able to set them
     * after construction. Sets the locations member and also determines the pathLength.
     *
     * @param locations
     */
    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;

        pathLength = 0.0;
        for (int i = 0; i < locations.size() - 1; ++i) {
            Location l1 = locations.get(i);
            Location l2 = locations.get(i + 1);
            pathLength += l1.distance(l2);
        }
    }

    /**
     * Returns the location of the stop at the end of the path.
     *
     * @return Location of stop
     */
    public Location getStopLocation() {
        // Simply return the last location of the path, since it
        // corresponds to the stop associated with the path.
        return locations.get(locations.size() - 1);
    }

    /**
     * @return Number of segments in path.
     */
    public int getNumberSegments() {
        return locations.size() - 1;
    }

    /**
     * Returns the end of the path, which is where the stop is. Note that it is not exactly the stop
     * location because stops do not need to be on the path.
     *
     * @return
     */
    public Location getEndOfPathLocation() {
        return locations.get(locations.size() - 1);
    }

    /**
     * @return Combined length of all the path segments
     */
    public double getLength() {
        return pathLength;
    }

    /**
     * @return List of VectorWithHeadings of the segments that make up the path
     */
    public List<VectorWithHeading> getSegmentVectors() {
        return vectors;
    }

    /**
     * Returns the vector for the specified segment.
     *
     * @param segmentIndex
     * @return The vector, or null if segmentIndex not valid
     */
    public VectorWithHeading getSegmentVector(int segmentIndex) {
        // If index out of range return null
        if (segmentIndex < 0 || segmentIndex >= getSegmentVectors().size()) {
            return null;
        }

        return getSegmentVectors().get(segmentIndex);
    }

    /**
     * @param index
     * @return Location for the specified index along the StopPath
     */
    public Location getLocation(int index) {
        return locations.get(index);
    }

    /**
     * Returns how long driver is expected to have a break for at this stop.
     *
     * @return Layover time in seconds if layover stop, otherwise, 0.
     */
    public int getBreakTimeSec() {
        if (layoverStop) {
            if (breakTime != null) return breakTime;
            else return CoreConfig.getDefaultBreakTimeSec();
        } else {
            return 0;
        }
    }

    /**
     * How far a vehicle can be ahead of a stop and be considered to have arrived.
     *
     * @return
     */
    public double getBeforeStopDistance() {
        return CoreConfig.getBeforeStopDistance();
    }

    /**
     * How far a vehicle can be past a stop and still be considered at the stop.
     *
     * @return
     */
    public double getAfterStopDistance() {
        return CoreConfig.getAfterStopDistance();
    }

    /* (non-Javadoc)
     * @see org.hibernate.classic.Lifecycle#onDelete(org.hibernate.Session)
     */
    @Override
    public boolean onDelete(Session arg0) throws CallbackException {
        // Don't veto delete
        return false;
    }

    /*
     * When the vector is read in from db this method is automatically called to
     * set the transient vector array. This way it is simpler to go through the
     * path segments to determine matches.
     */
    @Override
    public void onLoad(Session arg0, Serializable arg1) {
        vectors = new ArrayList<>(locations.size() - 1);
        for (int segmentIndex = 0; segmentIndex < locations.size() - 1; ++segmentIndex) {
            VectorWithHeading v = new VectorWithHeading(
                    nullSafeLocation(locations.get(segmentIndex)), nullSafeLocation(locations.get(segmentIndex + 1)));
            vectors.add(v);
        }
    }

    private Location nullSafeLocation(Location location) {
        if (location == null) {
            location = new Location(0.0, 0.0);
        }
        return location;
    }

    @Override
    public boolean onSave(Session arg0) throws CallbackException {
        // Don't veto save
        return false;
    }

    @Override
    public boolean onUpdate(Session arg0) throws CallbackException {
        // Don't veto update
        return false;
    }
}
