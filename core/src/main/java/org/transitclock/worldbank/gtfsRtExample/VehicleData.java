/* (C)2023 */
package org.transitclock.worldbank.gtfsRtExample;

import java.util.Date;

/**
 * For keeping data from the database.
 *
 * @author SkiBu Smith
 */
public class VehicleData {
    private String vehicleId;
    private String licensePlate;
    private float latitude;
    private float longitude;
    private String speed; // km/hr
    private String heading;
    private Date gpsTime;
    private String routeId;

    /**
     * @param vehicleId
     * @param licensePlate
     * @param latitude2
     * @param longitude2
     * @param speed2
     * @param heading2
     * @param gpsTime2
     * @param routeId
     */
    public VehicleData(
            String vehicleId,
            String licensePlate,
            float latitude,
            float longitude,
            String speed,
            String heading,
            Date gpsTime,
            String routeId) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.heading = heading;
        this.gpsTime = gpsTime;
        this.routeId = routeId;
    }

    /** For creating error messages */
    @Override
    public String toString() {
        return "ApiVehicle ["
                + "vehicleId="
                + vehicleId
                + ", licensePlate="
                + licensePlate
                + ", latitude="
                + latitude
                + ", longitude="
                + longitude
                + ", speed="
                + speed
                + ", heading="
                + heading
                + ", gpsTime="
                + gpsTime
                + ", routeId="
                + routeId
                + "]";
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    /**
     * @return speed in kilometers/hour
     */
    public String getSpeed() {
        return speed;
    }

    public String getHeading() {
        return heading;
    }

    public Date getGpsTime() {
        return gpsTime;
    }

    public String getRouteId() {
        return routeId;
    }
}
