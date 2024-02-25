/* (C)2023 */
package org.transitclock.api.data.siri;

import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.utils.Geo;

/**
 * Location object for SIRI
 *
 * @author SkiBu Smith
 */
public class SiriLocation {

    @XmlElement(name = "Longitude")
    private String longitude;

    @XmlElement(name = "Latitude")
    private String latitude;

    /**
     * Need a no-arg constructor for Jersey for JSON. Otherwise get really obtuse "MessageBodyWriter
     * not found for media type=application/json" exception.
     */
    protected SiriLocation() {}

    public SiriLocation(double latitude, double longitude) {
        this.longitude = Geo.format(longitude);
        this.latitude = Geo.format(latitude);
    }
}
