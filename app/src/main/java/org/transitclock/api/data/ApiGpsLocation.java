/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.utils.MathUtils;
import org.transitclock.utils.Time;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Extends a location by including GPS information including time, speed, heading, and pathHeading.
 *
 * @author SkiBu Smith
 */
@EqualsAndHashCode(callSuper = true)
@Getter @Setter
public class ApiGpsLocation extends ApiLocation {

    // Epoch time in seconds (not msec, so that shorter)
    @JsonProperty
    private long time;

    // Speed in m/s. A Double so if null then won't show up in output
    @JsonProperty
    private Double speed;

    // A Double so if null then won't show up in output
    @JsonProperty
    private Double heading;

    public ApiGpsLocation(IpcVehicle vehicle) {
        super(vehicle.getLatitude(), vehicle.getLongitude());

        this.time = vehicle.getGpsTime() / Time.MS_PER_SEC;
        // Output only 1 digit past decimal point
        this.speed = Float.isNaN(vehicle.getSpeed()) ? null : MathUtils.round(vehicle.getSpeed(), 1);
        // Output only 1 digit past decimal point
        this.heading = Float.isNaN(vehicle.getHeading()) ? null : MathUtils.round(vehicle.getHeading(), 1);
    }
}
