/* (C)2023 */
package org.transitclock.db.structs;

import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of travel times for a trip. Can be shared amongst trips if the travel times are
 * similar. But need separate travel times for every trip pattern.
 *
 * @author SkiBu Smith
 */
@Entity
@Slf4j
@DynamicUpdate
@Getter
@EqualsAndHashCode
@ToString
@Table(
        name = "TravelTimesForTrips",
        indexes = {@Index(name = "TravelTimesRevIndex", columnList = "travelTimesRev")})
public class TravelTimesForTrip implements Serializable {

    // Need a generated ID because trying to share TravelTimesForStopPath
    // objects because having a separate set for each trip would be too much.
    // But can still have a few per path and trip pattern. Therefore
    // also need the generated ID since the other columns are not adequate
    // as an ID.
    @Column
    @Id
    @GeneratedValue
    private Integer id;

    // Need configRev for the configuration so that when old configurations
    // cleaned out can also easily get rid of old travel times. Note: at one
    // point tried making configRevan @Id so that the config rev is part of
    // the join table so that it would be easier to delete old config data
    // from the join table. But this cause the id member to not be declared
    // as auto_increment in the SQL for creating the table, which in turn
    // caused a strange PropertyAccessException when trying to save travel
    // times. Therefore cannot make this member an @Id and have to use fancy
    // delete with a join to clear out the join table of old data.
    @Column
    private final int configRev;

    // Each time update travel times it gets a new travel time rev. This
    // way can compare travel times with previous revisions. Probably only need
    // to keep the previous travel time rev around for comparison but by
    // using an integer for the rev all of the revs can be kept in the db
    // if desired.
    @Column
    private final int travelTimesRev;

    @Column(length = TripPattern.TRIP_PATTERN_ID_LENGTH)
    private final String tripPatternId;

    // So know which trip these travel times were created for. Useful
    // for logging statements. Used when creating travel times based
    // on schedule.
    @Column(length = 60)
    private final String tripCreatedForId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TravelTimesForTrip_to_TravelTimesForPath_joinTable")
    @Cascade({CascadeType.SAVE_UPDATE})
    @OrderColumn(name = "listIndex")
    private final List<TravelTimesForStopPath> travelTimesForStopPaths = new ArrayList<>();

    public TravelTimesForTrip(int configRev, int travelTimesRev, Trip trip) {
        this.configRev = configRev;
        this.travelTimesRev = travelTimesRev;
        this.tripPatternId = trip.getTripPattern().getId();
        this.tripCreatedForId = trip.getId();
    }

    /** Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected TravelTimesForTrip() {
        this.configRev = -1;
        this.travelTimesRev = -1;
        this.tripPatternId = null;
        this.tripCreatedForId = null;
    }

    /**
     * For when creating a new TravelTimesForTrip. This method is for adding each
     * TravelTimesForStopPath.
     *
     * @param travelTimesForPath
     */
    public void add(TravelTimesForStopPath travelTimesForPath) {
        travelTimesForStopPaths.add(travelTimesForPath);
    }

