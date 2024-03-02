/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.transitclock.core.avl.time.TemporalMatch;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.SystemTime;

/**
 * For storing events associated with vehicles into log file and into database. Used for situations
 * such as vehicles becoming predictable or unpredictable and specifying why. The resulting
 * information can be mapped out for finding problems.
 *
 * @author SkiBu Smith
 */
@Immutable
@Entity
@Slf4j
@DynamicUpdate
@Getter @Setter @ToString
@Table(
    name = "vehicle_events",
    indexes = {
        @Index(name = "VehicleEventsTimeIndex", columnList = "time")
    }
)
public class VehicleEvent implements Serializable {

    // Some standard event types
    public static final String PREDICTABLE = "Predictable";
    public static final String TIMEOUT = "Timeout";
    public static final String NO_MATCH = "No match";
    public static final String NO_PROGRESS = "No progress";
    public static final String DELAYED = "Delayed";
    public static final String END_OF_BLOCK = "End of block";
    public static final String LEFT_TERMINAL_EARLY = "Left terminal early";
    public static final String LEFT_TERMINAL_LATE = "Left terminal late";
    public static final String NOT_LEAVING_TERMINAL = "Not leaving terminal";
    public static final String ASSIGNMENT_GRABBED = "Assignment Grabbed";
    public static final String ASSIGNMENT_CHANGED = "Assignment Changed";
    public static final String AVL_CONFLICT = "AVL Conflict";
    public static final String PREDICTION_VARIATION = "Prediction variation";

    // System time of the event.
    @Id
    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date time;

    // Important for understanding context of issue
    @Id
    @Column(name = "vehicle_id", length = 60)
    private final String vehicleId;

    // Short descriptor of event. Not using an enumerator because don't
    // want to have to change this code every time a new event type is
    // created. It is an @Id because several events for a vehicle might
    // happen with the same exact timestamp.
    @Id
    @Column(name = "event_type", length = 60)
    private final String eventType;

