/* (C)2023 */
package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.transitclock.api.rootResources.TransitimeApi.UiMode;
import org.transitclock.ipc.data.IpcVehicle;

/**
 * Contains the data for a single vehicle.
 *
 * <p>Note: @XmlType(propOrder=""...) is used to get the elements to be output in desired order
 * instead of the default of alphabetical. This makes the resulting JSON/XML more readable.
 *
 * @author SkiBu Smith
 */
@XmlRootElement
@XmlType(
        propOrder = {
            "id",
            "routeId",
            "routeShortName",
            "headsign",
            "directionId",
            "vehicleType",
            "uiType",
            "schedBasedPreds",
            "loc"
        })
public class ApiVehicle extends ApiVehicleAbstract {

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicle() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API.
     *
     * @param vehicle
     * @param uiType If should be labeled as "minor" in output for UI.
     */
    public ApiVehicle(IpcVehicle vehicle, UiMode uiType) {
        super(vehicle, uiType);
    }

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API. Sets UiMode to UiMode.NORMAL.
     *
     * @param vehicle
     */
    public ApiVehicle(IpcVehicle vehicle) {
        super(vehicle, UiMode.NORMAL);
    }
}
