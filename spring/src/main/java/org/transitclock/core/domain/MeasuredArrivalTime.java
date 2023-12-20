/* (C)2023 */
package org.transitclock.core.domain;

import lombok.*;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * For storing a measured arrival time so that can see if measured arrival time via GPS is accurate.
 *
 * @author Michael Smith
 */
@Data
@Document(collection = "MeasuredArrivalTimes")
public class MeasuredArrivalTime implements Serializable {
    @Data
    @AllArgsConstructor
    public static class Key {
        @Indexed(name = "MeasuredArrivalTimesIndex")
        private final Date time;

        private String stopId;
    }
    @Id
    @Delegate
    private Key key;

    private String routeId;

    private String routeShortName;

    private String directionId;

    private String headsign;

    public MeasuredArrivalTime(Date time, String stopId, String routeId,
                               String routeShortName, String directionId, String headsign) {
        this.key = new Key(time, stopId);
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.directionId = directionId;
        this.headsign = headsign;
    }
}