    /**
     * Deletes data from the TravelTimesForTrip and the
     * TravelTimesForTrip_to_TravelTimesForPath_jointable.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        int totalRowsUpdated = 0;

        // Delete configRev data from TravelTimesForTrip_to_TravelTimesForPath_jointable.
        // This needs to work with at least mySQL and PostgreSQL but they are different.
        // This means that cannot use an INNER JOIN as part of the delete since the
        // syntax for inner joins is different for the two databases. Therefore need to
        // use the IN statement with a SELECT clause.
        int rowsUpdated = session
                .createNativeQuery("DELETE FROM TravelTimesForTrip_to_TravelTimesForPath_joinTable WHERE TravelTimesForTrip_id IN   (SELECT id FROM TravelTimesForTrips WHERE configRev=" + configRev +")")
                .executeUpdate();
        logger.info(
                "Deleted {} rows from TravelTimesForTrip_to_TravelTimesForPath_joinTable for configRev={}",
                rowsUpdated,
                configRev);
        totalRowsUpdated += rowsUpdated;

        // Delete configRev data from TravelTimesForStopPaths
        rowsUpdated = session
                .createNativeQuery("DELETE FROM TravelTimesForStopPaths WHERE configRev=" + configRev)
                .executeUpdate();
        logger.info("Deleted {} rows from TravelTimesForStopPaths for " + "configRev={}", rowsUpdated, configRev);
        totalRowsUpdated += rowsUpdated;

        // Delete configRev data from TravelTimesForTrips
        rowsUpdated = session
                .createNativeQuery("DELETE FROM TravelTimesForTrips WHERE configRev=" + configRev)
                .executeUpdate();
        logger.info("Deleted {} rows from TravelTimesForTrips for configRev={}", rowsUpdated, configRev);
        totalRowsUpdated += rowsUpdated;

        return totalRowsUpdated;
    }

    /**
     * Returns Map keyed by tripPatternId of Lists of TravelTimesForTrip. Since there are usually
     * multiple trips per trip pattern the Map contains a List of TravelTimesForTrip instead of just
     * a single one.
     *
     * @param session
     * @param travelTimesRev
     * @return Map keyed by tripPatternId of Lists of TripPatterns
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<TravelTimesForTrip>> getTravelTimesForTrips(Session session, int travelTimesRev)
            throws HibernateException {
        logger.info("Reading TravelTimesForTrips for travelTimesRev={} ...", travelTimesRev);

        JPAQuery<TravelTimesForTrip> query = new JPAQuery<>(session);
        var qentity = QTravelTimesForTrip.travelTimesForTrip;
        List<TravelTimesForTrip> allTravelTimes = query.from(qentity)
                .where(qentity.travelTimesRev.eq(travelTimesRev))
                .distinct()
                .fetch();

        logger.info("Putting travel times into map...");

        // Now create the map and return it
        Map<String, List<TravelTimesForTrip>> map = new HashMap<>();
        for (TravelTimesForTrip travelTimes : allTravelTimes) {
            // Get the List to add the travelTimes to
            String tripPatternId = travelTimes.getTripPatternId();
            List<TravelTimesForTrip> listForTripPattern = map.computeIfAbsent(tripPatternId, k -> new ArrayList<>());

            // Add the travelTimes to the List
            listForTripPattern.add(travelTimes);
        }

        logger.info("Done putting travel times into map.");

        // Return the map containing all the travel times
        return map;
    }

    /**
     * Returns true if every single stop path travel time is schedule based.
     *
     * @return
     */
    public boolean purelyScheduleBased() {
        for (TravelTimesForStopPath times : travelTimesForStopPaths) {
            if (!times.getHowSet().isScheduleBased()) return false;
        }

        // All of them travel times are schedule based so return true
        return true;
    }

    /** Returns true if all stop paths are valid. */
    public boolean isValid() {
        for (TravelTimesForStopPath times : travelTimesForStopPaths) {
            if (!times.isValid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * For output list of travel times for stop paths. Uses newlines to put each one on separate
     * line so that easier to read.
     *
     * @param travelTimesForStopPaths
     * @return
     */
    private static String travelTimesToStringWithNewlines(List<TravelTimesForStopPath> travelTimesForStopPaths) {
        StringBuilder results = new StringBuilder();
        for (TravelTimesForStopPath travelTimesForSP : travelTimesForStopPaths) {
            results.append("     ")
                    .append(travelTimesForSP.toStringEmphasizeTravelTimes())
                    .append("\n");
        }
        return results.toString();
    }

    /**
     * Similar to toString() but puts each travelTimesForStopPath on a separate line to try to make
     * the output more readable.
     *
     * @return
     */
    public String toStringWithNewlines() {
        return "TravelTimesForTrip ["
                + "configRev="
                + configRev
                + ", travelTimesRev="
                + travelTimesRev
                + ", tripPatternId="
                + tripPatternId
                + ", tripCreatedForId="
                + tripCreatedForId
                + ", travelTimesForStopPaths=\n"
                + travelTimesToStringWithNewlines(travelTimesForStopPaths)
                + "]";
    }

    public TravelTimesForStopPath getTravelTimesForStopPath(int index) {
        return travelTimesForStopPaths.get(index);
    }

    /**
     * @return Number of stopPaths in trip
     */
    public int numberOfStopPaths() {
        return travelTimesForStopPaths.size();
    }
}
