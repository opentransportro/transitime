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
 * For persisting the vehicle state for the vehicle. Can be joined with AvlReport table in order to
 * get additional info for each historic AVL report.
 */
@Data
@Document(collection = "vehicleStates")
public class VehicleState implements Serializable {
    @Data
    public static class Key {
        private final String vehicleId;
        @Indexed(name = "VehicleStateAvlTimeIndex")
        private final Date avlTime;
    }

    @Id
    @Delegate
    private final Key key;

    private String blockId;

    private String tripId;

    private String tripShortName;

    private String routeId;

    private static final int ROUTE_SHORT_NAME_MAX_LENGTH = 80;

    private String routeShortName;

    // Positive means vehicle early, negative means vehicle late
    private final Integer schedAdhMsec;

    // A String representing the schedule adherence
    private static final int SCHED_ADH_MAX_LENGTH = 50;

    private final String schedAdh;

    private final Boolean schedAdhWithinBounds;

    private final Boolean isDelayed;

    private final Boolean isLayover;

    private final Boolean isPredictable;

    private final Boolean isWaitStop;

    private final Boolean isForSchedBasedPreds;


    /**
     * For making sure that members don't get a value that is longer than allowed. Truncates string
     * to maxLength if it is too long. This way won't get a db error if try to store a string that
     * is too long.
     *
     * @param original the string to possibly be truncated
     * @param maxLength max length string can have in db
     * @return possibly truncated version of the original string
     */
    private String truncate(String original, int maxLength) {
        if (original == null || original.length() <= maxLength) {
            return original;
        }

        return original.substring(0, maxLength);
    }
}
