/* (C)2023 */
package org.transitclock.core.dataCache;

import org.transitclock.core.Indices;

/**
 * @author Sean Og Crudden TODO This is the same as StopPathCacheKey but left seperate in case we
 *     might use block_id as well.
 */
public class KalmanErrorCacheKey implements java.io.Serializable {


    private String tripId;
    private Integer stopPathIndex;

    // The vehicleId is only used for debug purposed we know in log which vehicle set the error
    // value
    private String vehiceId;

    public KalmanErrorCacheKey(Indices indices, String vehicleId) {
        this.tripId = indices.getBlock().getTrip(indices.getTripIndex()).getId();
        this.stopPathIndex = indices.getStopPathIndex();
        this.vehiceId = vehicleId;
    }

    public KalmanErrorCacheKey(Indices indices) {
        this.tripId = indices.getBlock().getTrip(indices.getTripIndex()).getId();
        this.stopPathIndex = indices.getStopPathIndex();
    }

    public KalmanErrorCacheKey(String tripId, Integer stopPathIndex) {
        this.tripId = tripId;
        this.stopPathIndex = stopPathIndex;
    }


    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setStopPathIndex(Integer stopPathIndex) {
        this.stopPathIndex = stopPathIndex;
    }


    public String getVehiceId() {
        return vehiceId;
    }

    public void setVehiceId(String vehiceId) {
        this.vehiceId = vehiceId;
    }

    @Override
    public String toString() {
        return "KalmanErrorCacheKey [tripId=" + tripId + ", stopPathIndex=" + stopPathIndex + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stopPathIndex == null) ? 0 : stopPathIndex.hashCode());
        result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        KalmanErrorCacheKey other = (KalmanErrorCacheKey) obj;
        if (stopPathIndex == null) {
            if (other.stopPathIndex != null) return false;
        } else if (!stopPathIndex.equals(other.stopPathIndex)) return false;
        if (tripId == null) {
            return other.tripId == null;
        } else return tripId.equals(other.tripId);
    }

    /**
     * @return the stopPathIndex
     */
    public int getStopPathIndex() {
        return stopPathIndex;
    }

    /**
     * @param stopPathIndex the stopPathIndex to set
     */
    public void setStopPathIndex(int stopPathIndex) {
        this.stopPathIndex = stopPathIndex;
    }
}
