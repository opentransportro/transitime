/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.transitclock.domain.structs.Location;
import org.transitclock.service.dto.IpcStopPath;
import org.transitclock.utils.MathUtils;

/**
 * Represents a path from one stop to another.
 *
 * @author SkiBu Smith
 */
public class ApiStopPath {

    @XmlAttribute
    private int configRev;

    @XmlAttribute
    private String stopPathId;

    @XmlAttribute
    private String stopId;

    @XmlAttribute
    private String stopName;

    @XmlAttribute
    private int gtfsStopSeq;

    @XmlAttribute
    private Boolean layoverStop;

    @XmlAttribute
    private Boolean waitStop;

    @XmlAttribute
    private Boolean scheduleAdherenceStop;

    @XmlAttribute
    private Integer breakTime;

    @XmlElement
    private List<ApiLocation> locations;

    @XmlAttribute
    private Double pathLength;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiStopPath() {}

    public ApiStopPath(IpcStopPath ipcStopPath) {
        configRev = ipcStopPath.getConfigRev();
        stopPathId = ipcStopPath.getStopPathId();
        stopId = ipcStopPath.getStopId();
        stopName = ipcStopPath.getStopName();
        gtfsStopSeq = ipcStopPath.getGtfsStopSeq();
        layoverStop = ipcStopPath.isLayoverStop() ? true : null;
        waitStop = ipcStopPath.isWaitStop() ? true : null;
        scheduleAdherenceStop = ipcStopPath.isScheduleAdherenceStop() ? true : null;
        breakTime = ipcStopPath.getBreakTime() != 0 ? ipcStopPath.getBreakTime() : null;

        locations = new ArrayList<ApiLocation>();
        for (Location loc : ipcStopPath.getLocations()) {
            locations.add(new ApiLocation(loc.getLat(), loc.getLon()));
        }

        pathLength = MathUtils.round(ipcStopPath.getPathLength(), 1);
    }
}
