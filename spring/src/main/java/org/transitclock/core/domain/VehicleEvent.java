/* (C)2023 */
package org.transitclock.core.domain;

import lombok.*;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * For storing events associated with vehicles into log file and into database. Used for situations
 * such as vehicles becoming predictable or unpredictable and specifying why. The resulting
 * information can be mapped out for finding problems.
 *
 * @author SkiBu Smith
 */
@EqualsAndHashCode
@ToString
@Getter @Setter
@Document
//@Table(
//        name = "VehicleEvents",
//        indexes = {@Index(name = "VehicleEventsTimeIndex", columnList = "time")})
public class VehicleEvent implements Serializable {

    @Data
    public static class Key {
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

    // The new state of the vehicle.
    // Using boolean instead of Boolean because it is a required element
    // and must therefore not be null.
    private final boolean predictable;

    // Whether this event caused the vehicle to become unpredictable. This
    // is specifically to record transitions from predictable to
    // unpredictable because a common use of VehicleEvents is to find
    // out specifically why a vehicle became unpredictable.
    // Using boolean instead of Boolean because it is a required element
    // and must therefore not be null.
    private final boolean becameUnpredictable;

    // If event was initiated by a supervisor, such as logging out
    // a vehicle, then the login for the supervisor should also
    // be stored.
    private final String supervisor;

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
        this.key = new VehicleEvent.Key(time, vehicleId, eventType);
        this.avlTime = avlTime;
        this.description = description;
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
}
