/* (C)2023 */
package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcTrip;
import org.transitclock.utils.Time;

/**
 * A shorter version of ApiTrip for when all the detailed info is not needed.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "trip")
public class ApiTripTerse {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String shortName;

    @XmlAttribute
    private String startTime;

    @XmlAttribute
    private String endTime;

    @XmlAttribute
    private String directionId;

    @XmlAttribute
    private String headsign;

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String routeShortName;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTripTerse() {}

    public ApiTripTerse(IpcTrip ipcTrip) {
        id = ipcTrip.getId();
        shortName = ipcTrip.getShortName();
        startTime = Time.timeOfDayStr(ipcTrip.getStartTime());
        endTime = Time.timeOfDayStr(ipcTrip.getEndTime());
        directionId = ipcTrip.getDirectionId();
        headsign = ipcTrip.getHeadsign();
        routeId = ipcTrip.getRouteId();
        routeShortName = ipcTrip.getRouteShortName();
    }

    public String getRouteId() {
        return routeId;
    }
}
