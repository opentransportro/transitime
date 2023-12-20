/* (C)2023 */
package org.transitclock.core.domain;

import java.util.Date;

/**
 * For persisting a Departure time.
 *
 * @author SkiBu Smith
 */
public class Departure extends ArrivalDeparture {

    public Departure(
            int configRev,
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int stopPathIndex,
            Date freqStartTime) {
        super(configRev, vehicleId, time, avlTime, block, tripIndex, stopPathIndex, false, freqStartTime);
    }
}
