/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcRouteSummary;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a block in terse form, without schedule and trip pattern info
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "block")
public class ApiBlockTerse {

    @XmlAttribute
    private int configRev;

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String serviceId;

    @XmlAttribute
    private String startTime;

    @XmlAttribute
    private String endTime;

    @XmlElement
    private List<ApiTripTerse> trips;

    @XmlElement(name = "routes")
    private List<ApiRoute> routeSummaries;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiBlockTerse() {}

    public ApiBlockTerse(IpcBlock ipcBlock) {
        configRev = ipcBlock.getConfigRev();
        id = ipcBlock.getId();
        serviceId = ipcBlock.getServiceId();
        startTime = Time.timeOfDayStr(ipcBlock.getStartTime());
        endTime = Time.timeOfDayStr(ipcBlock.getEndTime());

        trips = new ArrayList<ApiTripTerse>();
        for (IpcTrip ipcTrip : ipcBlock.getTrips()) {
            trips.add(new ApiTripTerse(ipcTrip));
        }

        routeSummaries = new ArrayList<ApiRoute>();
        for (IpcRouteSummary ipcRouteSummary : ipcBlock.getRouteSummaries()) {
            routeSummaries.add(new ApiRoute(ipcRouteSummary));
        }
    }
}
