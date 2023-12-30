/* (C)2023 */
package org.transitclock.db.structs;

import java.util.Date;
import javax.persistence.Entity;

/**
 * For persisting a Departure time.
 *
 * @author SkiBu Smith
 */
@Entity
public class Departure extends ArrivalDeparture {

    // Needed because Hibernate requires objects to be serializable
    private static final long serialVersionUID = 8489481047642753556L;

    /********************** Member Functions **************************/

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
