/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Date;

/**
 * For persisting a Departure time.
 *
 * @author SkiBu Smith
 */
@Entity
@DiscriminatorValue(value = "DEPARTURE")
public class Departure extends ArrivalDeparture {
    /**
     * Simple constructor
     *
     * @param vehicleId
     * @param time
     * @param avlTime So can match arrival to the AVL report that generated it
     * @param blockId
     * @param stopId
     * @param tripId
     * @param tripIndex
     * @param stopPathIndex
     */
    public Departure(
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int stopPathIndex,
            Date freqStartTime) {
        super(vehicleId, time, avlTime, block, tripIndex, stopPathIndex, false, freqStartTime); // isArrival
    }

    public Departure(
            int configRev,
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int stopPathIndex,
            Date freqStartTime) {
        super(configRev, vehicleId, time, avlTime, block, tripIndex, stopPathIndex, false, freqStartTime); // isArrival
    }

    /**
     * Hibernate always wants a no-arg constructor. Made private since it shouldn't normally be
     * used.
     */
    @SuppressWarnings("unused")
    protected Departure() {
        super();
    }
}
