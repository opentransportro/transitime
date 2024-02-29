/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcKalmanErrorCacheKey;

import java.io.Serializable;

/**
 * Describes an kalman error key which is used to refer to data elements in the kalman error cache
 *
 * @author Sean Og Crudden
 */
@Data
@XmlRootElement(name = "KalmanErrorCacheKey")
public class ApiKalmanErrorCacheKey implements Serializable {

    @XmlAttribute
    private String tripId;

    @XmlAttribute
    private Integer stopPathIndex;

    public ApiKalmanErrorCacheKey() {}

    public ApiKalmanErrorCacheKey(IpcKalmanErrorCacheKey key) {

        this.tripId = key.getTripId();
        this.stopPathIndex = key.getStopPathIndex();
    }
}
