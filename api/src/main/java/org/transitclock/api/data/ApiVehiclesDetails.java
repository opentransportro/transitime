/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.db.structs.Agency;
import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.ipc.data.IpcVehicle;
import org.transitclock.utils.Time;

/**
 * For when have list of VehicleDetails. By using this class can control the element name when data
 * is output.
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiVehiclesDetails {

    @XmlElement(name = "vehicles")
    private List<ApiVehicleDetails> vehiclesData;

    /********************** Member Functions **************************/

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
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ApiVehiclesDetails(
            Collection<IpcVehicle> vehicles, String agencyId, Map<String, UiMode> uiTypesForVehicles, boolean assigned)
            throws IllegalAccessException, InvocationTargetException {
        // Get Time object based on timezone for agency
        WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
        Agency agency = webAgency.getAgency();
        Time timeForAgency = agency != null ? agency.getTime() : new Time((String) null);

        // Process each vehicle
        vehiclesData = new ArrayList<ApiVehicleDetails>();
        for (IpcVehicle vehicle : vehicles) {
            // Determine UI type for vehicle
            UiMode uiType = uiTypesForVehicles.get(vehicle.getId());
            if ((assigned && vehicle.getTripId() != null) || !assigned)
                vehiclesData.add(new ApiVehicleDetails(vehicle, timeForAgency, uiType));
        }
    }
}
