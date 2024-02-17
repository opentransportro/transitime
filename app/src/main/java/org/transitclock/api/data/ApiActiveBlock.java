/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.service.dto.IpcActiveBlock;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.utils.Time;

/**
 * @author SkiBu Smith
 */
public class ApiActiveBlock {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String serviceId;

    @XmlAttribute
    private String startTime;

    @XmlAttribute
    private String endTime;

    @XmlElement(name = "trip")
    private ApiTripTerse apiTripSummary;

    @XmlElement(name = "vehicle")
    private Collection<ApiVehicleDetails> vehicles;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiActiveBlock() {}

    public ApiActiveBlock(IpcActiveBlock ipcActiveBlock, String agencyId)
            throws IllegalAccessException, InvocationTargetException {
        id = ipcActiveBlock.getBlock().getId();
        serviceId = ipcActiveBlock.getBlock().getServiceId();
        startTime = Time.timeOfDayStr(ipcActiveBlock.getBlock().getStartTime());
        endTime = Time.timeOfDayStr(ipcActiveBlock.getBlock().getEndTime());

        List<IpcTrip> trips = ipcActiveBlock.getBlock().getTrips();
        IpcTrip ipcTrip = trips.get(ipcActiveBlock.getActiveTripIndex());
        apiTripSummary = new ApiTripTerse(ipcTrip);

        // Get Time object based on timezone for agency
        WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
        Time timeForAgency = webAgency.getAgency().getTime();

        vehicles = new ArrayList<>();
        for (IpcVehicle ipcVehicles : ipcActiveBlock.getVehicles()) {
            vehicles.add(new ApiVehicleDetails(ipcVehicles, timeForAgency));
        }
    }

    public String getId() {
        return id;
    }

    public ApiTripTerse getApiTripSummary() {
        return apiTripSummary;
    }
}
