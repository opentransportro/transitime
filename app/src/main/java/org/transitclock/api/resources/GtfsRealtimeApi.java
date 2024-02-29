/* (C)2023 */
package org.transitclock.api.resources;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.transitclock.api.data.ApiCommandAck;
import org.transitclock.api.data.gtfs.GtfsRtTripFeed;
import org.transitclock.api.data.gtfs.GtfsRtVehicleFeed;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.gtfs.realtime.OctalDecoder;

/**
 * Contains API commands for the GTFS-realtime API.
 *
 * @author SkiBu Smith
 */
@Path("/gtfs-rt/")
public class GtfsRealtimeApi {
    /**
     * For getting GTFS-realtime Vehicle Positions data for all vehicles.
     *
     * @param stdParameters
     * @param format if set to "human" then will output GTFS-rt data in human readable format.
     *     Otherwise will output data in binary format.
     * @return
     * @throws WebApplicationException
     */
    @Path("/vehiclePositions")
    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM})
    @Operation(
            summary = "GTFS-realtime Vehicle Positions data for all vehicles.",
            description = "Gets real time vehicle position feed. It might be in human readeable format or" + " binary.",
            tags = {"GTFS"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = FeedMessage.class))
            })
    })
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
                        stdParameters.getAgencyId());

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
    @Path("/tripUpdates")
    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM})
    @Operation(
            summary = "GTFS-realtime trip data.",
            description = "Gets real time trip feed. It might be in human readeable format or binary.",
            tags = {"GTFS"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = FeedMessage.class))
            })
    })
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
                        stdParameters.getAgencyId());

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
