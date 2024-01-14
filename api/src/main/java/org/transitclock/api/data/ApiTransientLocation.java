/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import org.transitclock.utils.ChinaGpsOffset;
import org.transitclock.utils.MathUtils;

/**
 * A simple latitude/longitude:
 *
 * <p>Note: this class marked as @XmlTransient so that ApiLocations are not part of the domain
 * model. This means that can't instantiate as an element. The reason doing this is so that the
 * subclass can set propOrder for all elements in the subclass, including lat & lon. Explanation of
 * this is in http://blog.bdoughan.com/2011/06/ignoring-inheritance-with-xmltransient.html
 *
 * @author SkiBu Smith
 */
@XmlTransient
public class ApiTransientLocation {

    @XmlAttribute
    private double lat;

    @XmlAttribute
    private double lon;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTransientLocation() {}

    public ApiTransientLocation(double lat, double lon) {
        // If location is in China (approximately) then adjust lat & lon so
        // that will be displayed properly on map.
        ChinaGpsOffset.LatLon latLon = ChinaGpsOffset.transform(lat, lon);

        // Output only 5 digits past decimal point
        this.lat = MathUtils.round(latLon.getLat(), 5);
        // Output only 5 digits past decimal point
        this.lon = MathUtils.round(latLon.getLon(), 5);
    }
}
