/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.transitclock.core.TemporalMatch;
import org.transitclock.utils.SystemTime;

/**
 * For storing events associated with predictions into log file and into database. The resulting
 * information can be mapped out for finding problems in prediction methods. Based on VehicleEvents
 * class.
 *
 * @author SkiBu Smith, Sean Óg Crudden
 */
@Immutable
@Entity
@DynamicUpdate
@Data
@Table(
    name = "prediction_events",
    indexes = {
            @Index(name = "PredictionEventsTimeIndex", columnList = "time")
    })
public class PredictionEvent implements Serializable {
    // A more verbose textual description of the event
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    // Some standard prediciton event types
    public static final String PREDICTION_VARIATION = "Prediction variation";
    public static final String TRAVELTIME_EXCEPTION = "Travel time exception";

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

    @Column(name = "description", length = MAX_DESCRIPTION_LENGTH)
    private final String description;

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

    @Column(name = "arrival_stop_id", length = 60)
    private final String arrivalstopid;

    @Column(name = "departure_stop_id", length = 60)
    private final String departurestopid;

    @Column(name = "reference_vehicle_id", length = 60)
    private final String referenceVehicleId;

    @Column(name = "arrival_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date arrivalTime;

    @Column(name = "departure_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date departureTime;


    public PredictionEvent(
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

    public PredictionEvent(
            AvlReport avlReport,
            TemporalMatch match,
            String eventType,
            String description,
            String arrivalStopId,
            String departureStopId,
            String referenceVehicleId,
            Date arrivalTime,
            Date departureTime) {
        this(SystemTime.getDate(),
                avlReport.getDate(),
                avlReport.getVehicleId(),
                eventType,
                description,
                avlReport.getLocation(),
                match == null ? null : match.getTrip().getRouteId(),
                match == null ? null : match.getTrip().getRouteShortName(),
                match == null ? null : match.getBlock().getId(),
                match == null ? null : match.getBlock().getServiceId(),
                match == null ? null : match.getTrip().getId(),
                match == null ? null : match.getStopPath().getStopId(),
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
