/* (C)2023 */
package org.transitclock.db.structs;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.classic.Lifecycle;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * A database object for persisting information on how accurate a prediction was compared to the
 * actual measured arrival/departure time for the vehicle.
 *
 * <p>Serializable since Hibernate requires such.
 *
 * <p>Implements Lifecycle so that can have the onLoad() callback be called when reading in data so
 * that can intern() member strings. In order to do this the String members could not be declared as
 * final since they are updated after the constructor is called.
 *
 * @author SkiBu Smith
 */
@Entity
@DynamicUpdate
@Table(
        name = "PredictionAccuracy",
        indexes = {@Index(name = "PredictionAccuracyTimeIndex", columnList = "arrivalDepartureTime")})
public class PredictionAccuracy implements Lifecycle, Serializable {

    // Need an ID but using regular columns doesn't really make
    // sense. So use an auto generated one. Not final since
    // autogenerated and therefore not set in constructor.
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // Not declared final since using intern() when reading from db
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String routeId;

    // routeShortName is included because for some agencies the
    // route_id changes when there are schedule updates. But the
    // routeShortName is more likely to stay consistent. Therefore
    // it is better for when querying for arrival/departure data
    // over a time span.
    // Not declared final since using intern() when reading from db
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String routeShortName;

    // Not declared final since using intern() when reading from db
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String directionId;

    // Not declared final since using intern() when reading from db
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String stopId;

    // So can see which trip predictions for so can easily determine
    // what the travel times are and see if they appear to be correct.
    // Not declared final since using intern() when reading from db
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String tripId;

    // The actual arrival time that corresponds to the prediction time
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date arrivalDepartureTime;

    // The predicted time the vehicle was expected to arrive/depart the stop
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date predictedTime;

    // The time the prediction was read. This allows us to determine
    // how far out into the future the prediction is for.
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private final Date predictionReadTime;

    // Positive means vehicle arrived at stop later then predicted for and
    // negative value means vehicle arrived earlier.
    @Column
    private final int predictionAccuracyMsecs;

    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String predictionSource;

    /* TODO */
    // @Column(length=HibernateUtils.DEFAULT_ID_SIZE)
    @Transient
    private String predictionAlgorithm;

    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String vehicleId;

    @Column
    private final Boolean affectedByWaitStop;

    private static final long serialVersionUID = -6900411351649946446L;

    /********************** Member Functions **************************/

    /**
     * Simple constructor for creating object to be stored in db
     *
     * @param routeId
     * @param directionId
     * @param stopId
     * @param tripId
     * @param arrivalDepartureTime
     * @param predictedTime The time the vehicle was predicted to arrive at the stop
     * @param predictionReadTime
     * @param predictionSource
     * @param predictionAlgorithm
     * @param vehicleId
     */
    public PredictionAccuracy(
            String routeId,
            String directionId,
            String stopId,
            String tripId,
            Date arrivalDepartureTime,
            Date predictedTime,
            Date predictionReadTime,
            String predictionSource,
            String predictionAlgorithm,
            String vehicleId,
            Boolean affectedByWaitStop) {
        super();
        this.routeId = routeId;

        Route route = Core.getInstance().getDbConfig().getRouteById(routeId);
        this.routeShortName = route.getShortName();
        this.directionId = directionId;
        this.stopId = stopId;
        this.tripId = tripId;
        this.arrivalDepartureTime = arrivalDepartureTime;
        this.predictedTime = predictedTime;
        this.predictionReadTime = predictionReadTime;
        this.predictionAccuracyMsecs =
                arrivalDepartureTime != null ? (int) (arrivalDepartureTime.getTime() - predictedTime.getTime()) : 0;
        this.predictionSource = predictionSource;
        this.vehicleId = vehicleId;
        this.affectedByWaitStop = affectedByWaitStop;
        this.predictionAlgorithm = predictionAlgorithm;
    }

    public String getPredictionAlgorithm() {
        return predictionAlgorithm;
    }

