/* (C)2023 */
package org.transitclock.api.data;

import java.io.Serializable;

import org.transitclock.service.dto.IpcKalmanErrorCacheKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes an kalman error key which is used to refer to data elements in the kalman error cache
 *
 * @author Sean Og Crudden
 */
@Data
public class ApiKalmanErrorCacheKey implements Serializable {
    @JsonProperty
    private String tripId;

    @JsonProperty
    private Integer stopPathIndex;

    public ApiKalmanErrorCacheKey(IpcKalmanErrorCacheKey key) {
        this.tripId = key.getTripId();
        this.stopPathIndex = key.getStopPathIndex();
    }
}
