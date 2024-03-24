/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.domain.structs.Agency;
import org.transitclock.service.dto.IpcRoute;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * When have a list of routes.
 *
 * @author Michael
 */
@Data
public class ApiRoutesDetailsResponse {
    // So can easily get agency name when getting routes. Useful for db reports
    // and such.
    @JsonProperty("agency")
    private String agencyName;

    // List of route info
    @JsonProperty("routes")
    private List<ApiRouteDetails> routes;

    /**
     * For constructing a ApiRoutes object from a Collection of IpcRoute objects.
     *
     * @param routes
     * @param agency so can get agency name
     */
    public ApiRoutesDetailsResponse(Collection<IpcRoute> routes, Agency agency) {
        this.routes = new ArrayList<>();
        for (IpcRoute route : routes) {
            this.routes.add(new ApiRouteDetails(route));
        }

        // Also set agency name
        agencyName = agency.getName();
    }
}
