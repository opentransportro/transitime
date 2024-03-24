/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.domain.structs.Location;
import org.transitclock.utils.ChinaGpsOffset;
import org.transitclock.utils.MathUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A simple latitude/longitude.
 *
 * <p>This is a non-transient implementation of ApiTransientLocation. By not being transient this
 * class can be used to output a location as an element (as opposed to an attribute). By inheriting
 * from ApiTransientLocation don't need to duplicate any code.
 *
 * @author SkiBu Smith
 */
@Getter @Setter
@ToString
@EqualsAndHashCode
public class ApiLocation {

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private double lat;

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private double lon;

    public ApiLocation(double lat, double lon) {
        // If location is in China (approximately) then adjust lat & lon so
        // that will be displayed properly on map.
        ChinaGpsOffset.LatLon latLon = ChinaGpsOffset.transform(lat, lon);

        // Output only 5 digits past decimal point
        this.lat = MathUtils.round(latLon.getLat(), 5);
        // Output only 5 digits past decimal point
        this.lon = MathUtils.round(latLon.getLon(), 5);
    }
}
