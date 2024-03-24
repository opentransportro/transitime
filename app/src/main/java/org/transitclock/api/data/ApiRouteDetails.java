/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.domain.structs.Location;
import org.transitclock.service.dto.IpcDirection;
import org.transitclock.service.dto.IpcDirectionsForRoute;
import org.transitclock.service.dto.IpcRoute;
import org.transitclock.service.dto.IpcShape;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Provides detailed information for a route include stops and shape info.
 *
 * @author SkiBu Smith
 */
@Data
public class ApiRouteDetails {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String shortName;

    @JsonProperty
    private String longName;

    @JsonProperty
    private String color;

    @JsonProperty
    private String textColor;

    @JsonProperty
    private String type;

    @JsonProperty
    private List<ApiDirection> directions;

    @JsonProperty
    private List<ApiShape> shapes;

    @JsonProperty
    private ApiExtent extent;

    @JsonProperty
    private ApiLocation locationOfNextPredictedVehicle;

    public ApiRouteDetails(IpcRoute ipcRoute) {
        this.id = ipcRoute.getId();
        this.name = ipcRoute.getName();
        this.shortName = ipcRoute.getShortName();
        this.longName = ipcRoute.getLongName();
        this.color = ipcRoute.getColor();
        this.textColor = ipcRoute.getTextColor();
        this.type = ipcRoute.getType();

        IpcDirectionsForRoute stops = ipcRoute.getStops();
        this.directions = new ArrayList<>();
        for (IpcDirection ipcDirection : stops.getDirections()) {
            this.directions.add(new ApiDirection(ipcDirection));
        }

        this.shapes = new ArrayList<>();
        for (IpcShape shape : ipcRoute.getShapes()) {
            this.shapes.add(new ApiShape(shape));
        }

        this.extent = new ApiExtent(ipcRoute.getExtent());

        Location vehicleLoc = ipcRoute.getLocationOfNextPredictedVehicle();
        if (vehicleLoc == null) this.locationOfNextPredictedVehicle = null;
        else this.locationOfNextPredictedVehicle = new ApiLocation(vehicleLoc.getLat(), vehicleLoc.getLon());
    }
}
