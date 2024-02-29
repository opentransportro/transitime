/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import org.transitclock.service.dto.IpcStopPath;
import org.transitclock.service.dto.IpcTripPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * A single trip pattern
 *
 * @author SkiBu Smith
 */
@Data
public class ApiTripPattern {

    @XmlAttribute
    private int configRev;

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String headsign;

    @XmlAttribute
    private String directionId;

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private String routeShortName;

    @XmlElement
    private ApiExtent extent;

    @XmlAttribute
    private String shapeId;

    @XmlElement
    private List<ApiStopPath> stopPaths;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiTripPattern() {}

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
            stopPaths = new ArrayList<ApiStopPath>();
            for (IpcStopPath ipcStopPath : ipcTripPattern.getStopPaths()) {
                stopPaths.add(new ApiStopPath(ipcStopPath));
            }
        } else {
            stopPaths = null;
        }
    }
}
