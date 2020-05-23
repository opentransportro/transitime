package org.transitclock.api.data;

import org.transitclock.ipc.data.IpcHoldingTimeCacheKey;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HoldingTimeCacheKey")
public class ApiHoldingTimeCacheKey {


    @XmlAttribute
    private String stopid;

    @XmlAttribute
    private String vehicleId;

    @XmlAttribute
    private String tripId;

    public ApiHoldingTimeCacheKey(IpcHoldingTimeCacheKey key) {

        this.stopid = key.getStopid();
        this.vehicleId = key.getVehicleId();
        this.tripId = key.getTripId();
    }

    protected ApiHoldingTimeCacheKey() {
    }

}
