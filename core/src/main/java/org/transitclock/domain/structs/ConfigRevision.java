/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.domain.hibernate.HibernateUtils;

/**
 * For keeping track of information having to do with a configuration revision. This way can keep
 * track of reason for processing config, when it was run, etc.
 *
 * @author Michael Smith (michael@transitclock.org)
 */
@Entity
@Slf4j
@DynamicUpdate
@Table(name = "config_revisions")
public class ConfigRevision {
    @Id
    @Column(name = "config_rev")
    private final int configRev;

    @Column(name = "processed_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date processedTime;

    // Last modified time of zip file if GTFS data comes directly
    // from zip file instead of from a directory.
    @Column(name = "zipfile_last_modified_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date zipFileLastModifiedTime;

    @Column(name = "notes", length = 512)
    private final String notes;

    public ConfigRevision(int configRev, Date processedTime, Date zipFileLastModifiedTime, String notes) {
        this.configRev = configRev;
        this.processedTime = processedTime;
        this.zipFileLastModifiedTime = zipFileLastModifiedTime;
        this.notes = notes;
    }

    /**
     * Needed because Hibernate requires no-arg constructor
     */
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
        try (session) {
            Transaction tx = session.beginTransaction();
            session.persist(this);
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Error saving ConfigRevision data to db. {}", this, e);
        }
    }

    public int getConfigRev() {
        return configRev;
    }

    public Date getProcessedTime() {
        return processedTime;
    }

    /**
     * Last modified time of zip file if GTFS data comes directly from zip file instead of from a
     * directory.
     */
    public Date getZipFileLastModifiedTime() {
        return zipFileLastModifiedTime;
    }

    public String getNotes() {
        return notes;
    }
}
