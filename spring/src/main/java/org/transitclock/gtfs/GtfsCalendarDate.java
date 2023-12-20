/* (C)2023 */
package org.transitclock.gtfs;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBase;

import java.text.ParseException;

@ToString
@Getter
public class GtfsCalendarDate extends CsvBase {

    private final String serviceId;
    private final String date;
    private final String exceptionType;

    /********************** Member Functions **************************/

    /**
     * Creates a GtfsRoute object by reading the data from the CSVRecord.
     *
     * @param record
     * @param supplemental
     * @param fileName for logging errors
     */
    public GtfsCalendarDate(CSVRecord record, boolean supplemental, String fileName) throws ParseException {
        super(record, supplemental, fileName);

        serviceId = getRequiredValue(record, "service_id");
        date = getRequiredValue(record, "date");
        exceptionType = getRequiredValue(record, "exception_type");
    }

}
