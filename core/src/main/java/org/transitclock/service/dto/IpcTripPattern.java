/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.transitclock.domain.structs.Extent;
import org.transitclock.domain.structs.StopPath;
import org.transitclock.domain.structs.TripPattern;

/**
 * Configuration information for a TripPattern. For IPC.
 *
 * @author SkiBu Smith
 */
public class IpcTripPattern implements Serializable {

    private final int configRev;
    private final String id;
    private final String headsign;
    private final String directionId;
    private final String routeId;
    private final String routeShortName;
    private final Extent extent;
    private final String shapeId;
    private final List<IpcStopPath> stopPaths;

    public IpcTripPattern(TripPattern dbTripPattern) {
        this.configRev = dbTripPattern.getConfigRev();
        this.id = dbTripPattern.getId();
        this.headsign = dbTripPattern.getHeadsign();
        this.directionId = dbTripPattern.getDirectionId();
        this.routeId = dbTripPattern.getRouteId();
        this.routeShortName = dbTripPattern.getRouteShortName();
        this.extent = dbTripPattern.getExtent();
        this.shapeId = dbTripPattern.getShapeId();

        this.stopPaths = new ArrayList<IpcStopPath>();
        for (StopPath stopPath : dbTripPattern.getStopPaths()) this.stopPaths.add(new IpcStopPath(stopPath));
    }

    @Override
    public String toString() {
        return "IpcTripPattern ["
                + "configRev="
                + configRev
                + ", id="
                + id
                + ", headsign="
                + headsign
                + ", directionId="
                + directionId
                + ", routeId="
                + routeId
                + ", routeShortName="
                + routeShortName
                + ", extent="
                + extent
                + ", shapeId="
                + shapeId
                + ", stopPaths="
                + stopPaths
                + "]";
    }

    public int getConfigRev() {
        return configRev;
    }

    public String getId() {
        return id;
    }

    public String getHeadsign() {
        return headsign;
    }

    public String getDirectionId() {
        return directionId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public Extent getExtent() {
        return extent;
    }

    public String getShapeId() {
        return shapeId;
    }

    public List<IpcStopPath> getStopPaths() {
        return stopPaths;
    }
}
