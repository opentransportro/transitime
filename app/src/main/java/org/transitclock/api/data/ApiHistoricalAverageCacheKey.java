/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;

/**
 * Describes an historical average key which is used to refer to data elements in the cache
 *
 * @author Sean Og Crudden
 */@Data
@XmlRootElement(name = "HistoricalAverageCacheKey")
public class ApiHistoricalAverageCacheKey {

    @XmlAttribute
    private String tripId;

    @XmlAttribute
    private Integer stopPathIndex;

    public ApiHistoricalAverageCacheKey() {}

    public ApiHistoricalAverageCacheKey(IpcHistoricalAverageCacheKey key) {

        this.tripId = key.getTripId();
        this.stopPathIndex = key.getStopPathIndex();
    }
}
