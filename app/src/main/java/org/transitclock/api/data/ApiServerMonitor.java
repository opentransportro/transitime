/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import org.transitclock.monitoring.MonitorResult;

/**
 * @author SkiBu Smith
 */
public class ApiServerMonitor {

    @XmlAttribute
    private String type;

    @XmlValue
    private String message;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiServerMonitor() {}

    public ApiServerMonitor(MonitorResult monitorResult) {
        this.type = monitorResult.getType();
        this.message = monitorResult.getMessage();
    }
}
