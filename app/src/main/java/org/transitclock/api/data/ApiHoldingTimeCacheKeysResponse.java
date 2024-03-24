/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.service.dto.IpcHoldingTimeCacheKey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Sean Óg Crudden
 */
@Data
public class ApiHoldingTimeCacheKeysResponse {

    @JsonProperty
    private List<ApiHoldingTimeCacheKey> data;

    public ApiHoldingTimeCacheKeysResponse(Collection<IpcHoldingTimeCacheKey> cacheKeys) {
        data = cacheKeys.stream()
                .map(ApiHoldingTimeCacheKey::new)
                .toList();
    }
}
