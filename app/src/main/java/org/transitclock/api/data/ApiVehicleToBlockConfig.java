/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import org.transitclock.service.dto.IpcVehicleToBlockConfig;

/**
 * Contains the data for a single vehicle.
 *
 * <p>Note: @XmlType(propOrder=""...) is used to get the elements to be output in desired order
 * instead of the default of alphabetical. This makes the resulting JSON/XML more readable.
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement
@XmlType(propOrder = {"id", "vehicleId", "blockId", "tripId", "validFrom", "validTo", "assignmentDate"})
public class ApiVehicleToBlockConfig extends ApiVehicleToBlockConfigAbstract {

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleToBlockConfig() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API. Sets UiMode to UiMode.NORMAL.
     *
     * @param vTBC
     */
    public ApiVehicleToBlockConfig(IpcVehicleToBlockConfig vTBC) {
        super(vTBC);
    }
}
