/* (C)2023 */
package org.transitclock.api.resources;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.gtfs.GtfsRtTripFeed;
import org.transitclock.api.data.gtfs.GtfsRtVehicleFeed;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.gtfs.realtime.OctalDecoder;
import org.transitclock.service.contract.ConfigInterface;
import org.transitclock.service.contract.PredictionsInterface;
import org.transitclock.service.contract.VehiclesInterface;

/**
 * Contains API commands for the GTFS-realtime API.
 *
 * @author SkiBu Smith
 */
@Path("/key/{key}/agency/{agency}")
@RestController
public class GtfsRealtimeApi {
    private final VehiclesInterface vehiclesInterface;
    private final PredictionsInterface predictionsInterface;
    private final ConfigInterface configInterface;

    public GtfsRealtimeApi(VehiclesInterface vehiclesInterface, PredictionsInterface predictionsInterface, ConfigInterface configInterface) {
        this.vehiclesInterface = vehiclesInterface;
        this.predictionsInterface = predictionsInterface;
        this.configInterface = configInterface;
    }

    /**
     * For getting GTFS-realtime Vehicle Positions data for all vehicles.
     *
     * @param stdParameters
     * @param format if set to "human" then will output GTFS-rt data in human readable format.
     *     Otherwise will output data in binary format.
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/gtfs-rt/vehiclePositions")
    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM})
    @Operation(
            summary = "GTFS-realtime Vehicle Positions data for all vehicles.",
            description = "Gets real time vehicle position feed. It might be in human readeable format or" + " binary.",
            tags = {"GTFS", "feed"})
    public Response getGtfsRealtimeVehiclePositionsFeed(
            final @BeanParam StandardParameters stdParameters,
            @Parameter(description = "If specified as human, it will get the output in human readable format. Otherwise will output data in binary format")
            @QueryParam(value = "format")
            String format) throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        // Determine if output should be in human-readable format or in
        // standard binary GTFS-realtime format.
        final boolean humanFormatOutput = "human".equals(format);

        // Determine the appropriate output format. For plain text best to use
        // MediaType.TEXT_PLAIN so that output is formatted properly in web
        // browser instead of newlines being removed. For binary output should
        // use MediaType.APPLICATION_OCTET_STREAM.
        String mediaType = humanFormatOutput ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        // Prepare a StreamingOutput object so can write using it
        StreamingOutput stream = outputStream -> {
            try {
                FeedMessage message = GtfsRtVehicleFeed.getPossiblyCachedMessage(
                        stdParameters.getAgencyId(), vehiclesInterface, configInterface);

                // Output in human-readable format or in standard binary
                // format
                if (humanFormatOutput) {
                    // Output data in human-readable format. First, convert
                    // the octal escaped message to regular UTF encoding.
                    String decodedMessage = OctalDecoder.convertOctalEscapedString(message.toString());
                    outputStream.write(decodedMessage.getBytes());
                } else {
                    // Standard binary output
                    message.writeTo(outputStream);
                }
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };

        // Write out the data using the output stream
        return Response.ok(stream).type(mediaType).build();
    }

    /**
     * For getting GTFS-realtime for all trips.
     *
     * @param stdParameters
     * @param format if set to "human" then will output GTFS-rt data in human readable format.
     *     Otherwise will output data in binary format.
     * @return
     * @throws WebApplicationException
     */
    @Path("/command/gtfs-rt/tripUpdates")
    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM})
    @Operation(
            summary = "GTFS-realtime trip data.",
            description = "Gets real time trip feed. It might be in human readeable format or binary.",
            tags = {"GTFS", "feed"})
    public Response getGtfsRealtimeTripFeed(
            final @BeanParam StandardParameters stdParameters,
            @Parameter(description = "If specified as human, it will get the output in human readable format. Otherwise will output data in binary format")
            @QueryParam(value = "format")
            String format) throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        // Determine if output should be in human readable format or in
        // standard binary GTFS-realtime format.
        final boolean humanFormatOutput = "human".equals(format);

        // Determine the appropriate output format. For plain text best to use
        // MediaType.TEXT_PLAIN so that output is formatted properly in web
        // browser instead of newlines being removed. For binary output should
        // use MediaType.APPLICATION_OCTET_STREAM.
        String mediaType = humanFormatOutput ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        // Prepare a StreamingOutput object so can write using it
        StreamingOutput stream = outputStream -> {
            try {
                FeedMessage message = GtfsRtTripFeed.getPossiblyCachedMessage(
                        stdParameters.getAgencyId(), vehiclesInterface, predictionsInterface, configInterface);

                // Output in human-readable format or in standard binary
                // format
                if (humanFormatOutput) {
                    // Output data in human-readable format. First, convert
                    // the octal escaped message to regular UTF encoding.
                    String decodedMessage = OctalDecoder.convertOctalEscapedString(message.toString());
                    outputStream.write(decodedMessage.getBytes());
                } else {
                    // Standard binary output
                    message.writeTo(outputStream);
                }
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };

        // Write out the data using the output stream
        return Response.ok(stream).type(mediaType).build();
    }
}
