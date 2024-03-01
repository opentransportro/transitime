/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcHistoricalAverage;

/**
 * Describes an historical average
 *
 * @author Sean Og Crudden
 */@Data
@XmlRootElement(name = "HistoricalAverage")
public class ApiHistoricalAverage {

    @XmlAttribute
    private Integer count;

    @XmlAttribute
    private Double average;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiHistoricalAverage() {}

    public ApiHistoricalAverage(IpcHistoricalAverage ipcHistoricalAverage) {
        this.count = ipcHistoricalAverage.getCount();
        this.average = ipcHistoricalAverage.getAverage();
    }
}
