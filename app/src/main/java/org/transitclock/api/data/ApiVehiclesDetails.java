/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.domain.structs.Agency;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.utils.Time;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * For when have list of VehicleDetails. By using this class can control the element name when data
 * is output.
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement
public class ApiVehiclesDetails {

    @XmlElement(name = "vehicles")
    private List<ApiVehicleDetails> vehiclesData;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehiclesDetails() {}

    /**
     * For constructing a ApiVehiclesDetails object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param agencyId
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     * @param assigned
     */
    public ApiVehiclesDetails(
            Collection<IpcVehicle> vehicles, String agencyId, Map<String, UiMode> uiTypesForVehicles, boolean assigned) {
        // Get Time object based on timezone for agency
        WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
        Agency agency = webAgency.getAgency();
        Time timeForAgency = agency != null ? agency.getTime() : new Time((String) null);

        // Process each vehicle
        vehiclesData = new ArrayList<>();
        for (IpcVehicle vehicle : vehicles) {
            // Determine UI type for vehicle
            UiMode uiType = uiTypesForVehicles.get(vehicle.getId());
            if ((assigned && vehicle.getTripId() != null) || !assigned)
                vehiclesData.add(new ApiVehicleDetails(vehicle, timeForAgency, uiType));
        }
    }
}
