/* (C)2023 */
package org.transitclock.core.prediction.kalman;

import org.transitclock.domain.structs.Stop;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class VehicleStopDetail {

    protected Stop stop;
    protected long time;
    protected Vehicle vehicle;

    public VehicleStopDetail(Stop stop, long time, Vehicle vehicle) {
        this.stop = stop;
        this.time = time;
        this.vehicle = vehicle;
    }

    /**
     * @return the stop
     */
    public Stop getStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(Stop stop) {
        this.stop = stop;
    }


    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * @param vehicle the vehicle to set
     */
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
