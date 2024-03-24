/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes an historical average key which is used to refer to data elements in the cache
 *
 * @author Sean Og Crudden
 */
@Data
public class ApiHistoricalAverageCacheKey {

    @JsonProperty
    private String tripId;

    @JsonProperty
    private Integer stopPathIndex;

    public ApiHistoricalAverageCacheKey(IpcHistoricalAverageCacheKey key) {
        this.tripId = key.getTripId();
        this.stopPathIndex = key.getStopPathIndex();
    }
}
