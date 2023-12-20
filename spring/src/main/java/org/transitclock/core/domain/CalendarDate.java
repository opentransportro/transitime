/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.gtfs.GtfsCalendarDate;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Contains data from the calendardates.txt GTFS file. This class is for reading/writing that data
 * to the db.
 *
 * @author SkiBu Smith
 */
@Data
@Slf4j
@Document(collection = "CalendarDates")
public class CalendarDate implements Serializable {
    @Data
    public static class Key {
        private final int configRev;
        private final String serviceId;
        private final Date date;
    }
    @Id
    @Delegate
    private final Key key;

    private final String exceptionType;


    /**
     * Constructor
     *
     * @param configRev
     * @param gtfsCalendarDate
     * @param dateFormat
     */
    public CalendarDate(int configRev, GtfsCalendarDate gtfsCalendarDate, DateFormat dateFormat) {

        // Dealing with date is complicated because must parse
        Date tempDate;
        try {
            tempDate = dateFormat.parse(gtfsCalendarDate.getDate());
        } catch (ParseException e) {
            logger.error(
                    "Could not parse calendar date \"{}\" from " + "line #{} from file {}",
                    gtfsCalendarDate.getDate(),
                    gtfsCalendarDate.getLineNumber(),
                    gtfsCalendarDate.getFileName());
            tempDate = new Date();
        }
        this.key = new Key(configRev, gtfsCalendarDate.getServiceId(), tempDate);

        this.exceptionType = gtfsCalendarDate.getExceptionType();
    }


    @Override
    public String toString() {
        return "CalendarDate ["
                + "key="
                + key
                + ", exceptionType="
                + exceptionType
                + " ("
                + (isAddService() ? "add" : "subtract")
                + " service)"
                + "]";
    }

    /**
     * The epoch start time of midnight, the beginning of the day.
     *
     * @return the epoch time
     */
    public long getTime() {
        return key.date.getTime();
    }

    /**
     * Returns true if for this calendar date should add this service. Otherwise should subtract
     * this service for this date.
     *
     * @return True if should add service for this calendar date.
     */
    public boolean isAddService() {
        return "1".equals(exceptionType);
    }
}
