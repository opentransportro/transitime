/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.service.dto.IpcVehicle;

import lombok.Data;

/**
 * Contains the data for a single vehicle.
 *
 * <p>Note: @XmlType(propOrder=""...) is used to get the elements to be output in desired order
 * instead of the default of alphabetical. This makes the resulting JSON/XML more readable.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiVehicle extends ApiVehicleAbstract {

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
