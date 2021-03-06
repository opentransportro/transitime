package org.transitclock.db.structs;

import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@DynamicUpdate
@Table(name = "Headway",
        indexes = {@Index(name = "HeadwayIndex",
                columnList = "creationTime")})
public class Headway implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4561111910398287801L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    // The revision of the configuration data that was being used
    @Column
    private int configRev;
    @Column
    private double headway;
    @Column
    private double average;
    @Column
    private double variance;
    @Column
    private double coefficientOfVariation;
    @Column
    private int numVehicles;
    // The time the AVL data was processed and the headway was created.
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String vehicleId;
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String otherVehicleId;
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String stopId;
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String tripId;
    @Column(length = HibernateUtils.DEFAULT_ID_SIZE)
    private String routeId;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstDeparture;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date secondDeparture;

    public Headway() {
    }


    public Headway(long headway, Date creationTime, String vehicleId, String otherVehicleId, String stopId, String tripId,
                   String routeId, Date firstDeparture, Date secondDeparture) {

        this.configRev = Core.getInstance().getDbConfig().getConfigRev();
        this.headway = headway;
        this.creationTime = creationTime;
        this.vehicleId = vehicleId;
        this.stopId = stopId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.average = 0;
        this.variance = 0;
        this.coefficientOfVariation = 0;
        this.numVehicles = 0;
        this.otherVehicleId = otherVehicleId;
        this.firstDeparture = firstDeparture;
        this.secondDeparture = secondDeparture;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getOtherVehicleId() {
        return otherVehicleId;
    }

    public void setOtherVehicleId(String otherVehicleId) {
        this.otherVehicleId = otherVehicleId;
    }

    public Date getFirstDeparture() {
        return firstDeparture;
    }

    public void setFirstDeparture(Date firstDeparture) {
        this.firstDeparture = firstDeparture;
    }

    public Date getSecondDeparture() {
        return secondDeparture;
    }

    public void setSecondDeparture(Date secondDeparture) {
        this.secondDeparture = secondDeparture;
    }

    public long getId() {
        return id;
    }

    public int getConfigRev() {
        return configRev;
    }

    public void setConfigRev(int configRev) {
        this.configRev = configRev;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public int getNumVehicles() {
        return numVehicles;
    }

    public void setNumVehicles(int numVehicles) {
        this.numVehicles = numVehicles;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public double getCoefficientOfVariation() {
        return coefficientOfVariation;
    }

    public void setCoefficientOfVariation(double coefficientOfVariation) {
        this.coefficientOfVariation = coefficientOfVariation;
    }

    public double getHeadway() {
        return headway;
    }

    public void setHeadway(double headway) {
        this.headway = headway;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(headway);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((otherVehicleId == null) ? 0 : otherVehicleId.hashCode());
        result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
        result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
        result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Headway other = (Headway) obj;
        if (Double.doubleToLongBits(headway) != Double.doubleToLongBits(other.headway))
            return false;
        if (otherVehicleId == null) {
            if (other.otherVehicleId != null)
                return false;
        } else if (!otherVehicleId.equals(other.otherVehicleId))
            return false;
        if (routeId == null) {
            if (other.routeId != null)
                return false;
        } else if (!routeId.equals(other.routeId))
            return false;
        if (stopId == null) {
            if (other.stopId != null)
                return false;
        } else if (!stopId.equals(other.stopId))
            return false;
        if (tripId == null) {
            if (other.tripId != null)
                return false;
        } else if (!tripId.equals(other.tripId))
            return false;
        if (vehicleId == null) {
            return other.vehicleId == null;
        } else return vehicleId.equals(other.vehicleId);
    }

    @Override
    public String toString() {
        return "Headway [id=" + id + ", configRev=" + configRev + ", headway=" + headway + ", average=" + average
                + ", variance=" + variance + ", coefficientOfVariation=" + coefficientOfVariation + ", numVehicles="
                + numVehicles + ", creationTime=" + creationTime + ", vehicleId=" + vehicleId + ", otherVehicleId="
                + otherVehicleId + ", stopId=" + stopId + ", tripId=" + tripId + ", routeId=" + routeId
                + ", firstDeparture=" + firstDeparture + ", secondDeparture=" + secondDeparture + "]";
    }


}
