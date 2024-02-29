/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcKalmanErrorCacheKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sean Og Crudden
 */
@Data
@XmlRootElement(name = "KalmanErrorCacheKeys")
public class ApiKalmanErrorCacheKeys implements Serializable {

    @XmlElement(name = "KalmanErrorCacheKey")
    private List<ApiKalmanErrorCacheKey> apiKalmanErrorCacheKeys;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiKalmanErrorCacheKeys() {}

    public ApiKalmanErrorCacheKeys(Collection<IpcKalmanErrorCacheKey> cacheKeys) {
        apiKalmanErrorCacheKeys = new ArrayList<ApiKalmanErrorCacheKey>();
        for (IpcKalmanErrorCacheKey key : cacheKeys) {
            apiKalmanErrorCacheKeys.add(new ApiKalmanErrorCacheKey(key));
        }
    }
}
