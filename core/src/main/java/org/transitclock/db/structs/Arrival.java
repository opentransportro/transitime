/* (C)2023 */
package org.transitclock.db.structs;

import java.util.Date;
import javax.persistence.Entity;

/**
 * For persisting an Arrival time.
 *
 * @author SkiBu Smith
 */
@Entity
public class Arrival extends ArrivalDeparture {

    // Needed because Hibernate requires objects to be serializable
    private static final long serialVersionUID = 5438246244164457207L;

    /********************** Member Functions **************************/

    /**
     * Simple constructor
     *
     * @param vehicleId
     * @param time
     * @param avlTime So can match arrival to the AVL report that generated it
     * @param block
     * @param tripIndex
     * @param stopPathIndex
     */
    public Arrival(
            String vehicleId, Date time, Date avlTime, Block block, int tripIndex, int pathIndex, Date freqStartTime) {
        super(vehicleId, time, avlTime, block, tripIndex, pathIndex, true, freqStartTime); // isArrival
    }

    public Arrival(
            int configRev,
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int pathIndex,
            Date freqStartTime) {
        super(configRev, vehicleId, time, avlTime, block, tripIndex, pathIndex, true, freqStartTime); // isArrival
    }

    /**
     * Hibernate always wants a no-arg constructor. Made private since it shouldn't normally be
     * used.
     */
    @SuppressWarnings("unused")
    private Arrival() {
        super();
    }

    /**
     * A copy constructor that creates a new Arrival by copying this one but using the newTime. This
     * method is needed to update a time given that the class has all final elements.
     *
     * @param newTime
     * @return The newly constructed Arrival with the new time.
     */
    public Arrival withUpdatedTime(Date newTime) {
        return new Arrival(
                getVehicleId(),
                newTime,
                getAvlTime(),
                getBlock(),
                getTripIndex(),
                getStopPathIndex(),
                this.getFreqStartTime());
    }
}
