/* (C)2023 */
package org.transitclock.api.data;

import java.util.Date;

import org.transitclock.service.dto.IpcVehicleToBlockConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ApiVehicleToBlockConfig {

    @JsonProperty
    protected long id;

    @JsonProperty
    protected String vehicleId;

    @JsonProperty
    protected Date validFrom;

    @JsonProperty
    protected Date validTo;

    @JsonProperty
    protected Date assignmentDate;

    @JsonProperty
    protected String tripId;

    @JsonProperty
    protected String blockId;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleToBlockConfig() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API.
     *
     * @param vehicle
     * @param uiType If should be labeled as "minor" in output for UI.
     */
    public ApiVehicleToBlockConfig(IpcVehicleToBlockConfig vTBC) {
        id = vTBC.getId();
        vehicleId = vTBC.getVehicleId();
        tripId = vTBC.getTripId();
        blockId = vTBC.getBlockId();
        validFrom = vTBC.getValidFrom();
        validTo = vTBC.getValidTo();
        assignmentDate = vTBC.getAssignmentDate();
    }
}
