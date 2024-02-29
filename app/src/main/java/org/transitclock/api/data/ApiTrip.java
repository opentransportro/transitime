/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcSchedTimes;
import org.transitclock.service.dto.IpcTrip;
import org.transitclock.utils.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies how trip data is formatted for the API.
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement(name = "trip")
public class ApiTrip {

    @XmlAttribute
    private int configRev;

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
    private String routeId;

    @XmlAttribute
    private String routeShortName;

    @XmlElement
    private ApiTripPattern tripPattern;

    @XmlAttribute
    private String serviceId;

    @XmlAttribute
    private String headsign;

    @XmlAttribute
    private String blockId;

    @XmlAttribute
    private String shapeId;

    // Using a Boolean so that will be output only if true
    @XmlAttribute
    private Boolean noSchedule;

    @XmlElement(name = "schedule")
    private List<ApiScheduleArrDepTime> scheduleTimes;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTrip() {}

    /**
     * @param ipcTrip
     * @param includeStopPaths Stop paths are only included in output if this param set to true.
     */
    public ApiTrip(IpcTrip ipcTrip, boolean includeStopPaths) {
        configRev = ipcTrip.getConfigRev();
        id = ipcTrip.getId();
        shortName = ipcTrip.getShortName();
        startTime = Time.timeOfDayStr(ipcTrip.getStartTime());
        endTime = Time.timeOfDayStr(ipcTrip.getEndTime());
        directionId = ipcTrip.getDirectionId();
        routeId = ipcTrip.getRouteId();
        routeShortName = ipcTrip.getRouteShortName();
        tripPattern = new ApiTripPattern(ipcTrip.getTripPattern(), includeStopPaths);
        serviceId = ipcTrip.getServiceId();
        headsign = ipcTrip.getHeadsign();
        blockId = ipcTrip.getBlockId();
        shapeId = ipcTrip.getShapeId();

        noSchedule = ipcTrip.isNoSchedule() ? true : null;

        scheduleTimes = new ArrayList<ApiScheduleArrDepTime>();
        for (IpcSchedTimes ipcScheduleTimes : ipcTrip.getScheduleTimes()) {
            scheduleTimes.add(new ApiScheduleArrDepTime(ipcScheduleTimes));
        }
    }
}
