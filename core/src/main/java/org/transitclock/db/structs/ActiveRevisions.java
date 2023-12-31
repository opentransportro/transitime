/* (C)2023 */
package org.transitclock.db.structs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For keeping track of current revisions. This table should only have a single row, one that
 * specified the configRev and the travelTimesRev currently being used.
 *
 * @author SkiBu Smith
 */
@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Slf4j
public class ActiveRevisions {

    // Need a generated ID since Hibernate required some type
    // of ID. Both configRev and travelTimesRev
    // might get updated and with Hibernate you can't read in an
    // object, modify an ID and then write it out again. Therefore
    // configRev and travelTimesRev can't be an ID. This means
    // that need a separate ID. Yes, somewhat peculiar.
    @Id
    @Column
    @GeneratedValue
    private Integer id;

    // For the configuration data for routes, stops, schedule, etc.
    @Column
    private int configRev;

    // For the travel time configuration data. Updated independently of
    // configRev.
    @Column
    private int travelTimesRev;

    /** Constructor. Sets the revisions to default values of -1. */
    public ActiveRevisions() {
        configRev = -1;
        travelTimesRev = -1;
    }

    /**
     * Gets the ActiveRevisions object using the passed in database session.
     *
     * @param session
     * @return the ActiveRevisions
     * @throws HibernateException
     */
    public static ActiveRevisions get(Session session) throws HibernateException {
        // There should only be a single object so don't need a WHERE clause
        var query = session.createQuery("FROM ActiveRevisions");
        ActiveRevisions activeRevisions = null;
        try {
            activeRevisions = (ActiveRevisions) query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Exception when reading ActiveRevisions object " + "from database so will create it");
        } finally {
            // If we couldn't read from db use default values and write the object to the database.
            if (activeRevisions == null) {
                activeRevisions = new ActiveRevisions();
                session.persist(activeRevisions);
            }
        }

        // Return the object
        return activeRevisions;
    }

    public static ActiveRevisions get(String agencyId) throws HibernateException {
        try (Session session = HibernateUtils.getSession(agencyId)) {
            return get(session);
        } catch (HibernateException e) {
            logger.error("Exception in ActiveRevisions.get(). {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Updates configRev member and calls saveOrUpdate(this) on the session. Useful for when want to
     * update the value but don't want to commit it until all other data is also written out
     * successfully.
     *
     * @param session
     * @param configRev
     */
    public void setConfigRev(Session session, int configRev) {
        this.configRev = configRev;
        session.saveOrUpdate(this);
    }

    /**
     * Updates travelTimeRev member and calls saveOrUpdate(this) on the session. Useful for when
     * want to update the value but don't want to commit it until all other data is also written out
     * successfully.
     */
    public void setTravelTimesRev(Session session, int travelTimeRev) {
        this.travelTimesRev = travelTimeRev;
        session.saveOrUpdate(this);
    }

    /**
     * @return True if both the configRev and travelTimesRev are both valid.
     */
    public boolean isValid() {
        return configRev >= 0 && travelTimesRev >= 0;
    }
}
