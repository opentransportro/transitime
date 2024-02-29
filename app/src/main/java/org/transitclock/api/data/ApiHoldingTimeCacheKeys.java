/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcHoldingTimeCacheKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sean Óg Crudden
 */@Data
@XmlRootElement(name = "HoldingTimeCacheKeys")
public class ApiHoldingTimeCacheKeys {

    @XmlElement(name = "HoldingTimeCacheKey")
    private List<ApiHoldingTimeCacheKey> apiHoldingTimeCacheKeys;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiHoldingTimeCacheKeys() {}

    public ApiHoldingTimeCacheKeys(Collection<IpcHoldingTimeCacheKey> cacheKeys) {
        apiHoldingTimeCacheKeys = new ArrayList<ApiHoldingTimeCacheKey>();
        for (IpcHoldingTimeCacheKey key : cacheKeys) {
            apiHoldingTimeCacheKeys.add(new ApiHoldingTimeCacheKey(key));
        }
    }
}
