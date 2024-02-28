/* (C)2023 */
package org.transitclock.api.resources;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.gtfs.GtfsRtTripFeed;
import org.transitclock.api.data.gtfs.GtfsRtVehicleFeed;
import org.transitclock.api.utils.AgencyTimezoneCache;
import org.transitclock.api.utils.StandardParameters;

/**
 * Contains API commands for the GTFS-realtime API.
 *
 * @author SkiBu Smith
 */
@RestController
@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public class GtfsRealtimeApi extends BaseApiResource {
    private final AgencyTimezoneCache agencyTimezoneCache;

    public GtfsRealtimeApi(AgencyTimezoneCache agencyTimezoneCache) {
        this.agencyTimezoneCache = agencyTimezoneCache;
    }

    /**
     * For getting GTFS-realtime Vehicle Positions data for all vehicles.
     *
     * @param stdParameters
     * @param format if set to "human" then will output GTFS-rt data in human readable format.
     *     Otherwise will output data in binary format.
     */
    @GetMapping(
        value = "/command/gtfs-rt/vehiclePositions",
        produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE}
    )
    @Operation(
            summary = "GTFS-realtime Vehicle Positions data for all vehicles.",
            description = "Gets real time vehicle position feed. It might be in human readeable format or" + " binary.",
            tags = {"GTFS", "feed"})
    public ResponseEntity<Object> getGtfsRealtimeVehiclePositionsFeed(
        StandardParameters stdParameters,
        @Parameter(description = "If specified as human, it will get the output in human readable format. Otherwise will output data in binary format")
        @RequestParam(value = "format", required = false) String format) {

        // Make sure request is valid
        validate(stdParameters);

        // Determine if output should be in human-readable format or in
        // standard binary GTFS-realtime format.
        final boolean humanFormatOutput = "human".equals(format);

        FeedMessage message = GtfsRtVehicleFeed.getPossiblyCachedMessage(
            stdParameters.getAgency(), vehiclesInterface, agencyTimezoneCache);

        return generateResponse(message, humanFormatOutput);
    }

    /**
     * For getting GTFS-realtime for all trips.
     *
     * @param stdParameters
     * @param format if set to "human" then will output GTFS-rt data in human readable format.
     *     Otherwise will output data in binary format.
     * @return
     */
    @GetMapping(
        value = "/command/gtfs-rt/tripUpdates",
        produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_PROTOBUF_VALUE}
    )
    @Operation(
            summary = "GTFS-realtime trip data.",
            description = "Gets real time trip feed. It might be in human readeable format or binary.",
            tags = {"GTFS", "feed"})
    public ResponseEntity<Object> getGtfsRealtimeTripFeed(
            StandardParameters stdParameters,
            @Parameter(description = "If specified as human, it will get the output in human readable format. Otherwise will output data in binary format")
            @RequestParam(value = "format", required = false)
            String format) {

        // Make sure request is valid
        validate(stdParameters);

        // Determine if output should be in human readable format or in
        // standard binary GTFS-realtime format.
        final boolean humanFormatOutput = "human".equals(format);

        FeedMessage message = GtfsRtTripFeed.getPossiblyCachedMessage(
            stdParameters.getAgencyId(), predictionsInterface, vehiclesInterface, agencyTimezoneCache);

        return generateResponse(message, humanFormatOutput);

    }

    private ResponseEntity<Object> generateResponse(FeedMessage message, boolean human) {
        // Output in human-readable format or in standard binary
        // format
        if (human) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok()
                .headers(headers)
                .body(message.toString());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROTOBUF);
        return ResponseEntity.ok()
            .headers(headers)
            .cacheControl(CacheControl.noCache())
            .body(message);
    }
}
