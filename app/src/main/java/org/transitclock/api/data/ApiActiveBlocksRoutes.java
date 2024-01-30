/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcTrip;

/**
 * A list of routes for when outputting active blocks
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "routes")
public class ApiActiveBlocksRoutes {

    @XmlElement(name = "routes")
    private List<ApiActiveBlocksRoute> routeData;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiActiveBlocksRoutes() {}

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcActiveBlock objects.
     *
     * @param activeBlocks Already ordered list of active blocks
     * @param agencyId
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ApiActiveBlocksRoutes(Collection<IpcActiveBlock> activeBlocks, String agencyId)
            throws IllegalAccessException, InvocationTargetException {
        routeData = new ArrayList<ApiActiveBlocksRoute>();

        ApiActiveBlocksRoute apiRoute = null;
        for (IpcActiveBlock activeBlock : activeBlocks) {
            IpcTrip trip = activeBlock.getBlock().getTrips().get(activeBlock.getActiveTripIndex());

            // If first block for the current route then create a new
            // ApiActiveBlocksRoute object to hold the info
            if (apiRoute == null || !apiRoute.getName().equals(trip.getRouteName())) {
                apiRoute = new ApiActiveBlocksRoute(trip.getRouteId(), trip.getRouteShortName(), trip.getRouteName());

                routeData.add(apiRoute);
            }

            // Add the block info to the ApiActiveBlocksRoute object
            apiRoute.add(activeBlock, agencyId);
        }
    }
}
