/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.domain.structs.Location;
import org.transitclock.service.dto.IpcDirection;
import org.transitclock.service.dto.IpcDirectionsForRoute;
import org.transitclock.service.dto.IpcRoute;
import org.transitclock.service.dto.IpcShape;

/**
 * Provides detailed information for a route include stops and shape info.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "route")
public class ApiRouteDetails {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String shortName;

    @XmlAttribute
    private String longName;

    @XmlAttribute
    private String color;

    @XmlAttribute
    private String textColor;

    @XmlAttribute
    private String type;

    @XmlElement(name = "direction")
    private List<ApiDirection> directions;

    @XmlElement(name = "shape")
    private List<ApiShape> shapes;

    @XmlElement
    private ApiExtent extent;

    @XmlElement
    private ApiLocation locationOfNextPredictedVehicle;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiRouteDetails() {}

    public ApiRouteDetails(IpcRoute ipcRoute) {
        this.id = ipcRoute.getId();
        this.name = ipcRoute.getName();
        this.shortName = ipcRoute.getShortName();
        this.longName = ipcRoute.getLongName();
        this.color = ipcRoute.getColor();
        this.textColor = ipcRoute.getTextColor();
        this.type = ipcRoute.getType();

        IpcDirectionsForRoute stops = ipcRoute.getStops();
        this.directions = new ArrayList<ApiDirection>();
        for (IpcDirection ipcDirection : stops.getDirections()) {
            this.directions.add(new ApiDirection(ipcDirection));
        }

        this.shapes = new ArrayList<ApiShape>();
        for (IpcShape shape : ipcRoute.getShapes()) {
            this.shapes.add(new ApiShape(shape));
        }

        this.extent = new ApiExtent(ipcRoute.getExtent());

        Location vehicleLoc = ipcRoute.getLocationOfNextPredictedVehicle();
        if (vehicleLoc == null) this.locationOfNextPredictedVehicle = null;
        else this.locationOfNextPredictedVehicle = new ApiLocation(vehicleLoc);
    }
}
