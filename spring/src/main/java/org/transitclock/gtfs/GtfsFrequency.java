/* (C)2023 */
package org.transitclock.gtfs;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvBase;

@ToString
@Getter
public class GtfsFrequency extends CsvBase {

    private final String tripId;
    private final int startTime;
    private final int endTime;
    /**
     * -- GETTER --
     *  Value of 0 means that there is no predetermined frequency. The vehicles will simply run when
     *  they run.
     *
     * @return
     */
    private final int headwaySecs;
    private final Boolean exactTimes;

    /**
     * Creates a GtfsFrequency object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsFrequency(CSVRecord record, boolean supplemental, String fileName) {
        super(record, supplemental, fileName);

        tripId = getRequiredValue(record, "trip_id");
        startTime = Time.parseTimeOfDay(getRequiredValue(record, "start_time"));
        endTime = Time.parseTimeOfDay(getRequiredValue(record, "end_time"));
        headwaySecs = Integer.parseInt(getRequiredValue(record, "headway_secs"));
        exactTimes = getOptionalBooleanValue(record, "exact_times");
    }

}
