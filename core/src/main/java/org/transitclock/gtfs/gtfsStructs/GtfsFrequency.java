/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.Time;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS frequencies object.
 *
 * @author SkiBu Smith
 */
public class GtfsFrequency extends CsvBase {

    private final String tripId;
    private final int startTime;
    private final int endTime;
    private final int headwaySecs;
    private final Boolean exactTimes;

    /********************** Member Functions **************************/

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

    public String getTripId() {
        return tripId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    /**
     * Value of 0 means that there is no predetermined frequency. The vehicles will simply run when
     * they run.
     *
     * @return
     */
    public int getHeadwaySecs() {
        return headwaySecs;
    }

    public Boolean getExactTimes() {
        return exactTimes;
    }

    @Override
    public String toString() {
        return "GtfsFrequency ["
                + "lineNumber="
                + lineNumber
                + ", tripId="
                + tripId
                + ", startTime="
                + startTime
                + ", endTime="
                + endTime
                + ", headwaySecs="
                + headwaySecs
                + ", exactTimes="
                + exactTimes
                + "]";
    }
}
