/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.gtfs.model.GtfsFareAttribute;

/**
 * Contains data from the fareattributes.txt GTFS file. This class is for reading/writing that data
 * to the db.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@Data
@Table(name = "fare_attributes")
public class FareAttribute implements Serializable {

    @Column(name = "config_rev")
    @Id
    private final int configRev;

    @Column(name = "fare_id", length = 60)
    @Id
    private final String fareId;

    @Column(name = "price")
    private final float price;

    @Column(name = "currency_type", length = 3)
    private final String currencyType;

    @Column(name = "payment_method")
    private final String paymentMethod;

    @Column(name = "transfers")
    private final String transfers;

    @Column(name = "transfer_duration")
    private final Integer transferDuration;

    /**
     * Constructor
     *
     * @param configRev
     * @param gf
     */
    public FareAttribute(int configRev, GtfsFareAttribute gf) {
        this.configRev = configRev;
        this.fareId = gf.getFareId();
        this.price = gf.getPrice();
        this.currencyType = gf.getCurrencyType();
        this.paymentMethod = gf.getPaymentMethod();
        this.transfers = gf.getTransfers();
        this.transferDuration = gf.getTransferDuration();
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected FareAttribute() {
        configRev = 0;
        fareId = null;
        price = Float.NaN;
        currencyType = null;
        paymentMethod = null;
        transfers = null;
        transferDuration = null;
    }

    /**
     * Deletes rev from the FareAttributes table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        // Note that hql uses class name, not the table name
        return session.createMutationQuery("DELETE FareAttribute WHERE configRev=" + configRev)
                .executeUpdate();
    }

    /**
     * Returns List of FareAttribute objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    public static List<FareAttribute> getFareAttributes(Session session, int configRev) throws HibernateException {
        return session.createQuery("FROM FareAttribute WHERE configRev = :configRev", FareAttribute.class)
                .setParameter("configRev", configRev)
                .list();
    }
}
