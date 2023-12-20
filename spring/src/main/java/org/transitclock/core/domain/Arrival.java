/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * For persisting an Arrival time.
 *
 * @author SkiBu Smith
 */
@Document
public class Arrival extends ArrivalDeparture {

    @Builder(toBuilder = true)
    public Arrival(int configRev, String vehicleId, Date time, Date avlTime,
                   Block block, int tripIndex, int pathIndex, Date freqStartTime) {
        super(configRev, vehicleId, time, avlTime, block, tripIndex, pathIndex, true, freqStartTime);
    }
}
