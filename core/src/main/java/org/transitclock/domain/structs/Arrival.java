/* (C)2023 */
package org.transitclock.domain.structs;

import java.util.Date;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.Time;

/**
 * For persisting an Arrival time.
 *
 * @author SkiBu Smith
 */
@Entity
@DiscriminatorValue(value = "ARRIVAL")
public class Arrival extends ArrivalDeparture {

    public Arrival(
            int configRev,
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int pathIndex,
            Date freqStartTime,
            DbConfig dbConfig) {
        super(configRev, vehicleId, time, avlTime, block, tripIndex, pathIndex, true, freqStartTime, dbConfig); // isArrival
    }

    /**
     * Hibernate always wants a no-arg constructor. Made private since it shouldn't normally be
     * used.
     */
    @SuppressWarnings("unused")
    protected Arrival() {
        super();
    }

    /**
     * A copy constructor that creates a new Arrival by copying this one but using the newTime. This
     * method is needed to update a time given that the class has all final elements.
     *
     * @param newTime
     * @return The newly constructed Arrival with the new time.
     */
    public Arrival withUpdatedTime(Date newTime, DbConfig dbConfig) {
        return new Arrival(
                dbConfig.getConfigRev(),
                getVehicleId(),
                newTime,
                getAvlTime(),
                getBlock(),
                getTripIndex(),
                getStopPathIndex(),
                this.getFreqStartTime(),
                dbConfig);
    }
}
