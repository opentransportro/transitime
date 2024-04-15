/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.gtfs.model.GtfsFareRule;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Contains data from the fare_rules.txt GTFS file. This class is for reading/writing that data to
 * the db.
 *
 * @author SkiBu Smith
 */
@Getter @ToString
@Entity
@DynamicUpdate
@Table(name = "fare_rules")
public class FareRule implements Serializable {

    @Id
    @Column(name = "config_rev")
    private final int configRev;

    @Id
    @Column(name = "fare_id", length = 60)
    private final String fareId;

    @Id
    @Column(name = "route_id", length = 60)
    private final String routeId;

    @Id
    @Column(name = "origin_id", length = 60)
    private final String originId;

    @Id
    @Column(name = "destination_id", length = 60)
    private final String destinationId;

    @Id
    @Column(name = "contains_id", length = 60)
    private final String containsId;

    /**
     * For constructing FareRule object using GTFS data.
     *
     * @param configRev
     * @param gfr The GTFS data for the fare rule
     * @param properRouteId If the routeId should be changed to use parent route ID
     */
    public FareRule(int configRev, GtfsFareRule gfr, String properRouteId) {
        this.configRev = configRev;
        this.fareId = gfr.getFareId();
        // routeId, originId and destinationId are primary keys, which means they
        // cannot be null. But they can be null from the GTFS fare_rules.txt
        // file since fare rule could apply to entire system. Therefore if
        // null set to empty string.
        String routeIdToUse;
        if (properRouteId == null) routeIdToUse = gfr.getRouteId() == null ? "" : gfr.getRouteId();
        else routeIdToUse = properRouteId;
        this.routeId = routeIdToUse;
        this.originId = gfr.getOriginId() == null ? "" : gfr.getOriginId();
        this.destinationId = gfr.getDestinationId() == null ? "" : gfr.getDestinationId();
        this.containsId = gfr.getContainsId() == null ? "" : gfr.getContainsId();
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected FareRule() {
        configRev = -1;
        fareId = null;
        routeId = null;
        originId = null;
        destinationId = null;
        containsId = null;
    }

    /**
     * Deletes rev from the FareRules table
     *
     * @param session
     * @param configRev
     * @return Number of rows deleted
     * @throws HibernateException
     */
    public static int deleteFromRev(Session session, int configRev) throws HibernateException {
        // Note that hql uses class name, not the table name
        return session
                .createMutationQuery("DELETE FareRule WHERE configRev = :configRev")
                .setParameter("configRev", configRev)
                .executeUpdate();
    }

    /**
     * Returns List of FareRule objects for the specified database revision.
     *
     * @param session
     * @param configRev
     * @return
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<FareRule> getFareRules(Session session, int configRev) throws HibernateException {
        return session.createQuery("FROM FareRule WHERE configRev = :configRev", FareRule.class)
                .setParameter("configRev", configRev)
                .list();
    }

    /**
     * @return the routeId
     */
    public String getRouteId() {
        // With respect to the database, routeId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return routeId.isEmpty() ? null : routeId;
    }

    /**
     * @return the originId
     */
    public String getOriginId() {
        // With respect to the database, originId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return originId.isEmpty() ? null : originId;
    }

    /**
     * @return the destinationId
     */
    public String getDestinationId() {
        // With respect to the database, destinationId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return destinationId.isEmpty() ? null : destinationId;
    }

    /**
     * @return the containsId
     */
    public String getContainsId() {
        // With respect to the database, containsId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return containsId.isEmpty() ? null : containsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FareRule fareRule)) return false;
        return configRev == fareRule.configRev
            && Objects.equals(fareId, fareRule.fareId)
            && Objects.equals(routeId, fareRule.routeId)
            && Objects.equals(originId, fareRule.originId)
            && Objects.equals(destinationId, fareRule.destinationId)
            && Objects.equals(containsId, fareRule.containsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configRev, fareId, routeId, originId, destinationId, containsId);
    }
}
