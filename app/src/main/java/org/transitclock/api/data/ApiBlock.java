/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcBlock;
import org.transitclock.service.dto.IpcRouteSummary;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

/**
 * Describes a block
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "block")
public class ApiBlock {

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
    private List<ApiTrip> trips;

    @XmlElement(name = "routes")
    private List<ApiRoute> routeSummaries;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiBlock() {}

    public ApiBlock(IpcBlock ipcBlock) {
        configRev = ipcBlock.getConfigRev();
        id = ipcBlock.getId();
        serviceId = ipcBlock.getServiceId();
        startTime = Time.timeOfDayStr(ipcBlock.getStartTime());
        endTime = Time.timeOfDayStr(ipcBlock.getEndTime());

        trips = new ArrayList<ApiTrip>();
        for (IpcTrip ipcTrip : ipcBlock.getTrips()) {
            // Note: not including stop paths in trip pattern output
            // because that can be really voluminous.
            trips.add(new ApiTrip(ipcTrip, false));
        }

        routeSummaries = new ArrayList<ApiRoute>();
        for (IpcRouteSummary ipcRouteSummary : ipcBlock.getRouteSummaries()) {
            routeSummaries.add(new ApiRoute(ipcRouteSummary));
        }
    }
}
