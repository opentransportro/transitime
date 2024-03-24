/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.structs.Location;
import org.transitclock.service.dto.IpcStopPath;
import org.transitclock.utils.MathUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a path from one stop to another.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiStopPath {

    @JsonProperty
    private int configRev;

    @JsonProperty
    private String stopPathId;

    @JsonProperty
    private String stopId;

    @JsonProperty
    private String stopName;

    @JsonProperty
    private int gtfsStopSeq;

    @JsonProperty
    private Boolean layoverStop;

    @JsonProperty
    private Boolean waitStop;

    @JsonProperty
    private Boolean scheduleAdherenceStop;

    @JsonProperty
    private Integer breakTime;

    @JsonProperty
    private List<ApiLocation> locations;

    @JsonProperty
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

        locations = new ArrayList<>();
        for (Location loc : ipcStopPath.getLocations()) {
            locations.add(new ApiLocation(loc.getLat(), loc.getLon()));
        }

        pathLength = MathUtils.round(ipcStopPath.getPathLength(), 1);
    }
}
