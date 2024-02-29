/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import org.transitclock.api.resources.TransitimeApi.UiMode;
import org.transitclock.core.BlockAssignmentMethod;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.service.dto.IpcVehicleComplete;
import org.transitclock.utils.Time;

import java.lang.reflect.InvocationTargetException;

/**
 * Contains data for a single vehicle with additional info that is meant more for management than
 * for passengers.
 *
 * @author SkiBu Smith
 */
@Data
@XmlRootElement
@XmlType(
        propOrder = {
            "id",
            "vehicleName",
            "routeId",
            "routeShortName",
            "routeName",
            "headsign",
            "directionId",
            "vehicleType",
            "uiType",
            "schedBasedPreds",
            "loc",
            "scheduleAdherence",
            "scheduleAdherenceStr",
            "blockId",
            "blockAssignmentMethod",
            "tripId",
            "tripPatternId",
            "isDelayed",
            "isLayover",
            "layoverDepTime",
            "layoverDepTimeStr",
            "nextStopId",
            "nextStopName",
            "driverId",
            "holdingTime"
        })
public class ApiVehicleDetails extends ApiVehicleAbstract {

    @XmlAttribute
    private String routeName;

    @XmlAttribute
    private String vehicleName;

    // Note: needs to be Integer instead of an int because it can be null
    // (for vehicles that are not predictable)
    @XmlAttribute(name = "schAdh")
    private Integer scheduleAdherence;

    @XmlAttribute(name = "schAdhStr")
    private String scheduleAdherenceStr;

    @XmlAttribute(name = "block")
    private String blockId;

    @XmlAttribute(name = "blockMthd")
    private BlockAssignmentMethod blockAssignmentMethod;

    @XmlAttribute(name = "trip")
    private String tripId;

    @XmlAttribute(name = "tripPattern")
    private String tripPatternId;

    @XmlAttribute(name = "delayed")
    private Boolean isDelayed;

    @XmlAttribute(name = "layover")
    private Boolean isLayover;

    @XmlAttribute
    private Long layoverDepTime;

    @XmlAttribute
    private String layoverDepTimeStr;

    @XmlAttribute
    private String nextStopId;

    @XmlAttribute
    private String nextStopName;

    @XmlElement(name = "driver")
    private String driverId;

    @XmlAttribute
    private Boolean isScheduledService;

    @XmlAttribute
    private Long freqStartTime;

    @XmlAttribute
    private Boolean isAtStop;

    @XmlElement
    private ApiHoldingTime holdingTime;

    @XmlAttribute
    private double distanceAlongTrip;

    @XmlAttribute
    private String licensePlate;

    @XmlAttribute
    private boolean isCanceled;

    @XmlAttribute
    private double headway;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiVehicleDetails() {}

    /**
     * Takes a Vehicle object for client/server communication and constructs a ApiVehicle object for
     * the API.
     *
     * @param vehicle
     * @param timeForAgency So can output times in proper timezone
     * @param uiType Optional parameter. If should be labeled as "minor" in output for UI. Default
     *     is UiMode.NORMAL.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ApiVehicleDetails(IpcVehicle vehicle, Time timeForAgency, UiMode... uiType)
            throws IllegalAccessException, InvocationTargetException {
        super(vehicle, uiType.length > 0 ? uiType[0] : UiMode.NORMAL);

        routeName = vehicle.getRouteName();
        vehicleName = vehicle.getVehicleName();
        scheduleAdherence = vehicle.getRealTimeSchedAdh() != null
                ? vehicle.getRealTimeSchedAdh().getTemporalDifference()
                : null;
        scheduleAdherenceStr = vehicle.getRealTimeSchedAdh() != null
                ? vehicle.getRealTimeSchedAdh().toString()
                : null;
        blockId = vehicle.getBlockId();
        blockAssignmentMethod = vehicle.getBlockAssignmentMethod();
        tripId = vehicle.getTripId();
        tripPatternId = vehicle.getTripPatternId();
        isDelayed = vehicle.isDelayed() ? true : null;
        isLayover = vehicle.isLayover() ? true : null;
        layoverDepTime = vehicle.isLayover() ? vehicle.getLayoverDepartureTime() / Time.MS_PER_SEC : null;

        layoverDepTimeStr =
                vehicle.isLayover() ? timeForAgency.timeStrForTimezone(vehicle.getLayoverDepartureTime()) : null;

        nextStopId = vehicle.getNextStopId() != null ? vehicle.getNextStopId() : null;
        nextStopName = vehicle.getNextStopName() != null ? vehicle.getNextStopName() : null;
        driverId = vehicle.getAvl().getDriverId();
        licensePlate = vehicle.getLicensePlate();
        isCanceled = false;
        headway = -1;
        if (vehicle instanceof IpcVehicleComplete && tripId != null) {
            distanceAlongTrip = ((IpcVehicleComplete) vehicle).getDistanceAlongTrip();
            isCanceled = ((IpcVehicleComplete) vehicle).isCanceled();
            headway = ((IpcVehicleComplete) vehicle).getHeadway();
        }
        isScheduledService = vehicle.getFreqStartTime() > 0 ? false : true;
        if (!isScheduledService) freqStartTime = vehicle.getFreqStartTime();
        else freqStartTime = null;

        this.isAtStop = vehicle.isAtStop();

        if (vehicle.getHoldingTime() != null) {
            this.holdingTime = new ApiHoldingTime(vehicle.getHoldingTime());
        } else {
            this.holdingTime = null;
        }
    }
}
