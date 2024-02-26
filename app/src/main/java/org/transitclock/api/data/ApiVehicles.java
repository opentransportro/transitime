/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.service.dto.IpcVehicle;

/**
 * For when have list of Vehicles. By using this class can control the element name when data is
 * output.
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiVehicles {

    @XmlElement(name = "vehicles")
    private List<ApiVehicle> vehiclesData;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    public ApiVehicles() {}

    /**
     * For constructing a ApiVehicles object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     */
    public ApiVehicles(Collection<IpcVehicle> vehicles, Map<String, UiMode> uiTypesForVehicles) {
        vehiclesData = new ArrayList<ApiVehicle>();
        for (IpcVehicle vehicle : vehicles) {
            // Determine UI type for vehicle
            UiMode uiType = uiTypesForVehicles.get(vehicle.getId());

            // Add this vehicle to the ApiVehicle list
            vehiclesData.add(new ApiVehicle(vehicle, uiType));
        }
    }

    /**
     * For constructing a ApiVehicles object from a Collection of Vehicle objects. Sets UiMode to
     * UiMode.NORMAL.
     *
     * @param vehicles
     */
    public ApiVehicles(Collection<IpcVehicle> vehicles) {
        vehiclesData = new ArrayList<ApiVehicle>();
        for (IpcVehicle vehicle : vehicles) {
            // Add this vehicle to the ApiVehicle list
            vehiclesData.add(new ApiVehicle(vehicle, UiMode.NORMAL));
        }
    }
}
