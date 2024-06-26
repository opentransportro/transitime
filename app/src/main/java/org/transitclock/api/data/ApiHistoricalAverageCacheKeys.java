/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;

/**
 * @author Sean Og Crudden
 */
@XmlRootElement(name = "HistoricalAverageCacheKeys")
public class ApiHistoricalAverageCacheKeys {

    @XmlElement(name = "HistoricalAverageCacheKey")
    private List<ApiHistoricalAverageCacheKey> apiHistoricalAverageCacheKeys;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiHistoricalAverageCacheKeys() {}

    public ApiHistoricalAverageCacheKeys(Collection<IpcHistoricalAverageCacheKey> cacheKeys) {
        apiHistoricalAverageCacheKeys = new ArrayList<ApiHistoricalAverageCacheKey>();
        for (IpcHistoricalAverageCacheKey key : cacheKeys) {
            apiHistoricalAverageCacheKeys.add(new ApiHistoricalAverageCacheKey(key));
        }
    }
}
