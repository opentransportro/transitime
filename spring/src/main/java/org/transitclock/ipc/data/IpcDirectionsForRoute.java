/* (C)2023 */
package org.transitclock.ipc.data;

import org.transitclock.core.domain.Route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains each direction for route, along with each stop for each direction.
 *
 * @author SkiBu Smith
 */
public class IpcDirectionsForRoute implements Serializable {

    private List<IpcDirection> directions;

    private static final long serialVersionUID = -3112277760645758349L;

    /********************** Member Functions **************************/
    public IpcDirectionsForRoute(Route dbRoute) {
        directions = new ArrayList<IpcDirection>();

        // Determine the directions
        List<String> directionIds = dbRoute.getDirectionIds();

        // For each directionId...
        for (String directionId : directionIds) {
            IpcDirection ipcDirection = new IpcDirection(dbRoute, directionId);
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
