/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.jcip.annotations.Immutable;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.gtfsStructs.GtfsCalendar;
import org.transitclock.utils.Time;

/**
 * Contains data from the calendar.txt GTFS file. This class is for reading/writing that data to the
 * db.
 *
 * @author SkiBu Smith
 */
@Immutable
@Entity
@DynamicUpdate
@EqualsAndHashCode
@ToString
@Getter
@Table(name = "Calendars")
public class Calendar implements Serializable {

    @Column
    @Id
    private final int configRev;

    @Column(length = 60)
    @Id
    private final String serviceId;

    @Column
    @Id
    private final boolean monday;

    @Column
    @Id
    private final boolean tuesday;

    @Column
    @Id
    private final boolean wednesday;

    @Column
    @Id
    private final boolean thursday;

    @Column
    @Id
    private final boolean friday;

    @Column
    @Id
    private final boolean saturday;

    @Column
    @Id
    private final boolean sunday;

    @Temporal(TemporalType.DATE)
    @Id
    private final Date startDate;

    // The service is to run until midnight of the end date, which is actually
    // the endDate plus 1 day.
    @Temporal(TemporalType.DATE)
    @Id
    private final Date endDate;

    // For outputting start and end date as strings
    private static final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");

    // Logging
    public static final Logger logger = LoggerFactory.getLogger(Calendar.class);

    /********************** Member Functions **************************/

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected Calendar() {
        configRev = -1;
        serviceId = null;
        monday = false;
        tuesday = false;
        wednesday = false;
        thursday = false;
        friday = false;
        saturday = false;
        sunday = false;
        startDate = null;
        endDate = null;
    }

    /**
     * Constructor
     *
     * @param configRev
     * @param gc
     * @param dateFormat
     */
    public Calendar(int configRev, GtfsCalendar gc, DateFormat dateFormat) {
        this.configRev = configRev;
        this.serviceId = gc.getServiceId();
        this.monday = isSetToTrue(gc.getMonday());
        this.tuesday = isSetToTrue(gc.getTuesday());
        this.wednesday = isSetToTrue(gc.getWednesday());
        this.thursday = isSetToTrue(gc.getThursday());
        this.friday = isSetToTrue(gc.getFriday());
        this.saturday = isSetToTrue(gc.getSaturday());
        this.sunday = isSetToTrue(gc.getSunday());

        // Dealing with dates is complicated because must parse
        Date tempDate;
        try {
            tempDate = dateFormat.parse(gc.getStartDate());
        } catch (ParseException e) {
            logger.error(
                    "Could not parse calendar start_date \"{}\" from " + "line #{} from file {}",
                    gc.getStartDate(),
                    gc.getLineNumber(),
                    gc.getFileName());
            tempDate = new Date();
        }
        this.startDate = tempDate;

        // For end date parse the specified date and add a day so that
        // the end date will be midnight of the date specified.
        try {
            tempDate = dateFormat.parse(gc.getEndDate());
        } catch (ParseException e) {
            logger.error(
                    "Could not parse calendar end_date \"{}\" from " + "line #{} from file {}",
                    gc.getStartDate(),
                    gc.getLineNumber(),
                    gc.getFileName());
            tempDate = new Date();
        }
        this.endDate = tempDate;
    }

    /**
     * Deletes rev from the Calendars table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        // Note that hql uses class name, not the table name
        String hql = "DELETE Calendar WHERE configRev=" + configRev;
        return session.createQuery(hql).executeUpdate();
    }

    /**
     * Returns List of Calendar objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return List of Calendar objects
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Calendar> getCalendars(Session session, int configRev) throws HibernateException {
        String hql = "FROM Calendar " + "    WHERE configRev = :configRev" + " ORDER BY serviceId";
        Query query = session.createQuery(hql);
        query.setInteger("configRev", configRev);
        return query.list();
    }

    /**
     * Opens up a new db session and returns Map of Calendar objects for the specified database
     * revision. The map is keyed on the serviceId.
     *
     * @param dbName Specified name of database
     * @param configRev
     * @return Map of Calendar objects keyed on serviceId
     * @throws HibernateException
     */
    public static Map<String, Calendar> getCalendars(String dbName, int configRev) throws HibernateException {
        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(dbName);

        // Get list of calendars
        List<Calendar> calendarList = getCalendars(session, configRev);

        // Convert list to map and return result
        Map<String, Calendar> map = new HashMap<String, Calendar>();
        for (Calendar calendar : calendarList) map.put(calendar.getServiceId(), calendar);
        return map;
    }

    /**
     * Returns true if the parameter zeroOrOne is set to "1". Otherwise returns false.
     *
     * @param zeroOrOne
     * @return
     */
    private boolean isSetToTrue(String zeroOrOne) {
        return zeroOrOne != null && zeroOrOne.trim().equals("1");
    }

    public boolean getMonday() {
        return monday;
    }

    public boolean getTuesday() {
        return tuesday;
    }

    public boolean getWednesday() {
        return wednesday;
    }

    public boolean getThursday() {
        return thursday;
    }

    public boolean getFriday() {
        return friday;
    }

    public boolean getSaturday() {
        return saturday;
    }

    public boolean getSunday() {
        return sunday;
    }

    /**
     * @return the start date as a formatted string
     */
    public String getStartDateStr() {
        return formatter.format(startDate);
    }

    /**
     * End of the last day of service. This means that when an end date is specified the service
     * runs for up to and including that day. Since days start at midnight, this method returns the
     * endDate plus 1 day so that it represents midnight of the configured endDate.
     *
     * @return midnight at the end of the endDate
     */
    public Date getEndDate() {
        return new Date(endDate.getTime() + Time.MS_PER_DAY);
    }

    /**
     * @return the end date as a formatted string
     */
    public String getEndDateStr() {
        return formatter.format(endDate);
    }
}
