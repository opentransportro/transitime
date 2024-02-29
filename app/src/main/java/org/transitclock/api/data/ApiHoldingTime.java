/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcHoldingTime;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
@Data
@XmlRootElement(name = "holdingtime")
public class ApiHoldingTime {

    @XmlAttribute
    private Date holdingTime;

    @XmlAttribute
    private Date creationTime;

    @XmlAttribute
    private Date currentTime;

    @XmlAttribute
    private String vehicleId;

    @XmlAttribute
    private String stopId;

    @XmlAttribute
    private String tripId;

    @XmlAttribute
    private String routeId;

    @XmlAttribute
    private boolean arrivalPredictionUsed;

    @XmlAttribute
    private boolean arrivalUsed;

    @XmlAttribute
    private Date arrivalTime;

    @XmlAttribute
    private boolean hasN1;

    @XmlAttribute
    private boolean hasN2;

    @XmlAttribute
    private boolean hasD1;

    @XmlAttribute
    private int numberPredictionsUsed;

    protected ApiHoldingTime() {}

    public ApiHoldingTime(IpcHoldingTime ipcHoldingTime) throws IllegalAccessException, InvocationTargetException {
        this.holdingTime = ipcHoldingTime.getHoldingTime();
        this.creationTime = ipcHoldingTime.getCreationTime();
        this.vehicleId = ipcHoldingTime.getVehicleId();
        this.tripId = ipcHoldingTime.getTripId();
        this.routeId = ipcHoldingTime.getRouteId();
        this.stopId = ipcHoldingTime.getStopId();
        this.arrivalPredictionUsed = ipcHoldingTime.isArrivalPredictionUsed();
        this.arrivalUsed = ipcHoldingTime.isArrivalUsed();
        this.currentTime = ipcHoldingTime.getCurrentTime();
        this.arrivalTime = ipcHoldingTime.getArrivalTime();
        this.hasD1 = ipcHoldingTime.isHasD1();
        this.numberPredictionsUsed = ipcHoldingTime.getNumberPredictionsUsed();
    }
}
