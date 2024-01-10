/* (C)2023 */
package org.transitclock.db.structs;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.IntervalTimer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * For storing monitoring events into database. By storing the monitoring events one can look back
 * and easily see how frequent events are, when the start and stop, etc.
 *
 * @author SkiBu Smith
 */
@Immutable
@Slf4j
@Entity
@DynamicUpdate
@Table(
        name = "MonitoringEvents",
        indexes = {@Index(name = "MonitoringEventsTimeIndex", columnList = "time")})
public class MonitoringEvent implements Serializable {
    // The time the event occurred
    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date time;

    // String describing type of event, such as "System CPU"
    @Id
    @Column(length = 40)
    private final String type;

    // Whether monitoring is now triggered
    @Column
    private final boolean triggered;

    // The long message associated with the monitoring
    private static final int MAX_MESSAGE_LENGTH = 512;

    @Column(length = MAX_MESSAGE_LENGTH)
    private final String message;

    // The value that caused monitoring to be triggered or untriggered.
    // For some monitors they are triggered when this value is too
    // high. For others though it can be when the value is too low.
    @Column
    private final double value;

    private MonitoringEvent(Date time, String type, boolean triggered, String message, double value) {
        this.time = time;
        this.type = type;
        this.triggered = triggered;
        // Since message to be stored in db and don't know how long it might
        // be make sure it is not too long so that don't get db errors.
        this.message = message.length() <= MAX_MESSAGE_LENGTH ? message : message.substring(0, MAX_MESSAGE_LENGTH);
        // Note: MySQL can't handle double values of Double.NaN. Get an exception
        // "java.sql.SQLException: 'NaN' is not a valid numeric or approximate numeric value".
        // So if value is a NaN use 0.0 instead. Works fine with Postgres though.
        this.value = Double.isNaN(value) ? 0.0 : value;
    }

    public static MonitoringEvent create(Date time, String type, boolean triggered, String message, double value) {
        MonitoringEvent monitoringEvent = new MonitoringEvent(time, type, triggered, message, value);

        // Queue to write object to database
        Core.getInstance().getDbLogger().add(monitoringEvent);

        // Return new MonitoringEvent
        return monitoringEvent;
    }

    /**
     * Reads in all MonitoringEvents from the database that were between the beginTime and endTime.
     *
     * @param agencyId Which project getting data for
     * @param beginTime Specifies time range for query
     * @param endTime Specifies time range for query
     * @param sqlClause Optional. Can specify an SQL clause to winnow down the data, such as "AND
     *     routeId='71'".
     * @return
     */
    public static List<MonitoringEvent> getMonitoringEvents(
            String agencyId, Date beginTime, Date endTime, String sqlClause) {
        IntervalTimer timer = new IntervalTimer();

        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(agencyId);

        // Create the query. Table name is case sensitive and needs to be the
        // class name instead of the name of the db table.
        String hql = "FROM MonitoringEvent WHERE time >= :beginDate AND time < :endDate";
        if (sqlClause != null) {
            hql += " " + sqlClause;
        }
        var query = session.createQuery(hql);

        // Set the parameters
        query.setParameter("beginDate", beginTime);
        query.setParameter("endDate", endTime);

        try {
            @SuppressWarnings("unchecked")
            List<MonitoringEvent> monitorEvents = query.list();
            logger.debug("Getting MonitoringEvent from database " + "took {} msec", timer.elapsedMsec());
            return monitorEvents;
        } catch (HibernateException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            // Clean things up. Not sure if this absolutely needed nor if
            // it might actually be detrimental and slow things down.
            session.close();
        }
    }

    /**
     * Hibernate requires a no-args constructor for reading data. So this is an experiment to see
     * what can be done to satisfy Hibernate but still have an object be immutable. Since this
     * constructor is only intended to be used by Hibernate is is declared protected, since that
     * still works. That way others won't accidentally use this inappropriate constructor. And yes,
     * it is peculiar that even though the members in this class are declared final that Hibernate
     * can still create an object using this no-args constructor and then set the fields. Not quite
     * as "final" as one might think. But at least it works.
     */
    protected MonitoringEvent() {
        time = null;
        type = null;
        triggered = false;
        message = null;
        value = Double.NaN;
    }

    /** Because using a composite Id Hibernate wants this method. */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + (triggered ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /** Because using a composite Id Hibernate wants this method. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MonitoringEvent other = (MonitoringEvent) obj;
        if (message == null) {
            if (other.message != null) return false;
        } else if (!message.equals(other.message)) return false;
        if (time == null) {
            if (other.time != null) return false;
        } else if (!time.equals(other.time)) return false;
        if (triggered != other.triggered) return false;
        if (type == null) {
            if (other.type != null) return false;
        } else if (!type.equals(other.type)) return false;
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "MonitoringEvent ["
                + "time="
                + time
                + ", type="
                + type
                + ", triggered="
                + triggered
                + ", message="
                + message
                + ", value="
                + value
                + "]";
    }

    public Date getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public String getMessage() {
        return message;
    }

    public double getValue() {
        return value;
    }
}
