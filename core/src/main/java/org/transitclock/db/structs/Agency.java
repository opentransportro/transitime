/* (C)2023 */
package org.transitclock.db.structs;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.gtfs.gtfsStructs.GtfsAgency;
import org.transitclock.utils.Time;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.TimeZone;

/**
 * Contains data from the agency.txt GTFS file. This class is for reading/writing that data to the
 * db.
 *
 * @author SkiBu Smith
 */
@Entity
@ToString
@EqualsAndHashCode
@DynamicUpdate
@Table(name = "Agencies")
public class Agency implements Serializable {

    @Column
    @Id
    private final int configRev;

    @Column(length = 60)
    @Id
    private final String agencyName;

    // Note: this is the GTFS agency_id, not the usual
    // Transitime agencyId.
    @Column(length = 60)
    private final String agencyId;

    @Column
    private final String agencyUrl;

    // Note: agencyTimezone can be reasonable long. At least as long
    // as "America/Los_Angeles". Valid timezone format is at
    // http://en.wikipedia.org/wiki/List_of_tz_zones
    @Column(length = 40)
    private final String agencyTimezone;

    @Column(length = 15)
    private final String agencyLang;

    @Column(length = 15)
    private final String agencyPhone;

    @Column
    private final String agencyFareUrl;

    @Embedded
    private final Extent extent;

    @Transient
    private TimeZone timezone = null;

    @Transient
    private Time time = null;

    // Because Hibernate requires objects with composite Ids to be Serializable
    private static final long serialVersionUID = -3381456129303325040L;

    /********************** Member Functions **************************/

    /**
     * For creating object to be written to db.
     *
     * @param configRev
     * @param gtfsAgency
     * @param routes
     */
    public Agency(int configRev, GtfsAgency gtfsAgency, List<Route> routes) {
        this.configRev = configRev;
        this.agencyId = gtfsAgency.getAgencyId();
        this.agencyName = gtfsAgency.getAgencyName();
        this.agencyUrl = gtfsAgency.getAgencyUrl();
        this.agencyTimezone = gtfsAgency.getAgencyTimezone();
        this.agencyLang = gtfsAgency.getAgencyLang();
        this.agencyPhone = gtfsAgency.getAgencyPhone();
        this.agencyFareUrl = gtfsAgency.getAgencyFareUrl();

        Extent extent = new Extent();
        for (Route route : routes) {
            extent.add(route.getExtent());
        }
        this.extent = extent;
    }

    /** Needed because Hibernate requires no-arg constructor for reading in data */
    @SuppressWarnings("unused")
    protected Agency() {
        configRev = -1;
        agencyId = null;
        agencyName = null;
        agencyUrl = null;
        agencyTimezone = null;
        agencyLang = null;
        agencyPhone = null;
        agencyFareUrl = null;
        extent = null;
    }

    /**
     * Deletes rev from the Agencies table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        // Note that hql uses class name, not the table name
        String hql = "DELETE Agency WHERE configRev=" + configRev;
        return session.createQuery(hql).executeUpdate();
    }

    /**
     * Returns List of Agency objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Agency> getAgencies(Session session, int configRev) throws HibernateException {
        String hql = "FROM Agency " + "    WHERE configRev = :configRev";
        Query query = session.createQuery(hql);
        query.setInteger("configRev", configRev);
        return query.list();
    }

    /**
     * Returns the list of agencies for the specified project ID.
     *
     * @param agencyId Specifies name of database
     * @param configRev
     * @return
     */
    public static List<Agency> getAgencies(String agencyId, int configRev) {
        // Get the database session. This is supposed to be pretty light weight
        Session session = HibernateUtils.getSession(agencyId);
        try {
            return getAgencies(session, configRev);
        } finally {
            session.close();
        }
    }

    /**
     * Reads the current timezone for the agency from the agencies database
     *
     * @param agencyId
     * @return The TimeZone, or null if not successful
     */
    public static TimeZone getTimeZoneFromDb(String agencyId) {
        int configRev = ActiveRevisions.get(agencyId).getConfigRev();

        List<Agency> agencies = getAgencies(agencyId, configRev);
        if (!agencies.isEmpty()) return agencies.get(0).getTimeZone();
        else return null;
    }

    /**
     * Returns cached TimeZone object for agency. Useful for creating Calendar objects and such.
     *
     * @return The TimeZone object for this agency
     */
    public TimeZone getTimeZone() {
        if (timezone == null) timezone = TimeZone.getTimeZone(agencyTimezone);
        return timezone;
    }

    /**
     * Returns cached Time object which allows one to easly convert epoch time to time of day and
     * such.
     *
     * @return Time object
     */
    public Time getTime() {
        if (time == null) time = new Time(agencyTimezone);
        return time;
    }


    /************************** Getter Methods ******************************/

    /**
     * @return the configRev
     */
    public int getConfigRev() {
        return configRev;
    }

    /**
     * Note that this method returns the GTFS agency_id which is usually different from the
     * Transitime agencyId
     *
     * @return the agencyId
     */
    public String getId() {
        return agencyId;
    }

    /**
     * @return the agencyName
     */
    public String getName() {
        return agencyName;
    }

    /**
     * @return the agencyUrl
     */
    public String getUrl() {
        return agencyUrl;
    }

    /**
     * Valid timezone format is at http://en.wikipedia.org/wiki/List_of_tz_zones
     *
     * @return the agencyTimezone as a String
     */
    public String getTimeZoneStr() {
        return agencyTimezone;
    }

    /**
     * @return the agencyLang
     */
    public String getLang() {
        return agencyLang;
    }

    /**
     * @return the agencyPhone
     */
    public String getPhone() {
        return agencyPhone;
    }

    /**
     * @return the agencyFareUrl
     */
    public String getFareUrl() {
        return agencyFareUrl;
    }

    /**
     * @return The extent of all the stops for the agency
     */
    public Extent getExtent() {
        return extent;
    }
}
