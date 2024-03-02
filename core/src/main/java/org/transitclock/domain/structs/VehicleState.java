/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.transitclock.gtfs.DbConfig;
import org.transitclock.utils.StringUtils;

/**
 * For persisting the vehicle state for the vehicle. Can be joined with AvlReport table in order to
 * get additional info for each historic AVL report.
 *
 * @author SkiBu Smith
 */
@Immutable
@Entity
@DynamicUpdate
@Getter @Setter @ToString
@Table(
    name = "vehicle_states",
    indexes = {
        @Index(name = "VehicleStateAvlTimeIndex", columnList = "avl_time")
    }
)
public class VehicleState implements Serializable {
    private static final int ROUTE_SHORT_NAME_MAX_LENGTH = 80;
    private static final int SCHED_ADH_MAX_LENGTH = 50;

    // vehicleId is an @Id since might get multiple AVL reports
    // for different vehicles with the same avlTime but need a unique
    // primary key.
    @Id
    @Column(name = "vehicle_id", length = 60)
    private final String vehicleId;

    // Need to use columnDefinition to explicitly specify that should use
    // fractional seconds. This column is an Id since shouldn't get two
    // AVL reports for the same vehicle for the same avlTime.
    @Id
    @Column(name = "avl_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date avlTime;

    @Column(name = "block_id", length = 60)
    private String blockId;

    @Column(name = "trip_id", length = 60)
    private String tripId;

    @Column(name = "trip_short_name", length = 60)
    private String tripShortName;

    @Column(name = "route_id", length = 60)
    private String routeId;

    @Column(name = "route_short_name", length = ROUTE_SHORT_NAME_MAX_LENGTH)
    private String routeShortName;

    // Positive means vehicle early, negative means vehicle late
    @Column(name = "schedule_adherence_msec")
    private final Integer schedAdhMsec;

    @Column(name = "schedule_adherence", length = SCHED_ADH_MAX_LENGTH)
    private final String schedAdh;

    @Column(name = "schedule_adherence_within_bounds")
    private final Boolean schedAdhWithinBounds;

    @Column(name = "is_delayed")
    private final Boolean isDelayed;

    @Column(name = "is_layover")
    private final Boolean isLayover;

    @Column(name = "is_predictable")
    private final Boolean isPredictable;

    @Column(name = "is_wait_stop")
    private final Boolean isWaitStop;

    @Column(name = "is_for_sched_based_predictions")
    private final Boolean isForSchedBasedPreds;

    public VehicleState(org.transitclock.core.VehicleState vs, DbConfig dbConfig) {
        this.vehicleId = StringUtils.truncate(vs.getVehicleId(), 60);
        this.avlTime = vs.getAvlReport() == null ? null : vs.getAvlReport().getDate();
        this.blockId = vs.getBlock() == null ? null : vs.getBlock().getId();
        this.tripId = vs.getTrip() == null ? null : StringUtils.truncate(vs.getTrip().getId(), 60);
        this.tripShortName = vs.getTrip() == null ? null : StringUtils.truncate(vs.getTrip().getShortName(), 60);
        this.routeId = vs.getRouteId();
        this.routeShortName = StringUtils.truncate(vs.getRouteShortName(dbConfig), ROUTE_SHORT_NAME_MAX_LENGTH);
        this.schedAdhMsec = vs.getRealTimeSchedAdh() == null
                ? null
                : vs.getRealTimeSchedAdh().getTemporalDifference();
        this.schedAdh = vs.getRealTimeSchedAdh() == null
                ? null
                : StringUtils.truncate(vs.getRealTimeSchedAdh().toString(), SCHED_ADH_MAX_LENGTH);
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleState that)) return false;
        return Objects.equals(vehicleId, that.vehicleId) && Objects.equals(avlTime, that.avlTime) && Objects.equals(blockId, that.blockId) && Objects.equals(tripId, that.tripId) && Objects.equals(tripShortName, that.tripShortName) && Objects.equals(routeId, that.routeId) && Objects.equals(routeShortName, that.routeShortName) && Objects.equals(schedAdhMsec, that.schedAdhMsec) && Objects.equals(schedAdh, that.schedAdh) && Objects.equals(schedAdhWithinBounds, that.schedAdhWithinBounds) && Objects.equals(isDelayed, that.isDelayed) && Objects.equals(isLayover, that.isLayover) && Objects.equals(isPredictable, that.isPredictable) && Objects.equals(isWaitStop, that.isWaitStop) && Objects.equals(isForSchedBasedPreds, that.isForSchedBasedPreds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, avlTime, blockId, tripId, tripShortName, routeId, routeShortName, schedAdhMsec, schedAdh, schedAdhWithinBounds, isDelayed, isLayover, isPredictable, isWaitStop, isForSchedBasedPreds);
    }
}
