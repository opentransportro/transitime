/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.transitclock.domain.structs.Route;
import org.transitclock.gtfs.DbConfig;

/**
 * Contains each direction for route, along with each stop for each direction.
 *
 * @author SkiBu Smith
 */
public class IpcDirectionsForRoute implements Serializable {

    private final List<IpcDirection> directions;

    public IpcDirectionsForRoute(DbConfig dbConfig, Route dbRoute) {
        directions = new ArrayList<>();

        // Determine the directions
        List<String> directionIds = dbRoute.getDirectionIds(dbConfig);

        // For each directionId...
        for (String directionId : directionIds) {
            IpcDirection ipcDirection = new IpcDirection(dbRoute, dbConfig, directionId);
            directions.add(ipcDirection);
        }
    }

    public IpcDirectionsForRoute(List<IpcDirection> directions) {
        this.directions = directions;
    }

    @Override
    public String toString() {
        return "IpcDirectionsForRoute [" + "directions=" + directions + "]";
    }

    public List<IpcDirection> getDirections() {
        return directions;
    }
}
