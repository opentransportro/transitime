/* (C)2023 */
package org.transitclock.gtfs.gtfsStructs;

import java.text.ParseException;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

/**
 * A GTFS calendar object.
 *
 * @author SkiBu Smith
 */
public class GtfsCalendar extends CsvBase {

    private final String serviceId;
    private final String monday;
    private final String tuesday;
    private final String wednesday;
    private final String thursday;
    private final String friday;
    private final String saturday;
    private final String sunday;
    private final String startDate;
    private final String endDate;

    /********************** Member Functions **************************/

    /**
     * Creates a GtfsCalendar object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsCalendar(CSVRecord record, boolean supplemental, String fileName) throws ParseException {
        super(record, supplemental, fileName);

        serviceId = getRequiredValue(record, "service_id");
        monday = getRequiredValue(record, "monday");
        tuesday = getRequiredValue(record, "tuesday");
        wednesday = getRequiredValue(record, "wednesday");
        thursday = getRequiredValue(record, "thursday");
        friday = getRequiredValue(record, "friday");
        saturday = getRequiredValue(record, "saturday");
        sunday = getRequiredValue(record, "sunday");

        startDate = getRequiredValue(record, "start_date");
        endDate = getRequiredValue(record, "end_date");
    }

    public GtfsCalendar(
            String serviceId,
            String monday,
            String tuesday,
            String wednesday,
            String thursday,
            String friday,
            String saturday,
            String sunday,
            String startDate,
            String endDate) {
        this.serviceId = serviceId;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getMonday() {
        return monday;
    }

    public String getTuesday() {
        return tuesday;
    }

    public String getWednesday() {
        return wednesday;
    }

    public String getThursday() {
        return thursday;
    }

    public String getFriday() {
        return friday;
    }

    public String getSaturday() {
        return saturday;
    }

    public String getSunday() {
        return sunday;
    }

    public String getStartDate() {
        return startDate;
    }

    /**
     * End of the last day of service. This means that when an end date is specified the service
     * runs for up to and including that day.
     *
     * @return
     */
    public String getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return "GtfsCalendar ["
                + "lineNumber="
                + lineNumber
                + ", serviceId="
                + serviceId
                + ", monday="
                + monday
                + ", tuesday="
                + tuesday
                + ", wednesday="
                + wednesday
                + ", thursday="
                + thursday
                + ", friday="
                + friday
                + ", saturday="
                + saturday
                + ", sunday="
                + sunday
                + ", startDate="
                + startDate
                + ", endDate="
                + endDate
                + "]";
    }
}
