/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Data;
import org.transitclock.domain.structs.Extent;
import org.transitclock.utils.MathUtils;

/**
 * Describes the extent of a route or agency via a min & max lat & lon.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiExtent {

    @XmlAttribute
    private double minLat;

    @XmlAttribute
    private double minLon;

    @XmlAttribute
    private double maxLat;

    @XmlAttribute
    private double maxLon;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiExtent() {}

    public ApiExtent(Extent extent) {
        this.minLat = MathUtils.round(extent.getMinLat(), 5);
        this.minLon = MathUtils.round(extent.getMinLon(), 5);
        this.maxLat = MathUtils.round(extent.getMaxLat(), 5);
        this.maxLon = MathUtils.round(extent.getMaxLon(), 5);
    }
}
