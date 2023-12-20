/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * For storing monitoring events into database. By storing the monitoring events one can look back
 * and easily see how frequent events are, when the start and stop, etc.
 */
@Data
@Document(collection = "MonitoringEvents")
public class MonitoringEvent implements Serializable {
    // The long message associated with the monitoring
    private static final int MAX_MESSAGE_LENGTH = 512;

    @Data
    public static class Key {
        // The time the event occurred
        @Indexed(name = "MonitoringEventsTimeIndex")
        private final Date time;
        // String describing type of event, such as "System CPU"
        private final String type;
    }

    @Id
    @Delegate
    private final Key key;

    // Whether monitoring is now triggered
    private final boolean triggered;

    private final String message;

    // The value that caused monitoring to be triggered or untriggered.
    // For some monitors they are triggered when this value is too
    // high. For others though it can be when the value is too low.
    private final double value;

    private MonitoringEvent(Date time, String type, boolean triggered, String message, double value) {
        this.key = new Key(time, type);
        this.triggered = triggered;
        // Since message to be stored in db and don't know how long it might
        // be make sure it is not too long so that don't get db errors.
        this.message = message.length() <= MAX_MESSAGE_LENGTH ? message : message.substring(0, MAX_MESSAGE_LENGTH);
        // Note: MySQL can't handle double values of Double.NaN. Get an exception
        // "java.sql.SQLException: 'NaN' is not a valid numeric or approximate numeric value".
        // So if value is a NaN use 0.0 instead. Works fine with Postgres though.
        this.value = Double.isNaN(value) ? 0.0 : value;
    }
}
