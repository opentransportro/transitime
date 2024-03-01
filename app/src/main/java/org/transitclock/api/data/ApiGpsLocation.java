/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import org.transitclock.service.dto.IpcVehicle;
import org.transitclock.utils.MathUtils;
import org.transitclock.utils.Time;

/**
 * Extends a location by including GPS information including time, speed, heading, and pathHeading.
 *
 * @author SkiBu Smith
 */
@Data
@XmlType(propOrder = {"lat", "lon", "time", "speed", "heading"})
public class ApiGpsLocation extends ApiTransientLocation {

    // Epoch time in seconds (not msec, so that shorter)
    @XmlAttribute
    private long time;

    // Speed in m/s. A Double so if null then won't show up in output
    @XmlAttribute
    private Double speed;

    // A Double so if null then won't show up in output
    @XmlAttribute
    private Double heading;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiGpsLocation() {}

    /**
     * @param lat
     * @param lon
     */
    public ApiGpsLocation(IpcVehicle vehicle) {
        super(vehicle.getLatitude(), vehicle.getLongitude());

        this.time = vehicle.getGpsTime() / Time.MS_PER_SEC;
        // Output only 1 digit past decimal point
        this.speed = Float.isNaN(vehicle.getSpeed()) ? null : MathUtils.round(vehicle.getSpeed(), 1);
        // Output only 1 digit past decimal point
        this.heading = Float.isNaN(vehicle.getHeading()) ? null : MathUtils.round(vehicle.getHeading(), 1);
    }
}
