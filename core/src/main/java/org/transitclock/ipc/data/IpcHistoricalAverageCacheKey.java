/* (C)2023 */
package org.transitclock.ipc.data;

import java.io.Serializable;
import org.transitclock.core.dataCache.StopPathCacheKey;

public class IpcHistoricalAverageCacheKey implements Serializable {
    private String tripId;
    private Integer stopPathIndex;

    public IpcHistoricalAverageCacheKey(StopPathCacheKey key) {
        super();
        this.tripId = key.getTripId();
        this.stopPathIndex = key.getStopPathIndex();
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Integer getStopPathIndex() {
        return stopPathIndex;
    }

    public void setStopPathIndex(Integer stopPathIndex) {
        this.stopPathIndex = stopPathIndex;
    }
}
