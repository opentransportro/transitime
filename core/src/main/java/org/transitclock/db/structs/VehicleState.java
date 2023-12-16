/* (C)2023 */
package org.transitclock.db.structs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.jcip.annotations.Immutable;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * For persisting the vehicle state for the vehicle. Can be joined with AvlReport table in order to
 * get additional info for each historic AVL report.
 *
 * @author SkiBu Smith
 */
@Immutable // From jcip.annoations
@Entity
@DynamicUpdate
@EqualsAndHashCode
@Getter
@ToString
@Table(
        name = "VehicleStates",
        indexes = {@Index(name = "VehicleStateAvlTimeIndex", columnList = "avlTime")})
public class VehicleState implements Serializable {
    // vehicleId is an @Id since might get multiple AVL reports
    // for different vehicles with the same avlTime but need a unique
    // primary key.
    @Column(length = 60)
    @Id
    private final String vehicleId;

    // Need to use columnDefinition to explicitly specify that should use
    // fractional seconds. This column is an Id since shouldn't get two
    // AVL reports for the same vehicle for the same avlTime.
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Id
    private final Date avlTime;

    @Column(length = 60)
    private String blockId;

    @Column(length = 60)
    private String tripId;

    @Column(length = 60)
    private String tripShortName;

    @Column(length = 60)
    private String routeId;

    private static final int ROUTE_SHORT_NAME_MAX_LENGTH = 80;

    @Column(length = ROUTE_SHORT_NAME_MAX_LENGTH)
    private String routeShortName;

    // Positive means vehicle early, negative means vehicle late
    @Column
    private final Integer schedAdhMsec;

    // A String representing the schedule adherence
    private static final int SCHED_ADH_MAX_LENGTH = 50;

    @Column(length = SCHED_ADH_MAX_LENGTH)
    private final String schedAdh;

    @Column
    private final Boolean schedAdhWithinBounds;

    @Column
    private final Boolean isDelayed;

    @Column
    private final Boolean isLayover;

    @Column
    private final Boolean isPredictable;

    @Column
    private final Boolean isWaitStop;

    @Column
    private final Boolean isForSchedBasedPreds;

    public VehicleState(org.transitclock.core.VehicleState vs) {
        this.vehicleId = truncate(vs.getVehicleId(), 60);
        this.avlTime = vs.getAvlReport() == null ? null : vs.getAvlReport().getDate();
        this.blockId = vs.getBlock() == null ? null : vs.getBlock().getId();
        this.tripId = vs.getTrip() == null ? null : truncate(vs.getTrip().getId(), 60);
        this.tripShortName =
                vs.getTrip() == null ? null : truncate(vs.getTrip().getShortName(), 60);
        this.routeId = vs.getRouteId();
        this.routeShortName = truncate(vs.getRouteShortName(), ROUTE_SHORT_NAME_MAX_LENGTH);
        this.schedAdhMsec = vs.getRealTimeSchedAdh() == null
                ? null
                : vs.getRealTimeSchedAdh().getTemporalDifference();
        this.schedAdh = vs.getRealTimeSchedAdh() == null
                ? null
                : truncate(vs.getRealTimeSchedAdh().toString(), SCHED_ADH_MAX_LENGTH);
        this.schedAdhWithinBounds = vs.getRealTimeSchedAdh() == null
                ? null
                : vs.getRealTimeSchedAdh().isWithinBounds();
        this.isDelayed = vs.isDelayed();
        this.isLayover = vs.isLayover();
        this.isPredictable = vs.isPredictable();
        this.isWaitStop = vs.isWaitStop();
        this.isForSchedBasedPreds = vs.isForSchedBasedPreds();
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected VehicleState() {
        this.vehicleId = null;
        this.avlTime = null;
        this.blockId = null;
        this.tripId = null;
        this.routeId = null;
        this.routeShortName = null;
        this.schedAdhMsec = null;
        this.schedAdh = null;
        this.schedAdhWithinBounds = null;
        this.isDelayed = null;
        this.isLayover = null;
        this.isPredictable = null;
        this.isWaitStop = null;
        this.isForSchedBasedPreds = null;
    }

    /**
     * For making sure that members don't get a value that is longer than allowed. Truncates string
     * to maxLength if it is too long. This way won't get a db error if try to store a string that
     * is too long.
     *
     * @param original the string to possibly be truncated
     * @param maxLength max length string can have in db
     * @return possibly truncated version of the original string
     */
    private String truncate(String original, int maxLength) {
        if (original == null || original.length() <= maxLength)
            return original;

        return original.substring(0, maxLength);
    }
}
