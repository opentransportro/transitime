/* (C)2023 */
package org.transitclock.db.structs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.gtfs.gtfsStructs.GtfsFareAttribute;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * Contains data from the fareattributes.txt GTFS file. This class is for reading/writing that data
 * to the db.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@EqualsAndHashCode
@ToString
@Getter
@Table(name = "FareAttributes")
public class FareAttribute implements Serializable {

    @Column
    @Id
    private final int configRev;

    @Column(length = 60)
    @Id
    private final String fareId;

    @Column
    private final float price;

    @Column(length = 3)
    private final String currencyType;

    @Column
    private final String paymentMethod;

    @Column
    private final String transfers;

    @Column
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
        String hql = "DELETE FareAttribute WHERE configRev=" + configRev;
        return session.createQuery(hql).executeUpdate();
    }

    /**
     * Returns List of FareAttribute objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<FareAttribute> getFareAttributes(Session session, int configRev) throws HibernateException {
        String hql = "FROM FareAttribute " + "    WHERE configRev = :configRev";
        Query query = session.createQuery(hql);
        query.setInteger("configRev", configRev);
        return query.list();
    }

}
