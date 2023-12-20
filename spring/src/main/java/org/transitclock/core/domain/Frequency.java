/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.gtfs.GtfsFrequency;

import java.io.Serializable;

/**
 * Contains data from the frequencies.txt GTFS file. This class is for reading/writing that data to
 * the db.
 */
@ToString
@Getter
@EqualsAndHashCode
@Document(collection = "Frequencies")
public class Frequency implements Serializable {

    @Data
    public static class Key {
        private final int configRev;

        private final String tripId;

        private final int startTime;
    }

    @Id
    @Delegate
    private final Key key;

    private final int endTime;

    private final int headwaySecs;

    /**
     * The exact_times field determines if frequency-based trips should be exactly scheduled based
     * on the specified headway information. Valid values for this field are:
     *
     * <ul>
     *   <li>0 or (empty) - Frequency-based trips are not exactly scheduled. This is the default
     *       behavior. 1 - Frequency-based trips are exactly scheduled. For a frequencies.txt row,
     *       trips are scheduled starting with trip_start_time = start_time + x * headway_secs for
     *       all x in (0, 1, 2, ...) where trip_start_time < end_time.
     *   <li>The value of exact_times must be the same for all frequencies.txt rows with the same
     *       trip_id. If exact_times is 1 and a frequencies.txt row has a start_time equal to
     *       end_time, no trip must be scheduled. When exact_times is 1, care must be taken to
     *       choose an end_time value that is greater than the last desired trip start time but less
     *       than the last desired trip start time + headway_secs.
     * </ul>
     */
    private final boolean exactTimes;

    /**
     * Constructor
     *
     * @param configRev
     * @param gtfsFrequency
     */
    public Frequency(int configRev, GtfsFrequency gtfsFrequency) {
        this.key = new Key(configRev, gtfsFrequency.getTripId(), gtfsFrequency.getStartTime());
        this.endTime = gtfsFrequency.getEndTime();
        this.headwaySecs = gtfsFrequency.getHeadwaySecs();
        this.exactTimes = (gtfsFrequency.getExactTimes() != null ? gtfsFrequency.getExactTimes() : false);
    }
}
