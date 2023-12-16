/* (C)2023 */
package org.transitclock.db.structs;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * For keeping track of information having to do with a configuration revision. This way can keep
 * track of reason for processing config, when it was run, etc.
 *
 * @author Michael Smith (michael@transitclock.org)
 */
@Entity
@DynamicUpdate
public class ConfigRevision {

    @Id
    @Column
    private final int configRev;

    @Temporal(TemporalType.TIMESTAMP)
    private final Date processedTime;

    // Last modified time of zip file if GTFS data comes directly
    // from zip file instead of from a directory.
    @Temporal(TemporalType.TIMESTAMP)
    private final Date zipFileLastModifiedTime;

    @Column(length = 512)
    private final String notes;

    // Logging
    public static final Logger logger = LoggerFactory.getLogger(ConfigRevision.class);

    /********************** Member Functions **************************/

    /**
     * @param configRev
     * @param processedTime
     * @param zipFileLastModifiedTime
     * @param notes
     */
    public ConfigRevision(int configRev, Date processedTime, Date zipFileLastModifiedTime, String notes) {
        this.configRev = configRev;
        this.processedTime = processedTime;
        this.zipFileLastModifiedTime = zipFileLastModifiedTime;
        this.notes = notes;
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected ConfigRevision() {
        this.configRev = -1;
        this.processedTime = null;
        this.zipFileLastModifiedTime = null;
        this.notes = null;
    }

    @Override
    public String toString() {
        return "ConfigRevision ["
                + "configRev="
                + configRev
                + ", processedTime="
                + processedTime
                + ", zipFileLastModifiedTime="
                + zipFileLastModifiedTime
                + ", notes="
                + notes
                + "]";
    }

    /**
     * Stores this ConfigRevision into the database for the agencyId.
     *
     * @param agencyId
     */
    public void save(String agencyId) {
        Session session = HibernateUtils.getSession(agencyId);
        Transaction tx = session.beginTransaction();
        try {
            session.save(this);
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Error saving ConfigRevision data to db. {}", this, e);
        } finally {
            session.close();
        }
    }

    /*********************** Getters *********************/
    public int getConfigRev() {
        return configRev;
    }

    public Date getProcessedTime() {
        return processedTime;
    }

    /**
     * Last modified time of zip file if GTFS data comes directly from zip file instead of from a
     * directory.
     *
     * @return
     */
    public Date getZipFileLastModifiedTime() {
        return zipFileLastModifiedTime;
    }

    public String getNotes() {
        return notes;
    }
}
