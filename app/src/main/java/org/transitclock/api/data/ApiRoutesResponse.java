/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.domain.structs.Agency;
import org.transitclock.service.dto.IpcRoute;
import org.transitclock.service.dto.IpcRouteSummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiRoutesResponse {
    // So can easily get agency name when getting routes. Useful for db reports
    // and such.
    @JsonProperty
    private String agencyName;

    // List of route info
    @JsonProperty
    private List<ApiRoute> routes;

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary objects.
     *
     * @param routes
     * @param agency so can get agency name
     */
    public ApiRoutesResponse(Collection<IpcRouteSummary> routes, Agency agency) {
        this.routes = new ArrayList<>();
        for (IpcRouteSummary route : routes) {
            ApiRoute routeSummary = new ApiRoute(route);
            this.routes.add(routeSummary);
        }

        // Also set agency name
        agencyName = agency.getName();
    }

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcRoute objects.
     *
     * @param routes
     * @param agency so can get agency name
     */
    public ApiRoutesResponse(List<IpcRoute> routes, Agency agency) {
        this.routes = new ArrayList<>();
        for (IpcRouteSummary route : routes) {
            ApiRoute routeSummary = new ApiRoute(route);
            this.routes.add(routeSummary);
        }

        // Also set agency name
        agencyName = agency.getName();
    }
}
