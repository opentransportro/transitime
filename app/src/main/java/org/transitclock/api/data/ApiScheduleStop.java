/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Data;

/**
 * Contains minimal for a stop for a schedule
 *
 * @author Michael
 */
@Data
public class ApiScheduleStop {
    @XmlAttribute
    private String stopId;

    @XmlAttribute
    private String stopName;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiScheduleStop() {}

    public ApiScheduleStop(String stopId, String stopName) {
        this.stopId = stopId;
        this.stopName = stopName;
    }
}
