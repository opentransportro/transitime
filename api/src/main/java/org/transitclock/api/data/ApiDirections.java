/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcDirection;
import org.transitclock.ipc.data.IpcDirectionsForRoute;

/**
 * A list of directions.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "directions")
public class ApiDirections {

    @XmlElement(name = "direction")
    private List<ApiDirection> directionsData;

    /********************** Member Functions **************************/

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
