/* (C)2023 */
package org.transitclock.db.structs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.applications.Core;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@EqualsAndHashCode
@ToString
@Getter @Setter
@DynamicUpdate
@Table(
        name = "HoldingTimes",
        indexes = {@Index(name = "HoldingTimeIndex", columnList = "creationTime")})

/**
 * For persisting a holding time recommendation.
 *
 * @author Sean Óg Crudden
 */
public class HoldingTime implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // The revision of the configuration data that was being used
    @Column
    private final int configRev;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date holdingTime;

    // The time the AVL data was processed and the prediction was created.
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date creationTime;

    @Column(length = 60)
    private final String vehicleId;

    @Column(length = 60)
    private final String stopId;

    @Column(length = 60)
    private final String tripId;

    @Column(length = 60)
    private final String routeId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date arrivalTime;

    public Date getArrivalTime() {
        return arrivalTime;
    }

    @Column
    private boolean arrivalPredictionUsed;

    @Column
    private boolean arrivalUsed;

    @Column
    private boolean hasD1;

    @Column
    int numberPredictionsUsed;

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
        this.configRev = Core.getInstance().getDbConfig().getConfigRev();
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

    public HoldingTime(HoldingTime holdingTimeToCopy, Date holdingTime) {
        this.configRev = Core.getInstance().getDbConfig().getConfigRev();
        this.holdingTime = holdingTime;
        this.creationTime = holdingTimeToCopy.creationTime;
        this.vehicleId = holdingTimeToCopy.vehicleId;
        this.stopId = holdingTimeToCopy.stopId;
        this.tripId = holdingTimeToCopy.tripId;
        this.routeId = holdingTimeToCopy.routeId;
        this.arrivalPredictionUsed = holdingTimeToCopy.arrivalPredictionUsed;
        this.arrivalTime = holdingTimeToCopy.arrivalTime;
        this.arrivalUsed = holdingTimeToCopy.arrivalUsed;
        this.hasD1 = holdingTimeToCopy.hasD1;
        this.numberPredictionsUsed = holdingTimeToCopy.numberPredictionsUsed;
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
