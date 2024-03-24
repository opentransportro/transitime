/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Sean Og Crudden
 */
@Data
public class ApiHistoricalAverageCacheKeysResponse {

    @JsonProperty
    private List<ApiHistoricalAverageCacheKey> data;

    public ApiHistoricalAverageCacheKeysResponse(Collection<IpcHistoricalAverageCacheKey> cacheKeys) {
        data = cacheKeys.stream()
                .map(ApiHistoricalAverageCacheKey::new)
                .toList();
    }
}
