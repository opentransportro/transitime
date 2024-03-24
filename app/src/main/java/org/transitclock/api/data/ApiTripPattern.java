/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.service.dto.IpcTripPattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A single trip pattern
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTripPattern {

    @JsonProperty
    private int configRev;

    @JsonProperty
    private String id;

    @JsonProperty
    private String headsign;

    @JsonProperty
    private String directionId;

    @JsonProperty
    private String routeId;

    @JsonProperty
    private String routeShortName;

    @JsonProperty
    private ApiExtent extent;

    @JsonProperty
    private String shapeId;

    @JsonProperty
    private List<ApiStopPath> stopPaths;


    /**
     * @param ipcTripPattern
     * @param includeStopPaths Stop paths are only included in output if this param set to true.
     */
    public ApiTripPattern(IpcTripPattern ipcTripPattern, boolean includeStopPaths) {
        configRev = ipcTripPattern.getConfigRev();
        id = ipcTripPattern.getId();
        headsign = ipcTripPattern.getHeadsign();
        directionId = ipcTripPattern.getDirectionId();
        routeId = ipcTripPattern.getRouteId();
        routeShortName = ipcTripPattern.getRouteShortName();
        extent = new ApiExtent(ipcTripPattern.getExtent());
        shapeId = ipcTripPattern.getShapeId();

        // Only include stop paths if actually want them. This
        // can greatly reduce volume of the output.
        if (includeStopPaths) {
            stopPaths = ipcTripPattern.getStopPaths()
                    .stream()
                    .map(ApiStopPath::new)
                    .toList();
        } else {
            stopPaths = new ArrayList<>();
        }
    }
}
