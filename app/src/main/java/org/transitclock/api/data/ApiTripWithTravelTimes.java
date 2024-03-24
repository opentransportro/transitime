/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcTrip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specifies how trip data along with travel times is formatted for the API.
 *
 * @author SkiBu Smith
 */
@Getter @Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ApiTripWithTravelTimes extends ApiTrip {

    @JsonProperty
    private ApiTravelTimes travelTimes;

    /**
     * Constructor
     *
     * @param ipcTrip
     * @param includeStopPaths
     */
    public ApiTripWithTravelTimes(IpcTrip ipcTrip, boolean includeStopPaths) {
        super(ipcTrip, includeStopPaths);
        travelTimes = new ApiTravelTimes(ipcTrip.getTravelTimes());
    }
}
