/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.transitclock.gtfs.DbConfig;

/**
 * For persisting the vehicle state for the vehicle. Can be joined with AvlReport table in order to
 * get additional info for each historic AVL report.
 *
 * @author SkiBu Smith
 */
@Immutable
@Entity
@DynamicUpdate
@Data
@Table(
        name = "vehicle_states",
        indexes = {@Index(name = "VehicleStateAvlTimeIndex", columnList = "avl_time")})
public class VehicleState implements Serializable {
    // vehicleId is an @Id since might get multiple AVL reports
    // for different vehicles with the same avlTime but need a unique
    // primary key.
    @Column(name = "vehicle_id", length = 60)
    @Id
    private final String vehicleId;

    // Need to use columnDefinition to explicitly specify that should use
    // fractional seconds. This column is an Id since shouldn't get two
    // AVL reports for the same vehicle for the same avlTime.
    @Column(name = "avl_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Id
    private final Date avlTime;

    @Column(name = "block_id", length = 60)
    private String blockId;

    @Column(name = "trip_id", length = 60)
    private String tripId;

    @Column(name = "trip_short_name", length = 60)
    private String tripShortName;

    @Column(name = "route_id", length = 60)
    private String routeId;

    private static final int ROUTE_SHORT_NAME_MAX_LENGTH = 80;

    @Column(name = "route_short_name", length = ROUTE_SHORT_NAME_MAX_LENGTH)
    private String routeShortName;

    // Positive means vehicle early, negative means vehicle late
    @Column(name = "schedule_adherence_msec")
    private final Integer schedAdhMsec;

    // A String representing the schedule adherence
    private static final int SCHED_ADH_MAX_LENGTH = 50;

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
        this.vehicleId = truncate(vs.getVehicleId(), 60);
        this.avlTime = vs.getAvlReport() == null ? null : vs.getAvlReport().getDate();
        this.blockId = vs.getBlock() == null ? null : vs.getBlock().getId();
        this.tripId = vs.getTrip() == null ? null : truncate(vs.getTrip().getId(), 60);
        this.tripShortName = vs.getTrip() == null ? null : truncate(vs.getTrip().getShortName(), 60);
        this.routeId = vs.getRouteId();
        this.routeShortName = truncate(vs.getRouteShortName(dbConfig), ROUTE_SHORT_NAME_MAX_LENGTH);
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
        if (original == null || original.length() <= maxLength) return original;

        return original.substring(0, maxLength);
    }
}
