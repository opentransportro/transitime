/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcVehicleToBlockConfig;

import java.util.ArrayList;
import java.util.List;

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

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleToBlockConfigs() {
    }

    /**
     * For constructing a ApiVehicleToBlockConfigs object from a Collection of VehicleToBlockConfig objects.
     *
     * @param vehicles
     */
    public ApiVehicleToBlockConfigs(List<IpcVehicleToBlockConfig> vehicles) {
        vehiclesData = new ArrayList<ApiVehicleToBlockConfig>();
        for (IpcVehicleToBlockConfig vehicleToBlock : vehicles) {
            vehiclesData.add(new ApiVehicleToBlockConfig(vehicleToBlock));
        }
    }
}
