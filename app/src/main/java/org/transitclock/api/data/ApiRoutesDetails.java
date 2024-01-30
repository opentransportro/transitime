/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.domain.structs.Agency;
import org.transitclock.service.dto.IpcRoute;

/**
 * When have a list of routes.
 *
 * @author Michael
 */
@XmlRootElement
public class ApiRoutesDetails {
    // So can easily get agency name when getting routes. Useful for db reports
    // and such.
    @XmlElement(name = "agency")
    private String agencyName;

    // List of route info
    @XmlElement(name = "routes")
    private List<ApiRouteDetails> routesData;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    public ApiRoutesDetails() {}

    /**
     * For constructing a ApiRoutes object from a Collection of IpcRoute objects.
     *
     * @param routes
     * @param agency so can get agency name
     */
    public ApiRoutesDetails(Collection<IpcRoute> routes, Agency agency) {
        routesData = new ArrayList<ApiRouteDetails>();
        for (IpcRoute route : routes) {
            routesData.add(new ApiRouteDetails(route));
        }

        // Also set agency name
        agencyName = agency.getName();
    }
}
