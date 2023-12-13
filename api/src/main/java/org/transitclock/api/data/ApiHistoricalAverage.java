/* (C)2023 */
package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcHistoricalAverage;

/**
 * Describes an historical average
 *
 * @author Sean Og Crudden
 */
@XmlRootElement(name = "HistoricalAverage")
public class ApiHistoricalAverage {

    @XmlAttribute
    private Integer count;

    @XmlAttribute
    private Double average;

    /********************** Member Functions **************************/

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
