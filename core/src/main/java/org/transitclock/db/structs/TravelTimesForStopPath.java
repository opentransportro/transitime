/* (C)2023 */
package org.transitclock.db.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.Geo;

/**
 * Contains the expected time it takes to travel along the specified path, which is for one stop to
 * another. There can be a different list of TravelTimesForStopPath for each trip. The idea is to
 * share travel times when possible, when they are relatively the same for a trip pattern. But if a
 * trip needs separate travel times then it can have it.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@ToString
@EqualsAndHashCode
@Getter
@Table(name = "TravelTimesForStopPaths")
public class TravelTimesForStopPath implements Serializable {

    // Need a generated ID because trying to share TravelTimesForStopPath objects because
    // having a separate set for each trip would be too much. But will usually
    // still have a few per path and trip pattern. Therefore also need the
    // generated ID.
    @Id
    @Column
    @GeneratedValue
    private Integer id;

    // Need configRev for the configuration so that when old configurations
    // cleaned out can also get rid of old travel times.
    @Column
    private final int configRev;

    // Each time update travel times it gets a new travel time rev. This
    // way can compare travel times with previous revisions. Probably only need
    // to keep the previous travel time rev around for comparison but by
    // using an integer for the rev all of the revs can be kept in the db
    // if desired.
    @Column
    private final int travelTimesRev;

    // Which stop on the trip the travel times are for. Using size of
    // 2 * DEFAULT_ID_SIZE since stop path names are stop1_to_stop2 so can
    // be twice as long as other IDs. And when using GTFS Editor the IDs
    // are quite long, a bit longer than 40 characters.
    @Column(length = 2 * 60)
    private final String stopPathId;

    // The distance for each travel time segment for this path. Doesn't
    // need to be precise so use float instead of double to save memory.
    @Column
    private final float travelTimeSegmentLength;

    // Travel time is a List of Integers containing the expected travel time
    // for each travel time segment whose length is travelTimeSegmentLength.
    // Integers are used to make it take
    // less space and processing than if longs were used. Values in milliseconds.
    // There are two ways to deal with Lists of basic types in Hibernate. The
    // normal way is to declare it an @ElementCollection (and use @OrderColumn
    // to maintain order of data in list) but this causes the list data to be
    // stored in a separate table, one having the same primary keys. When
    // reading data need to do a join of the tables. When doing a write
    // many SQL statements are needed (one for each element in the List).
    // This is all very cumbersome, slow, and space inefficient since
    // storing the primary keys again for each row in the additional table.
    // The other way to deal with such a list is to have it be serializable
    // and specify (length=1000) in the @Column annotation. In this way the
    // List data is simply serialized/unserialized into a BLOB. This means that
    // don't have a separate table with a separate row for each item in the
    // list. The drawback is that since the data is in a blob it cannot be
    // read directly using SQL on the command line or for reports. But since
    // it can make things so much more efficient want to try using it.
    // NOTE: since trying to use serialization need to use ArrayList<> instead
    // of List<> since List<> doesn't implement Serializable.
    private static final int travelTimesMaxBytes = 100000;

    @Column(length = travelTimesMaxBytes)
    private final ArrayList<Integer> travelTimesMsec;

    // There is a separate time for travel and for actually stopping. For
    // many systems might not be able to really differentiate between the two
    // but if can then can make more accurate predictions. The stopTimeMsec
    // can also be used at beginning of trips to determine when buses really
    // do leave the terminus. In this way if a driver always leaves a couple
    // minutes late then the predictions will be adjusted accordingly.
    @Column
    private final int stopTimeMsec;

    // For somehow overriding times for a particular day of the week.
    // For example, could have a serviceId that represents weekdays
    // for which the same service is provided. But might want to have
    // different travel times for Fridays since afternoon rush hour is
    // definitely different for Fridays.
    @Column
    private final short daysOfWeekOverride;

    // For keeping track of how the data was obtained (historic GPS,
    // schedule, default speed, etc)
    @Column(length = 5)
    @Enumerated(EnumType.STRING)
    private final HowSet howSet;

    private static final Logger logger = LoggerFactory.getLogger(TravelTimesForStopPath.class);

    /**
     * This enumeration is for keeping track of how the travel times were determined. This way can
     * tell of they should be overridden or not.
     */
    public enum HowSet {
        // From when there are no schedule times so simply need to use a
        // default speed
        SPEED(0),

        // From interpolating data in GTFS stop_times.txt file
        SCHED(1),

        // No AVL data was available for the actual day so using data from
        // another day.
        SERVC(2),

        // No AVL data was available for the actual trip so using data from
        // a trip that is before or after the trip in question
        TRIP(3),

        // Based on actual running times as determined by AVL data
        AVL(4);

        @SuppressWarnings("unused")
        private final int value;

        HowSet(int value) {
            this.value = value;
        }

        public boolean isScheduleBased() {
            return this == SPEED || this == SCHED;
        }
    }

    public TravelTimesForStopPath(
            int configRev,
            int travelTimesRev,
            String stopPathId,
            double travelTimeSegmentDistance,
            List<Integer> travelTimesMsec,
            int stopTimeMsec,
            int daysOfWeekOverride,
            HowSet howSet,
            Trip trip)
            throws ArrayIndexOutOfBoundsException {
        // First make sure that travelTimesMsec isn't bigger than
        // the space allocated for it. Only bother checking if have
        // at least a few travel times for the path.
        if (travelTimesMsec.size() > 5) {
            int serializedSize = HibernateUtils.sizeof(travelTimesMsec);
            if (serializedSize > travelTimesMaxBytes) {
                String msg = "Too many elements in "
                        + "travelTimesMsec when constructing a "
                        + "TravelTimesForStopPath for stopPathId="
                        + stopPathId
                        + " and travelTimeSegmentDistance="
                        + Geo.distanceFormat(travelTimeSegmentDistance)
                        + " . Have "
                        + travelTimesMsec.size()
                        + " travel time segments taking up "
                        + serializedSize
                        + " bytes but only have "
                        + travelTimesMaxBytes
                        + " bytes allocated for the data. TripId="
                        + (trip != null ? trip.getId() : "")
                        + " routeId="
                        + (trip != null ? trip.getRouteId() : "")
                        // Would like to get the route short name from the trip
                        // but that requires Core to be read in, which can't be
                        // don't when processing GTFS data.
                        // + " routeShortName="
                        // + (trip!=null ? trip.getRouteShortName() : "")
                        + ". You most likely need to set the "
                        + "-maxTravelTimeSegmentLength command line option to "
                        + "a larger value than than the default of 200m.";
                logger.error(msg);

                // Since this could be a really problematic issue, throw an error
                throw new ArrayIndexOutOfBoundsException(msg);
            }
        }

        this.configRev = configRev;
        this.travelTimesRev = travelTimesRev;
        this.stopPathId = stopPathId;
        this.travelTimeSegmentLength = (float) travelTimeSegmentDistance;
        this.travelTimesMsec = (ArrayList<Integer>) travelTimesMsec;
        this.stopTimeMsec = stopTimeMsec;
        this.daysOfWeekOverride = (short) daysOfWeekOverride;
        this.howSet = howSet;
    }

    /** Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected TravelTimesForStopPath() {
        this.configRev = -1;
        this.travelTimesRev = -1;
        this.stopPathId = null;
        this.travelTimeSegmentLength = Float.NaN;
        this.travelTimesMsec = null;
        this.stopTimeMsec = -1;
        this.daysOfWeekOverride = -1;
        this.howSet = HowSet.SCHED;
    }

    /**
     * Creates a new object. Useful for when need to copy a schedule based travel time. By having a
     * copy can erase the original one when done with the travel time rev, without deleting this new
     * one.
     *
     * @param newTravelTimesRev The new travel times rev to use for the clone
     * @return
     */
    public TravelTimesForStopPath clone(int newTravelTimesRev) {
        return new TravelTimesForStopPath(
                configRev,
                newTravelTimesRev,
                stopPathId,
                travelTimeSegmentLength,
                travelTimesMsec,
                stopTimeMsec,
                daysOfWeekOverride,
                howSet,
                null);
    }

    /**
     * For when the travelTimesMsec are most important element. Lists the travelTimesMsec first.
     *
     * @return
     */
    public String toStringEmphasizeTravelTimes() {
        return "TTForStopPath ["
                + "stopTimeMsec="
                + stopTimeMsec
                + ", travelTimeMsec="
                + getStopPathTravelTimeMsec()
                + ", travelTimesMsec="
                + travelTimesMsec
                + ", stopPathId="
                + stopPathId
                + ", ttSegLen="
                + Geo.distanceFormat(travelTimeSegmentLength)
                + ", howSet="
                + howSet
                + ", ttRev="
                + travelTimesRev
                + "]";
    }

    /**
     * @return How many travel time segments there are for the stop path
     */
    public int getNumberTravelTimeSegments() {
        return travelTimesMsec.size();
    }

    /**
     * Returns total travel time for the stop path. Does not include the stop time.
     *
     * @return total travel time for the stop path in msec
     */
    public int getStopPathTravelTimeMsec() {
        int totalTravelTimeMsec = 0;
        for (Integer timeMsec : travelTimesMsec) {
            totalTravelTimeMsec += timeMsec;
        }
        return totalTravelTimeMsec;
    }

    /**
     * Returns the travel time for the specified travel time segment in msec
     *
     * @param segmentIndex
     * @return travel time for the specified travel time segment in msec
     */
    public int getTravelTimeSegmentMsec(int segmentIndex) {
        return travelTimesMsec.get(segmentIndex);
    }

    /**
     * Reads in all the travel times for the specified rev
     *
     * @param sessionFactory
     * @param configRev
     * @return
     */
    public static List<TravelTimesForStopPath> getTravelTimes(SessionFactory sessionFactory, int configRev) {
        // Sessions are not threadsafe so need to create a new one each time.
        // They are supposed to be lightweight so this should be OK.
        Session session = sessionFactory.openSession();

        // Create the query. Table name is case-sensitive!

        try (session) {
            return (List<TravelTimesForStopPath>) session
                    .createQuery("FROM TravelTimesForStopPath WHERE configRev=:configRev ")
                    .setParameter("configRev", configRev)
                    .list();
        } catch (HibernateException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /** Returns true if all travel times and dwell time are nonnegative. */
    public boolean isValid() {
        if (travelTimesMsec != null) {
            for (int time : travelTimesMsec) {
                if (time < 0) return false;
            }
        }
        return stopTimeMsec >= 0;
    }
}
