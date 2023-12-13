/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcVehicleToBlockConfig;

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
    protected ApiVehicleToBlockConfigs() {}

    /**
     * For constructing a ApiVehiclesDetails object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param agencyId
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     * @param assigned
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ApiVehicleToBlockConfigs(Collection<IpcVehicleToBlockConfig> vehicles)
            throws IllegalAccessException, InvocationTargetException {
        vehiclesData = new ArrayList<ApiVehicleToBlockConfig>();

        for (IpcVehicleToBlockConfig vehicleToBlock : vehicles) {
            vehiclesData.add(new ApiVehicleToBlockConfig(vehicleToBlock));
        }
    }
}
