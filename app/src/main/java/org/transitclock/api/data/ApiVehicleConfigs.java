/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcVehicleConfig;

import java.util.*;

/**
 * For when have collection of ApiVehicleConfig
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "vehicleConfigs")
public class ApiVehicleConfigs {

    // Need to use @XmlElementRef so that the element name used for each
    // ApiVehicle object will be what is specified in the ApiVehicle class.
    @XmlElementRef
    private List<ApiVehicleConfig> vehicleConfigs;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleConfigs() {}

    /**
     * Constructs a ApiVehicleConfigs object using IpcVehicleConfig data obtained via IPC.
     *
     * @param vehicles
     */
    public ApiVehicleConfigs(Collection<IpcVehicleConfig> vehicles) {
        // Sort the vehicles by vehicle ID
        List<IpcVehicleConfig> vehiclesList = new ArrayList<IpcVehicleConfig>(vehicles);
        Collections.sort(vehiclesList, new Comparator<IpcVehicleConfig>() {
            @Override
            public int compare(final IpcVehicleConfig o1, final IpcVehicleConfig o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        // Create list of Api objects from the Ipc objects
        vehicleConfigs = new ArrayList<ApiVehicleConfig>();
        for (IpcVehicleConfig vehicle : vehiclesList) {
            vehicleConfigs.add(new ApiVehicleConfig(vehicle));
        }
    }
}
