/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.hibernate.classic.Lifecycle;
import org.transitclock.core.TemporalMatch;
import org.transitclock.core.VehicleState;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.utils.IntervalTimer;

/**
 * For persisting the match for the vehicle. This data is later used for determining expected travel
 * times. The key/IDs for the table are vehicleId and the AVL avlTime so that the Match data can
 * easily be joined with AvlReport data to get additional information.
 *
 * <p>Serializable since Hibernate requires such.
 *
 * <p>Implements Lifecycle so that can have the onLoad() callback be called when reading in data so
 * that can intern() member strings. In order to do this the String members could not be declared as
 * final since they are updated after the constructor is called.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Immutable
@Entity
@DynamicUpdate
@Getter @Setter @ToString
@Table(
    name = "matches",
    indexes = {@Index(name = "AvlTimeIndex", columnList = "avl_time")}
)
public class Match implements Lifecycle, Serializable {

    // vehicleId is an @Id since might get multiple AVL reports
    // for different vehicles with the same avlTime but need a unique
    // primary key.
    @Id
    @Column(name = "vehicle_id", length = 60)
    private String vehicleId;

    // Need to use columnDefinition to explicitly specify that should use
    // fractional seconds. This column is an Id since shouldn't get two
    // AVL reports for the same vehicle for the same avlTime.
    @Id
    @Column(name = "avl_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date avlTime;

    // So that know which configuration was being used when this data point
    // was created
    @Id
    @Column(name = "config_rev")
    private final int configRev;

    // So that know which service type was used when this data point was created
    @Column(name = "service_id")
    private String serviceId;

    // Not truly needed because currently using only trip info for generating
    // travel times, which is the main use of Match data from the db.
    @Column(name = "block_id", length = 60)
    private String blockId;

    // Creating travel times on a trip by trip basis so this element is
    // important.
    @Column(name = "trip_id", length = 60)
    private String tripId;

    // Important because generating travel times on a per stop path basis
    @Column(name = "stop_path_index")
    private final int stopPathIndex;

    // Not currently needed. Added for possible future uses of Match
    @Column(name = "segment_index")
    private final int segmentIndex;

    // Not currently needed. Added for possible future uses of Match
    @Column(name = "distance_along_segment")
    private final float distanceAlongSegment;

    // The distanceAlongStopPath is the important item since travel times are
    // based on dividing up the stop path into travel time paths. These travel
    // time paths are independent of the path segments.
    @Column(name = "distance_along_stop_path")
    private final float distanceAlongStopPath;

    // Whether vehicle is considered to be at a stop. Especially useful so
    // can filter out atStop matches when determining travel times since
    // instead using arrival/departure times for that situation.
    @Column(name = "at_stop")
    private final boolean atStop;

    public Match(VehicleState vehicleState, int configRev) {
        this.vehicleId = vehicleState.getVehicleId();
        this.avlTime = vehicleState.getAvlReport().getDate();
        this.configRev = configRev;
        this.serviceId = vehicleState.getBlock().getServiceId();
        this.blockId = vehicleState.getBlock().getId();

        TemporalMatch lastMatch = vehicleState.getMatch();
        this.tripId = lastMatch != null ? lastMatch.getTrip().getId() : null;
        this.stopPathIndex = lastMatch != null ? lastMatch.getStopPathIndex() : -1;
        this.segmentIndex = lastMatch != null ? lastMatch.getSegmentIndex() : -1;
        this.distanceAlongSegment = (float) (lastMatch != null ? lastMatch.getDistanceAlongSegment() : 0.0);
        this.distanceAlongStopPath = (float) (lastMatch != null ? lastMatch.getDistanceAlongStopPath() : 0.0);
        this.atStop = vehicleState.getMatch().isAtStop();
    }

    /**
     * Hibernate requires a no-args constructor for reading data. So this is an experiment to see
     * what can be done to satisfy Hibernate but still have an object be immutable. Since this
     * constructor is only intended to be used by Hibernate is is declared protected, since that
     * still works. That way others won't accidentally use this inappropriate constructor. And yes,
     * it is peculiar that even though the members in this class are declared final that Hibernate
     * can still create an object using this no-args constructor and then set the fields. Not quite
     * as "final" as one might think. But at least it works.
     */
    protected Match() {
        this.vehicleId = null;
        this.avlTime = null;
        this.configRev = -1;
        this.serviceId = null;
        this.blockId = null;
        this.tripId = null;
        this.stopPathIndex = -1;
        this.segmentIndex = -1;
        this.distanceAlongSegment = Float.NaN;
        this.distanceAlongStopPath = Float.NaN;
        this.atStop = false;
    }

    /**
     * Allows batch retrieval of Match data from database. This is likely the best way to read in
     * large amounts of data.
     *
     * @param projectId
     * @param beginTime
     * @param endTime
     * @param sqlClause The clause is added to the SQL for retrieving the arrival/departures. Useful
     *     for ordering the results. Can be null.
     * @param firstResult
     * @param maxResults
     * @return
     */
    public static List<Match> getMatchesFromDb(
            String projectId,
            Date beginTime,
            Date endTime,
            String sqlClause,
            final Integer firstResult,
            final Integer maxResults) {
        IntervalTimer timer = new IntervalTimer();

        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(projectId);

        // Create the query. Table name is case sensitive and needs to be the
        // class name instead of the name of the db table.
        String hql = "FROM Match WHERE avlTime between :beginDate AND :endDate";
        if (sqlClause != null)
            hql += " " + sqlClause;
        var query = session.createQuery(hql, Match.class);

        // Set the parameters for the query
        query.setParameter("beginDate", beginTime);
        query.setParameter("endDate", endTime);

        if (firstResult != null) {
            // Only get a batch of data at a time
            query.setFirstResult(firstResult);
        }
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }

        try {
            List<Match> matches = query.list();
            logger.debug("Getting matches from database took {} msec", timer.elapsedMsec());
            return matches;
        } catch (HibernateException e) {
            // Log error to the Core logger
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            // Clean things up. Not sure if this absolutely needed nor if
            // it might actually be detrimental and slow things down.
            session.close();
        }
    }

    public static Long getMatchesCountFromDb(String projectId, Date beginTime, Date endTime, String sqlClause) {
        // Create the query. Table name is case sensitive and needs to be the
        // class name instead of the name of the db table.
        String hql = "select count(*) FROM Match WHERE avlTime >= :beginDate AND avlTime < :endDate";
        if (sqlClause != null)
            hql += " " + sqlClause;

        // Get the database session. This is supposed to be pretty light weight
        try (Session session = HibernateUtils.getSession(projectId)) {
            var query = session.createQuery(hql, Long.class)
                .setParameter("beginDate", beginTime)
                .setParameter("endDate", endTime);

            return query.uniqueResult();
        } catch (HibernateException e) {
            // Log error to the Core logger
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public Date getDate() {
        return avlTime;
    }

    public long getTime() {
        return avlTime.getTime();
    }

    /**
     * Callback due to implementing Lifecycle interface. Used to compact string members by interning
     * them.
     */
    @Override
    public void onLoad(Session s, Object id) throws CallbackException {
        if (vehicleId != null) vehicleId = vehicleId.intern();
        if (tripId != null) tripId = tripId.intern();
        if (blockId != null) blockId = blockId.intern();
        if (serviceId != null) serviceId = serviceId.intern();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match match)) return false;
        return configRev == match.configRev && stopPathIndex == match.stopPathIndex && segmentIndex == match.segmentIndex && Float.compare(distanceAlongSegment, match.distanceAlongSegment) == 0 && Float.compare(distanceAlongStopPath, match.distanceAlongStopPath) == 0 && atStop == match.atStop && Objects.equals(vehicleId, match.vehicleId) && Objects.equals(avlTime, match.avlTime) && Objects.equals(serviceId, match.serviceId) && Objects.equals(blockId, match.blockId) && Objects.equals(tripId, match.tripId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, avlTime, configRev, serviceId, blockId, tripId, stopPathIndex, segmentIndex, distanceAlongSegment, distanceAlongStopPath, atStop);
    }
}
