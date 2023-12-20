/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * For storing events associated with predictions into log file and into database. The resulting
 * information can be mapped out for finding problems in prediction methods. Based on VehicleEvents
 * class.
 *
 * @author SkiBu Smith, Sean Óg Crudden
 */
@EqualsAndHashCode
@ToString
@Getter
@Document(collection = "PredictionEvents")
public class PredictionEvent implements Serializable {
    // Some standard prediciton event types
    public static final String PREDICTION_VARIATION = "Prediction variation";
    public static final String TRAVELTIME_EXCEPTION = "Travel time exception";


    @Data
    public static class Key {
        @Indexed(name = "PredictionEventsTimeIndex")
        // System time of the event.
        private final Date time;

        // Important for understanding context of issue
        private final String vehicleId;

        // Short descriptor of event. Not using an enumerator because don't
        // want to have to change this code every time a new event type is
        // created. It is an @Id because several events for a vehicle might
        // happen with the same exact timestamp.
        private final String eventType;
    }

    @Id
    @Delegate
    private final Key key;
    // AVL time of the event. Should correspond to last AVL report time so that
    // can join with AVL report to get more info if necessary.
    private final Date avlTime;

    private final String description;

    // Latitude/longitude of vehicle when event occurred. Though this could
    // be obtained by joining with corresponding AvlReport from db having
    // the lat/lon here makes it much easier to do things like display
    // events on a map using only a simple query.
    private final Location location;

    // Nice for providing context. Allows for query so can see all events
    // for a route.
    private final String routeId;

    // Nice for providing context.
    // routeShortName is included because for some agencies the
    // route_id changes when there are schedule updates. But the
    // routeShortName is more likely to stay consistent. Therefore
    // it is better for when querying for arrival/departure data
    // over a timespan.
    private final String routeShortName;

    // Nice for providing context.
    private final String blockId;

    // Nice for providing context.
    private final String serviceId;

    // Nice for providing context.
    private final String tripId;

    // Nice for providing context.
    private final String stopId;

    private final String arrivalstopid;

    private final String departurestopid;

    private final String referenceVehicleId;

    private final Date arrivalTime;

    private final Date departureTime;

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
        this.key = new Key(time, vehicleId, eventType);
        this.avlTime = avlTime;
        this.description = description;
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
}
