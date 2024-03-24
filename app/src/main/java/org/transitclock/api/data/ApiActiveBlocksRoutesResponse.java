/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcTrip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A list of routes for when outputting active blocks
 *
 * @author SkiBu Smith
 */
@Data
public class ApiActiveBlocksRoutesResponse {

    @JsonProperty("data")
    private List<ApiActiveBlocksRoute> data;

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcActiveBlock objects.
     *
     * @param activeBlocks Already ordered list of active blocks
     * @param agencyId
     */
    public ApiActiveBlocksRoutesResponse(Collection<IpcActiveBlock> activeBlocks, String agencyId) {
        data = new ArrayList<>();

        ApiActiveBlocksRoute apiRoute = null;
        for (IpcActiveBlock activeBlock : activeBlocks) {
            IpcBlock block = activeBlock.getBlock();
            List<IpcTrip> trips = block.getTrips();

            IpcTrip trip = trips.get(activeBlock.getActiveTripIndex());

            // If first block for the current route then create a new
            // ApiActiveBlocksRoute object to hold the info
            if (apiRoute == null || !Objects.equals(apiRoute.getName(), trip.getRouteName())) {
                apiRoute = new ApiActiveBlocksRoute(trip.getRouteId(), trip.getRouteShortName(), trip.getRouteName());

                data.add(apiRoute);
            }

            // Add the block info to the ApiActiveBlocksRoute object
            apiRoute.add(activeBlock, agencyId);
        }
    }
}
