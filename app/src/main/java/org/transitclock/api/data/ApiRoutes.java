/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.domain.structs.Agency;
import org.transitclock.service.dto.IpcRoute;
import org.transitclock.service.dto.IpcRouteSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 */@Data
@XmlRootElement
public class ApiRoutes {
    // So can easily get agency name when getting routes. Useful for db reports
    // and such.
    @XmlElement(name = "agency")
    private String agencyName;

    // List of route info
    @XmlElement(name = "routes")
    private List<ApiRoute> routesData;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiRoutes() {}

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary objects.
     *
     * @param routes
     * @param agency so can get agency name
     */
    public ApiRoutes(Collection<IpcRouteSummary> routes, Agency agency) {
        routesData = new ArrayList<ApiRoute>();
        for (IpcRouteSummary route : routes) {
            ApiRoute routeSummary = new ApiRoute(route);
            routesData.add(routeSummary);
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
    public ApiRoutes(List<IpcRoute> routes, Agency agency) {
        routesData = new ArrayList<ApiRoute>();
        for (IpcRouteSummary route : routes) {
            ApiRoute routeSummary = new ApiRoute(route);
            routesData.add(routeSummary);
        }

        // Also set agency name
        agencyName = agency.getName();
    }
}
