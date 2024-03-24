/* (C)2023 */
package org.transitclock.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcKalmanErrorCacheKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Sean Og Crudden
 */
@Data
public class ApiKalmanErrorCacheKeysResponse implements Serializable {

    @JsonProperty
    private List<ApiKalmanErrorCacheKey> data;

    public ApiKalmanErrorCacheKeysResponse(Collection<IpcKalmanErrorCacheKey> cacheKeys) {
        data = cacheKeys.stream()
                .map(ApiKalmanErrorCacheKey::new)
                .toList();
    }
}
