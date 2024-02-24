/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.domain.structs.HoldingTime;

import java.io.Serializable;

/**
 * @author Sean Óg Crudden
 */
public class HoldingTimeCacheKey implements Serializable {
    private String stopid;
    private String vehicleId;
    private String tripId;

    public HoldingTimeCacheKey(String stopid, String vehicleId, String tripId) {
        this.stopid = stopid;
        this.vehicleId = vehicleId;
        this.tripId = tripId;
    }

    public HoldingTimeCacheKey(HoldingTime holdTime) {
        this.stopid = holdTime.getStopId();
        this.vehicleId = holdTime.getVehicleId();
        this.tripId = holdTime.getTripId();
    }

    @Override
    public String toString() {
        return "HoldingTimeCacheKey [stopid=" + stopid + ", vehicleId=" + vehicleId + ", tripId=" + tripId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stopid == null) ? 0 : stopid.hashCode());
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HoldingTimeCacheKey other = (HoldingTimeCacheKey) obj;
        if (stopid == null) {
            if (other.stopid != null) return false;
        } else if (!stopid.equals(other.stopid)) return false;
        if (vehicleId == null) {
            return other.vehicleId == null;
        } else return vehicleId.equals(other.vehicleId);
    }

    public String getStopid() {
        return stopid;
    }

    public void setStopid(String stopid) {
        this.stopid = stopid;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
