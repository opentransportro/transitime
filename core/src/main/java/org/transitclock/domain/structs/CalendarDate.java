/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.gtfs.model.GtfsCalendarDate;

/**
 * Contains data from the calendardates.txt GTFS file. This class is for reading/writing that data
 * to the db.
 *
 * @author SkiBu Smith
 */
@Getter
@Entity
@DynamicUpdate
@Table(name = "calendar_dates")
public class CalendarDate implements Serializable {

    @Id
    @Column(name = "config_rev")
    private final int configRev;

    @Id
    @Column(name = "service_id", length = 60)
    private final String serviceId;

    @Id
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private final Date date;

    /**
     * -- GETTER --
     *  Note that is probably more clear to use addService() since that way don't need to know what
     *  valid values of exception_type are in GTFS.
     *
     * @return the exceptionType
     */
    @Column(name = "exception_type", length = 2)
    private final String exceptionType;

    /**
     * Constructor
     *
     * @param configRev
     * @param gtfsCalendarDate
     * @param dateFormat
     */
    public CalendarDate(int configRev, GtfsCalendarDate gtfsCalendarDate, DateFormat dateFormat) {
        this.configRev = configRev;
        this.serviceId = gtfsCalendarDate.getServiceId();

        // Dealing with date is complicated because must parse
        Date tempDate;
        try {
            tempDate = dateFormat.parse(gtfsCalendarDate.getDate());
        } catch (ParseException e) {
//            logger.error(
//                    "Could not parse calendar date \"{}\" from " + "line #{} from file {}",
//                    gtfsCalendarDate.getDate(),
//                    gtfsCalendarDate.getLineNumber(),
//                    gtfsCalendarDate.getFileName());
            tempDate = new Date();
        }
        this.date = tempDate;

        this.exceptionType = gtfsCalendarDate.getExceptionType();
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected CalendarDate() {
        configRev = -1;
        serviceId = null;
        date = null;
        exceptionType = null;
    }

    /**
     * Deletes rev from the CalendarDates table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        return session.createMutationQuery("DELETE CalendarDate WHERE configRev= :configRev")
                .setParameter("configRev", configRev)
                .executeUpdate();
    }

    /**
     * Returns List of Agency objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    public static List<CalendarDate> getCalendarDates(Session session, int configRev) throws HibernateException {
        return session.createQuery("FROM CalendarDate WHERE configRev = :configRev", CalendarDate.class)
                .setParameter("configRev", configRev)
                .list();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CalendarDate ["
                + "configRev="
                + configRev
                + ", serviceId="
                + serviceId
                + ", date="
                + date
                + ", exceptionType="
                + exceptionType
                + " ("
                + (addService() ? "add" : "subtract")
                + " service)"
                + "]";
    }

    /** Needed because have a composite ID for Hibernate storage */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + configRev;
        result = prime * result + ((exceptionType == null) ? 0 : exceptionType.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        return result;
    }

    /** Needed because have a composite ID for Hibernate storage */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CalendarDate other = (CalendarDate) obj;
        if (date == null) {
            if (other.date != null) return false;
        } else if (!date.equals(other.date)) return false;
        if (configRev != other.configRev) return false;
        if (exceptionType == null) {
            if (other.exceptionType != null) return false;
        } else if (!exceptionType.equals(other.exceptionType)) return false;
        if (serviceId == null) {
            return other.serviceId == null;
        } else return serviceId.equals(other.serviceId);
    }

    /**
     * The epoch start time of midnight, the beginning of the day.
     *
     * @return the epoch time
     */
    public long getTime() {
        return date.getTime();
    }

    /**
     * Returns true if for this calendar date should add this service. Otherwise should subtract
     * this service for this date.
     *
     * @return True if should add service for this calendar date.
     */
    public boolean addService() {
        return "1".equals(exceptionType);
    }
}
