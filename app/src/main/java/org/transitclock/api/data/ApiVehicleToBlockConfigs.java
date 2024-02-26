/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcVehicleToBlockConfig;

/**
 * For when have list of VehicleDetails. By using this class can control the element name when data
 * is output.
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiVehicleToBlockConfigs {

    @XmlElement(name = "vehicleToBlock")
    private List<ApiVehicleToBlockConfig> vehiclesData;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleToBlockConfigs() {}

    /**
     * For constructing a ApiVehiclesDetails object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param agencyId
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     * @param assigned
     */
    public ApiVehicleToBlockConfigs(Collection<IpcVehicleToBlockConfig> vehicles) {
        vehiclesData = new ArrayList<>();

        for (IpcVehicleToBlockConfig vehicleToBlock : vehicles) {
            vehiclesData.add(new ApiVehicleToBlockConfig(vehicleToBlock));
        }
    }
}
