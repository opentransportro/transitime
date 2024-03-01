/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcDirection;
import org.transitclock.service.dto.IpcDirectionsForRoute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of directions.
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "directions")
public class ApiDirections {

    @XmlElement(name = "direction")
    private List<ApiDirection> directionsData;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiDirections() {}

    public ApiDirections(IpcDirectionsForRoute stopsForRoute) {
        Collection<IpcDirection> directions = stopsForRoute.getDirections();
        directionsData = new ArrayList<ApiDirection>(directions.size());
        for (IpcDirection direction : directions) {
            directionsData.add(new ApiDirection(direction));
        }
    }
}