    // AVL time of the event. Should correspond to last AVL report time so that
    // can join with AVL report to get more info if necessary.
    @Column(name = "avl_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date avlTime;

    // A more verbose textual description of the event
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    @Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
    private final String description;

    // The new state of the vehicle.
    // Using boolean instead of Boolean because it is a required element
    // and must therefore not be null.
    @Column(name = "predictable")
    private final boolean predictable;

    // Whether this event caused the vehicle to become unpredictable. This
    // is specifically to record transitions from predictable to
    // unpredictable because a common use of VehicleEvents is to find
    // out specifically why a vehicle became unpredictable.
    // Using boolean instead of Boolean because it is a required element
    // and must therefore not be null.
    @Column(name = "became_unpredictable")
    private final boolean becameUnpredictable;

    // If event was initiated by a supervisor, such as logging out
    // a vehicle, then the login for the supervisor should also
    // be stored.
    @Column(name = "supervisor", length = 60)
    private final String supervisor;

    // Latitude/longitude of vehicle when event occurred. Though this could
    // be obtained by joining with corresponding AvlReport from db having
    // the lat/lon here makes it much easier to do things like display
    // events on a map using only a simple query.
    @Embedded
    private final Location location;

    // Nice for providing context. Allows for query so can see all events
    // for a route.
    @Column(name = "route_id", length = 60)
    private final String routeId;

    // Nice for providing context.
    // routeShortName is included because for some agencies the
    // route_id changes when there are schedule updates. But the
    // routeShortName is more likely to stay consistent. Therefore
    // it is better for when querying for arrival/departure data
    // over a timespan.
    @Column(name = "route_short_name", length = 60)
    private final String routeShortName;

    // Nice for providing context.
    @Column(name = "block_id", length = 60)
    private final String blockId;

    // Nice for providing context.
    @Column(name = "service_id", length = 60)
    private final String serviceId;

    // Nice for providing context.
    @Column(name = "trip_id", length = 60)
    private final String tripId;

    // Nice for providing context.
    @Column(name = "stop_id", length = 60)
    private final String stopId;


    private VehicleEvent(
            Date time,
            Date avlTime,
            String vehicleId,
            String eventType,
            String description,
            boolean predictable,
            boolean becameUnpredictable,
            String supervisor,
            Location location,
            String routeId,
            String routeShortName,
            String blockId,
            String serviceId,
            String tripId,
            String stopId) {
        this.time = time;
        this.avlTime = avlTime;
        this.vehicleId = vehicleId;
        this.eventType = eventType;
        this.description = description.length() <= MAX_DESCRIPTION_LENGTH
                ? description
                : description.substring(0, MAX_DESCRIPTION_LENGTH);
        this.predictable = predictable;
        this.becameUnpredictable = becameUnpredictable;
        this.supervisor = supervisor;
        this.location = location;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.blockId = blockId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.stopId = stopId;
    }

    /**
     * A simpler way to create a VehicleEvent that gets a lot of its info from the avlReport and
     * match params. This also logs it and queues it to be stored in database. The match param can
     * be null.
     *
     * @return The VehicleEvent constructed
     */
    public VehicleEvent(
            AvlReport avlReport,
            TemporalMatch match,
            String eventType,
            String description,
            boolean predictable,
            boolean becameUnpredictable,
            String supervisor) {
        this(
                SystemTime.getDate(),
                avlReport.getDate(),
                avlReport.getVehicleId(),
                eventType,
                description,
                predictable,
                becameUnpredictable,
                supervisor,
                avlReport.getLocation(),
                match == null ? null : match.getTrip().getRouteId(),
                match == null ? null : match.getTrip().getRouteShortName(),
                match == null ? null : match.getBlock().getId(),
                match == null ? null : match.getBlock().getServiceId(),
                match == null ? null : match.getTrip().getId(),
                match == null ? null : match.getStopPath().getStopId());
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
    protected VehicleEvent() {
        this.time = null;
        this.avlTime = null;
        this.vehicleId = null;
        this.eventType = null;
        this.description = null;
        this.predictable = false;
        this.becameUnpredictable = false;
        this.supervisor = null;
        this.location = null;
        this.routeId = null;
        this.routeShortName = null;
        this.blockId = null;
        this.serviceId = null;
        this.tripId = null;
        this.stopId = null;
    }

    /**
     * Reads in all VehicleEvents from the database that were between the beginTime and endTime.
     *
     * @param agencyId  Which project getting data for
     * @param beginTime Specifies time range for query
     * @param endTime   Specifies time range for query
     * @param sqlClause Optional. Can specify an SQL clause to winnow down the data, such as "AND
     *                  routeId='71'".
     * @return
     */
    public static List<VehicleEvent> getVehicleEvents(String agencyId, Date beginTime, Date endTime, String sqlClause) {
        IntervalTimer timer = new IntervalTimer();

        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(agencyId);

        // Create the query. Table name is case sensitive and needs to be the
        // class name instead of the name of the db table.
        String hql = "FROM VehicleEvent WHERE time >= :beginDate AND time < :endDate";
        if (sqlClause != null) {
            hql += " " + sqlClause;
        }
        var query = session.createQuery(hql, VehicleEvent.class);

        // Set the parameters
        query.setParameter("beginDate", beginTime);
        query.setParameter("endDate", endTime);

        try {
            List<VehicleEvent> vehicleEvents = query.list();
            logger.debug("Getting VehicleEvents from database took {} msec", timer.elapsedMsec());
            return vehicleEvents;
        } catch (HibernateException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            // Clean things up. Not sure if this absolutely needed nor if
            // it might actually be detrimental and slow things down.
            session.close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleEvent that)) return false;
        return predictable == that.predictable && becameUnpredictable == that.becameUnpredictable && Objects.equals(time, that.time) && Objects.equals(vehicleId, that.vehicleId) && Objects.equals(eventType, that.eventType) && Objects.equals(avlTime, that.avlTime) && Objects.equals(description, that.description) && Objects.equals(supervisor, that.supervisor) && Objects.equals(location, that.location) && Objects.equals(routeId, that.routeId) && Objects.equals(routeShortName, that.routeShortName) && Objects.equals(blockId, that.blockId) && Objects.equals(serviceId, that.serviceId) && Objects.equals(tripId, that.tripId) && Objects.equals(stopId, that.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, vehicleId, eventType, avlTime, description, predictable, becameUnpredictable, supervisor, location, routeId, routeShortName, blockId, serviceId, tripId, stopId);
    }
}