    /** Hibernate requires a no-arg constructor for reading objects from database. */
    protected PredictionAccuracy() {
        super();
        this.routeId = null;
        this.routeShortName = null;
        this.directionId = null;
        this.stopId = null;
        this.tripId = null;
        this.arrivalDepartureTime = null;
        this.predictedTime = null;
        this.predictionReadTime = null;
        this.predictionAccuracyMsecs = -1;
        this.predictionSource = null;
        this.vehicleId = null;
        this.affectedByWaitStop = null;
        this.predictionAlgorithm = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((affectedByWaitStop == null) ? 0 : affectedByWaitStop.hashCode());
        result = prime * result + ((arrivalDepartureTime == null) ? 0 : arrivalDepartureTime.hashCode());
        result = prime * result + ((directionId == null) ? 0 : directionId.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((predictedTime == null) ? 0 : predictedTime.hashCode());
        result = prime * result + predictionAccuracyMsecs;
        result = prime * result + ((predictionReadTime == null) ? 0 : predictionReadTime.hashCode());
        result = prime * result + ((predictionSource == null) ? 0 : predictionSource.hashCode());
        result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
        result = prime * result + ((routeShortName == null) ? 0 : routeShortName.hashCode());
        result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
        result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PredictionAccuracy other = (PredictionAccuracy) obj;
        if (affectedByWaitStop == null) {
            if (other.affectedByWaitStop != null) return false;
        } else if (!affectedByWaitStop.equals(other.affectedByWaitStop)) return false;
        if (arrivalDepartureTime == null) {
            if (other.arrivalDepartureTime != null) return false;
        } else if (!arrivalDepartureTime.equals(other.arrivalDepartureTime)) return false;
        if (directionId == null) {
            if (other.directionId != null) return false;
        } else if (!directionId.equals(other.directionId)) return false;
        if (id != other.id) return false;
        if (predictedTime == null) {
            if (other.predictedTime != null) return false;
        } else if (!predictedTime.equals(other.predictedTime)) return false;
        if (predictionAccuracyMsecs != other.predictionAccuracyMsecs) return false;
        if (predictionReadTime == null) {
            if (other.predictionReadTime != null) return false;
        } else if (!predictionReadTime.equals(other.predictionReadTime)) return false;
        if (predictionSource == null) {
            if (other.predictionSource != null) return false;
        } else if (!predictionSource.equals(other.predictionSource)) return false;
        if (predictionAlgorithm == null) {
            if (other.predictionAlgorithm != null) return false;
        } else if (!predictionAlgorithm.equals(other.predictionAlgorithm)) return false;
        if (routeId == null) {
            if (other.routeId != null) return false;
        } else if (!routeId.equals(other.routeId)) return false;
        if (routeShortName == null) {
            if (other.routeShortName != null) return false;
        } else if (!routeShortName.equals(other.routeShortName)) return false;
        if (stopId == null) {
            if (other.stopId != null) return false;
        } else if (!stopId.equals(other.stopId)) return false;
        if (tripId == null) {
            if (other.tripId != null) return false;
        } else if (!tripId.equals(other.tripId)) return false;
        if (vehicleId == null) {
            if (other.vehicleId != null) return false;
        } else if (!vehicleId.equals(other.vehicleId)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PredictionAccuracy ["
                + "routeId="
                + routeId
                + " routeShortName="
                + routeShortName
                + ", directionId="
                + directionId
                + ", stopId="
                + stopId
                + ", tripId="
                + tripId
                + ", arrivalDepartureTime="
                + arrivalDepartureTime
                + ", predictedTime="
                + predictedTime
                + ", predictionReadTime="
                + predictionReadTime
                + ", predictionLengthMsecs="
                + getPredictionLengthMsecs()
                + ", predictionAccuracyMsecs="
                + predictionAccuracyMsecs
                + ", predictionSource="
                + predictionSource
                + ", predictionAlgorithm="
                + predictionAlgorithm
                + ", vehicleId="
                + vehicleId
                + ", affectedByWaitStop="
                + affectedByWaitStop
                + "]";
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getDirectionId() {
        return directionId;
    }

    public String getStopId() {
        return stopId;
    }

    public String getTripId() {
        return tripId;
    }

    public Date getArrivalDepartureTime() {
        return arrivalDepartureTime;
    }

    public Date getPredictedTime() {
        return predictedTime;
    }

    public Date getPredictionReadTime() {
        return predictionReadTime;
    }

    public int getPredictionLengthMsecs() {
        return (int) (predictedTime.getTime() - predictionReadTime.getTime());
    }

    public int getPredictionAccuracyMsecs() {
        return predictionAccuracyMsecs;
    }

    public String getPredictionSource() {
        return predictionSource;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * True if the prediction is based on scheduled departure time, false if not. Null if feed of
     * predictions doesn't provide that information.
     *
     * @return
     */
    public Boolean isAffectedByWaitStop() {
        return affectedByWaitStop;
    }

    /** Callback due to implementing Lifecycle interface. Used to compact string members by them. */
    @Override
    public void onLoad(Session s, Serializable id) throws CallbackException {
        if (routeId != null) routeId = routeId.intern();
        if (routeShortName != null) routeShortName = routeShortName.intern();
        if (directionId != null) directionId = directionId.intern();
        if (stopId != null) stopId = stopId.intern();
        if (tripId != null) tripId = tripId.intern();
        if (predictionSource != null) predictionSource = predictionSource.intern();
        if (predictionAlgorithm != null) predictionAlgorithm = predictionAlgorithm.intern();
        if (vehicleId != null) vehicleId = vehicleId.intern();
    }

    /** Implemented due to Lifecycle interface being implemented. Not actually used. */
    @Override
    public boolean onSave(Session s) throws CallbackException {
        return Lifecycle.NO_VETO;
    }

    /** Implemented due to Lifecycle interface being implemented. Not actually used. */
    @Override
    public boolean onUpdate(Session s) throws CallbackException {
        return Lifecycle.NO_VETO;
    }

    /** Implemented due to Lifecycle interface being implemented. Not actually used. */
    @Override
    public boolean onDelete(Session s) throws CallbackException {
        return Lifecycle.NO_VETO;
    }
}
