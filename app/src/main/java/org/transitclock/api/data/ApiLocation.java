/* (C)2023 */
package org.transitclock.api.data;

import lombok.Data;
import org.transitclock.domain.structs.Location;

/**
 * A simple latitude/longitude.
 *
 * <p>This is a non-transient implementation of ApiTransientLocation. By not being transient this
 * class can be used to output a location as an element (as opposed to an attribute). By inheriting
 * from ApiTransientLocation don't need to duplicate any code.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiLocation extends ApiTransientLocation {

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiLocation() {}

    public ApiLocation(double lat, double lon) {
        super(lat, lon);
    }

    public ApiLocation(Location loc) {
        super(loc.getLat(), loc.getLon());
    }
}
