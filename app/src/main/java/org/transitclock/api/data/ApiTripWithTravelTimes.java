/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcTrip;

/**
 * Specifies how trip data along with travel times is formatted for the API.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "trip")
public class ApiTripWithTravelTimes extends ApiTrip {

    @XmlElement
    private ApiTravelTimes travelTimes;


    /** No args constructor needed for Jersey since this class is a @XmlRootElement */
    protected ApiTripWithTravelTimes() {}

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
