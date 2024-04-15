/* (C)2023 */
package org.transitclock.core.avl;

import com.google.protobuf.CodedInputStream;
import com.google.transit.realtime.GtfsRealtime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.transitclock.ApplicationProperties;
import org.transitclock.domain.structs.AssignmentType;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.Location;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.MathUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * For reading in feed of GTFS-realtime AVL data. Is used for both realtime feeds and for when
 * reading in a giant batch of data.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "transitclock.avl.gtfs-realtime-feed-uri")
public class GtfsRealtimeModule extends PollUrlAvlModule {
    public GtfsRealtimeModule(ApplicationProperties properties, AvlReportProcessor avlReportProcessor) {
        super(properties, avlReportProcessor);
    }

    @Override
    public List<String> getSources() {
        return avlProperties.getGtfsRealtimeFeedURI();
    }

    @Override
    protected Collection<AvlReport> processData(InputStream inputStream) throws Exception {
        CodedInputStream codedStream = CodedInputStream.newInstance(inputStream);
        codedStream.setSizeLimit(200000000);

        var feed = GtfsRealtime.FeedMessage.parseFrom(codedStream);

        return processMessage(feed);
    }

    /**
     * Returns the vehicleID. Returns null if no VehicleDescription associated with the vehicle or
     * if no ID associated with the VehicleDescription.
     *
     * @param vehicle
     * @return vehicle ID or null if there isn't one
     */
    private static String getVehicleId(GtfsRealtime.VehiclePosition vehicle) {
        if (!vehicle.hasVehicle()) {
            return null;
        }
        GtfsRealtime.VehicleDescriptor desc = vehicle.getVehicle();
        if (!desc.hasId()) {
            return null;
        }
        return desc.getId();
    }

    /**
     * Returns the vehicleID. Returns null if no VehicleDescription associated with the vehicle or
     * if no ID associated with the VehicleDescription.
     *
     * <p>If not vehicleId try label (VIA San Antonio Feed).
     *
     * @param vehicle
     * @return vehicle ID or null if there isn't one
     */
    private static String getLicensePlate(GtfsRealtime.VehiclePosition vehicle) {
        if (!vehicle.hasVehicle()) {
            return null;
        }
        GtfsRealtime.VehicleDescriptor desc = vehicle.getVehicle();
        if (!desc.hasLicensePlate()) {

            if (desc.hasLabel()) return desc.getLabel();
            return null;
        }
        return desc.getLicensePlate();
    }


    /**
     * For each vehicle in the GTFS-realtime message put AvlReport into list.
     *
     * @param message Contains all of the VehiclePosition objects
     * @return List of AvlReports
     */
    private List<AvlReport> processMessage(GtfsRealtime.FeedMessage message) {
        List<AvlReport> avlReports = new LinkedList<>();

        IntervalTimer timer = new IntervalTimer();

        // For each entity/vehicle process the data
        int counter = 0;
        for (GtfsRealtime.FeedEntity entity : message.getEntityList()) {
            // If no vehicles in the entity then nothing to process
            if (!entity.hasVehicle()) {
                continue;
            }

            // Get the object describing the vehicle
            GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();

            // Determine vehicle ID. If no vehicle ID then can't handle it.
            String vehicleId = getVehicleId(vehicle);
            String vehicleLabel = getVehicleLabel(vehicle);

            if (vehicleId == null && vehicleLabel != null)
                vehicleId = vehicleLabel;

            if (vehicleId == null)
                continue;

            // Determine the GPS time. If time is not available then use the
            // current time. This is really a bad idea though because the
            // latency will be quite large, resulting in inaccurate predictions
            // and arrival times. But better than not having a time at all.
            long gpsTime;

            if (vehicle.hasTimestamp()) {
                gpsTime = vehicle.getTimestamp();
                if (gpsTime < 14396727760L) {
                    // TODO if too small to be milli second epoch
                    gpsTime = gpsTime * 1000;
                }
            } else {
                gpsTime = System.currentTimeMillis();
            }

            // Determine the position data
            GtfsRealtime.Position position = vehicle.getPosition();

            // If no position then cannot handle the data
            if (!position.hasLatitude() || !position.hasLongitude()) continue;

            double lat = position.getLatitude();
            double lon = position.getLongitude();

            // Handle speed and heading
            float speed = Float.NaN;
            if (position.hasSpeed()) {
                speed = position.getSpeed();
            }

            float heading = Float.NaN;
            if (position.hasBearing()) {
                heading = position.getBearing();
            }

            // Create the core AVL object.
            // The feed can provide a silly amount of precision so round to just 5 decimal places.
            // AvlReport is expecting time in ms while the proto provides it in seconds
            AvlReport.AvlReportBuilder builder = AvlReport.builder()
                .withVehicleId(vehicleId)
                .withTime(new Date(gpsTime))
                .withLocation(new Location(MathUtils.round(lat, 5), MathUtils.round(lon, 5)))
                .withHeading(heading)
                .withSpeed(speed)
                .withSource("GTFS-rt")
                .withLicensePlate(getLicensePlate(vehicle))
                .withPassengerFullness(Float.NaN);

            // Determine vehicle assignment information
            if (vehicle.hasTrip()) {
                GtfsRealtime.TripDescriptor tripDescriptor = vehicle.getTrip();

                if (tripDescriptor.hasRouteId()) {
                    builder.withAssignmentId(tripDescriptor.getRouteId());
                    builder.withAssignmentType(AssignmentType.ROUTE_ID);
                }

                if (tripDescriptor.hasTripId()) {
                    builder.withAssignmentId(tripDescriptor.getTripId());
                    builder.withAssignmentType(AssignmentType.TRIP_ID);
                }
            }

            // The callback for each AvlReport
            avlReports.add(builder.build());
            ++counter;
        }

        logger.info(
            "Successfully processed {} AVL reports from GTFS-realtime feed in {} msec",
            counter,
            timer.elapsedMsec());

        return avlReports;
    }

    private String getVehicleLabel(GtfsRealtime.VehiclePosition vehicle) {
        return vehicle.getVehicle().getLabel();
    }
}
