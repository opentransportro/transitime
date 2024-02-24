/* (C)2023 */
package org.transitclock.domain.structs;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Data
@DynamicUpdate
@Table(
        name = "holding_times",
        indexes = {@Index(name = "HoldingTimeIndex", columnList = "creation_time")})

public class HoldingTime implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // The revision of the configuration data that was being used
    @Column(name = "config_rev")
    private final int configRev;

    @Column(name = "holding_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date holdingTime;

    // The time the AVL data was processed and the prediction was created.
    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date creationTime;

    @Column(name = "vehicle_id", length = 60)
    private final String vehicleId;

    @Column(name = "stop_id", length = 60)
    private final String stopId;

    @Column(name = "trip_id", length = 60)
    private final String tripId;

    @Column(name = "route_id", length = 60)
    private final String routeId;

    @Column(name = "arrival_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Date arrivalTime;

    @Column(name = "arrival_prediction_used")
    private boolean arrivalPredictionUsed;

    @Column(name = "arrival_used")
    private boolean arrivalUsed;

    @Column(name = "has_d1")
    private boolean hasD1;

    @Column(name = "number_prediction_used")
    private int numberPredictionsUsed;

    public HoldingTime() {
        this.configRev = -1;
        this.holdingTime = null;
        this.creationTime = null;
        this.vehicleId = null;
        this.stopId = null;
        this.tripId = null;
        this.routeId = null;
        arrivalPredictionUsed = false;
        arrivalUsed = false;
        arrivalTime = null;
        hasD1 = false;
        numberPredictionsUsed = -1;
    }

    public HoldingTime(
            int configRev,
            Date holdingTime,
            Date creationTime,
            String vehicleId,
            String stopId,
            String tripId,
            String routeId,
            boolean arrivalPredictionUsed,
            boolean arrivalUsed,
            Date arrivalTime,
            boolean hasD1,
            int numberPredictionsUsed) {
        this.configRev = configRev;
        this.holdingTime = holdingTime;
        this.creationTime = creationTime;
        this.vehicleId = vehicleId;
        this.stopId = stopId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.arrivalPredictionUsed = arrivalPredictionUsed;
        this.arrivalTime = arrivalTime;
        this.arrivalUsed = arrivalUsed;
        this.hasD1 = hasD1;
        this.numberPredictionsUsed = numberPredictionsUsed;
    }

    public Date getTimeToLeave(Date currentTime) {
        if (currentTime.after(holdingTime)) {
            return currentTime;
        }

        return holdingTime;
    }

    public boolean leaveStop(Date currentTime) {
        return holdingTime.before(currentTime);
    }
}
