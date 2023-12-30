/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.jcip.annotations.Immutable;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.core.TemporalMatch;

/**
 * For storing events associated with predictions into log file and into database. The resulting
 * information can be mapped out for finding problems in prediction methods. Based on VehicleEvents
 * class.
 *
 * @author SkiBu Smith, Sean Óg Crudden
 */
@Immutable // From jcip.annoations
@Entity
@DynamicUpdate
@EqualsAndHashCode
@ToString
@Getter
@Table(
        name = "PredictionEvents",
        indexes = {@Index(name = "PredictionEventsTimeIndex", columnList = "time")})
public class PredictionEvent implements Serializable {

    // System time of the event.
    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date time;

    // Important for understanding context of issue
    @Id
    @Column(length = 60)
    private final String vehicleId;

    // Short descriptor of event. Not using an enumerator because don't
    // want to have to change this code every time a new event type is
    // created. It is an @Id because several events for a vehicle might
    // happen with the same exact timestamp.
    @Id
    @Column(length = 60)
    private final String eventType;

    // AVL time of the event. Should correspond to last AVL report time so that
    // can join with AVL report to get more info if necessary.
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date avlTime;

    // A more verbose textual description of the event
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    @Column(length = MAX_DESCRIPTION_LENGTH)
    private final String description;

    // Latitude/longitude of vehicle when event occurred. Though this could
    // be obtained by joining with corresponding AvlReport from db having
    // the lat/lon here makes it much easier to do things like display
    // events on a map using only a simple query.
    @Embedded
    private final Location location;

    // Nice for providing context. Allows for query so can see all events
    // for a route.
    @Column(length = 60)
    private final String routeId;

    // Nice for providing context.
    // routeShortName is included because for some agencies the
    // route_id changes when there are schedule updates. But the
    // routeShortName is more likely to stay consistent. Therefore
    // it is better for when querying for arrival/departure data
    // over a timespan.
    @Column(length = 60)
    private final String routeShortName;

    // Nice for providing context.
    @Column(length = 60)
    private final String blockId;

    // Nice for providing context.
    @Column(length = 60)
    private final String serviceId;

    // Nice for providing context.
    @Column(length = 60)
    private final String tripId;

    // Nice for providing context.
    @Column(length = 60)
    private final String stopId;

    @Column(length = 60)
    private final String arrivalstopid;

    @Column(length = 60)
    private final String departurestopid;

    @Column(length = 60)
    private final String referenceVehicleId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date arrivalTime;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date departureTime;

    // Some standard prediciton event types
    public static final String PREDICTION_VARIATION = "Prediction variation";
    public static final String TRAVELTIME_EXCEPTION = "Travel time exception";

    private static final Logger logger = LoggerFactory.getLogger(PredictionEvent.class);

    /********************** Member Functions **************************/
    private PredictionEvent(
            Date time,
            Date avlTime,
            String vehicleId,
            String eventType,
            String description,
            Location location,
            String routeId,
            String routeShortName,
            String blockId,
            String serviceId,
            String tripId,
            String stopId,
            String arrivalStopId,
            String departureStopId,
            String referenceVehicleId,
            Date arrivalTime,
            Date departureTime) {
        super();
        this.time = time;
        this.avlTime = avlTime;
        this.vehicleId = vehicleId;
        this.eventType = eventType;
        this.description = description.length() <= MAX_DESCRIPTION_LENGTH
                ? description
                : description.substring(0, MAX_DESCRIPTION_LENGTH);
        this.location = location;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.blockId = blockId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.stopId = stopId;
        this.arrivalstopid = arrivalStopId;
        this.departurestopid = departureStopId;
        this.referenceVehicleId = referenceVehicleId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public static PredictionEvent create(
            Date time,
            Date avlTime,
            String vehicleId,
            String eventType,
            String description,
            Location location,
            String routeId,
            String routeShortName,
            String blockId,
            String serviceId,
            String tripId,
            String stopId,
            String arrivalStopId,
            String departureStopId,
            String referenceVehicleId,
            Date arrivalTime,
            Date departureTime) {
        PredictionEvent predictionEvent = new PredictionEvent(
                time,
                avlTime,
                vehicleId,
                eventType,
                description,
                location,
                routeId,
                routeShortName,
                blockId,
                serviceId,
                tripId,
                stopId,
                arrivalStopId,
                departureStopId,
                referenceVehicleId,
                arrivalTime,
                departureTime);

        // Log predictionEvent in log file
        logger.info(predictionEvent.toString());

        // Queue to write object to database
        Core.getInstance().getDbLogger().add(predictionEvent);

        // Return new predictionEvent
        return predictionEvent;
    }

    /**
     * A simpler way to create a VehicleEvent that gets a lot of its info from the avlReport and
     * match params. This also logs it and queues it to be stored in database. The match param can
     * be null.
     *
     * @param avlReport
     * @param match
     * @param eventType
     * @param description
     * @param predictable
     * @param becameUnpredictable
     * @param supervisor
     * @return The VehicleEvent constructed
     */
    public static PredictionEvent create(
            AvlReport avlReport,
            TemporalMatch match,
            String eventType,
            String description,
            String arrivalStopId,
            String departureStopId,
            String referenceVehicleId,
            Date arrivalTime,
            Date departureTime) {
        // Get a log of the info from the possibly null match param
        String routeId = match == null ? null : match.getTrip().getRouteId();
        String routeShortName = match == null ? null : match.getTrip().getRouteShortName();
        String blockId = match == null ? null : match.getBlock().getId();
        String serviceId = match == null ? null : match.getBlock().getServiceId();
        String tripId = match == null ? null : match.getTrip().getId();
        String stopId = match == null ? null : match.getStopPath().getStopId();

        // Create and return the VehicleEvent
        return create(
                Core.getInstance().getSystemDate(),
                avlReport.getDate(),
                avlReport.getVehicleId(),
                eventType,
                description,
                avlReport.getLocation(),
                routeId,
                routeShortName,
                blockId,
                serviceId,
                tripId,
                stopId,
                arrivalStopId,
                departureStopId,
                referenceVehicleId,
                arrivalTime,
                departureTime);
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
    protected PredictionEvent() {
        this.time = null;
        this.avlTime = null;
        this.vehicleId = null;
        this.eventType = null;
        this.description = null;
        this.location = null;
        this.routeId = null;
        this.routeShortName = null;
        this.blockId = null;
        this.serviceId = null;
        this.tripId = null;
        this.stopId = null;
        this.arrivalstopid = null;
        this.departurestopid = null;
        this.referenceVehicleId = null;
        this.departureTime = null;
        this.arrivalTime = null;
    }
}
