/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.gtfs.gtfsStructs.GtfsTransfer;

/**
 * Contains data from the transfers.txt GTFS file. This class is for reading/writing that data to
 * the db.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@ToString
@EqualsAndHashCode
@Getter
@Table(name = "Transfers")
public class Transfer implements Serializable {

    @Column
    @Id
    private final int configRev;

    @Column(length = 60)
    @Id
    private final String fromStopId;

    @Column(length = 60)
    @Id
    private final String toStopId;

    @Column(length = 1)
    private final String transferType;

    @Column
    private final Integer minTransferTime;

    public Transfer(int configRev, GtfsTransfer gt) {
        this.configRev = configRev;
        this.fromStopId = gt.getFromStopId();
        this.toStopId = gt.getToStopId();
        this.transferType = gt.getTransferType();
        this.minTransferTime = gt.getMinTransferTime();
    }

    /** Needed because no-arg constructor required by Hibernate */
    @SuppressWarnings("unused")
    protected Transfer() {
        configRev = -1;
        fromStopId = null;
        toStopId = null;
        transferType = null;
        minTransferTime = null;
    }

    /**
     * Deletes rev 0 from the Transfers table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        // Note that hql uses class name, not the table name
        return session.createQuery("DELETE Transfer WHERE configRev=:configRev")
                .setParameter("configRev", configRev)
                .executeUpdate();
    }

    /**
     * Returns List of Transfer objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Transfer> getTransfers(Session session, int configRev) throws HibernateException {
        return session.createQuery("FROM Transfer WHERE configRev = :configRev")
                .setParameter("configRev", configRev)
                .list();
    }
}
