/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.transitclock.gtfs.DbConfig;

import java.util.Date;

/**
 * For persisting a Departure time.
 *
 * @author SkiBu Smith
 */
@Entity
@DiscriminatorValue(value = "DEPARTURE")
public class Departure extends ArrivalDeparture {

    public Departure(
            int configRev,
            String vehicleId,
            Date time,
            Date avlTime,
            Block block,
            int tripIndex,
            int stopPathIndex,
            Date freqStartTime,
            DbConfig dbConfig) {
        super(configRev, vehicleId, time, avlTime, block, tripIndex, stopPathIndex, false, freqStartTime, dbConfig); // isArrival
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
