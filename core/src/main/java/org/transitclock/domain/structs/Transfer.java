/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.gtfs.model.GtfsTransfer;

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
@Table(name = "transfers")
public class Transfer implements Serializable {

    @Column(name = "config_rev")
    @Id
    private final int configRev;

    @Column(name = "from_stop_id", length = 60)
    @Id
    private final String fromStopId;

    @Column(name = "to_stop_id", length = 60)
    @Id
    private final String toStopId;

    @Column(name = "transfer_type", length = 1)
    private final String transferType;

    @Column(name = "min_transfer_time")
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
        return session.createMutationQuery("DELETE Transfer WHERE configRev=:configRev")
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
    public static List<Transfer> getTransfers(Session session, int configRev) throws HibernateException {
        return session.createQuery("FROM Transfer WHERE configRev = :configRev", Transfer.class)
                .setParameter("configRev", configRev)
                .list();
    }
}
