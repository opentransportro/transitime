/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.service.dto.IpcVehicle;

/**
 * This class exists so that can have multiple subclasses that inherent from each other while still
 * being able to set the propOrder for each class. Specifically, ApiVehicleDetails is supposed to be
 * a subclass of ApiVehicle. But want the vehicle id to be output as the first attribute. But the
 * attributes for the subclass are output first and one can't normally set the propOrder of parent
 * class attributes. One gets an internal error if one tries to do so.
 *
 * <p>The solution is to use the abstract class ApiVehicleAbstract. Then can implement ApiVehicle
 * and ApiVehicleDetails to inherit from ApiVehicleAbstract and those classes can each set propOrder
 * as desired. Yes, this is rather complicated, but it works.
 *
 * @author SkiBu Smith
 */
@Data
@XmlTransient
public abstract class ApiVehicleAbstract {

    @XmlAttribute
    protected String id;

    @XmlElement
    protected ApiGpsLocation loc;

    @XmlAttribute
    protected String routeId;

    @XmlAttribute
    protected String routeShortName;

    @XmlAttribute
    protected String headsign;

    @XmlAttribute(name = "direction")
    protected String directionId;

    @XmlAttribute
    protected String vehicleType;

    // Whether NORMAL, SECONDARY, or MINOR. Specifies how vehicle should
    // be drawn in the UI
    @XmlAttribute
    protected String uiType;

    @XmlAttribute(name = "scheduleBased")
    protected Boolean schedBasedPreds;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleAbstract() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API.
     *
     * @param vehicle
     * @param uiType If should be labeled as "minor" in output for UI.
     */
    public ApiVehicleAbstract(IpcVehicle vehicle, UiMode uiType) {
        id = vehicle.getId();
        loc = new ApiGpsLocation(vehicle);
        routeId = vehicle.getRouteId();
        routeShortName = vehicle.getRouteShortName();
        headsign = vehicle.getHeadsign();
        directionId = vehicle.getDirectionId();

        // Set GTFS vehicle type. If it was not set in the config then use
        // default value of "3" which is for buses.
        vehicleType = vehicle.getVehicleType();
        if (vehicleType == null) vehicleType = "3";

        // Determine UI type. Usually will be displaying vehicles
        // as NORMAL. To simplify API use null for this case.
        this.uiType = null;
        if (uiType == UiMode.SECONDARY) this.uiType = "secondary";
        else if (uiType == UiMode.MINOR) this.uiType = "minor";

        this.schedBasedPreds = vehicle.isForSchedBasedPred() ? true : null;
    }
}
