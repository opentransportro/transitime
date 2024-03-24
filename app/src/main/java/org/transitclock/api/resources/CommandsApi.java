package org.transitclock.api.resources;

import java.io.InputStream;
import java.util.List;

import org.transitclock.api.data.ApiCommandAck;
import org.transitclock.api.resources.request.DateTimeParam;
import org.transitclock.api.utils.StandardParameters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public interface CommandsApi {
    /**
     * Reads in a single AVL report specified by the query string parameters v=vehicleId
     * &t=epochTimeInMsec&lat=latitude&lon=longitude&s=speed(optional) &h=heading(option) . Can also
     * optionally specify assignmentType="1234" and assignmentType="BLOCK_ID" or ROUTE_ID, TRIP_ID,
     * or TRIP_SHORT_NAME.
     *
     * @param stdParameters
     * @param vehicleId
     * @param time
     * @param lat
     * @param lon
     * @param speed             (optional)
     * @param heading           (optional)
     * @param assignmentId      (optional)
     * @param assignmentTypeStr (optional)
     *
     * @return ApiCommandAck response indicating whether successful
     *
     * @
     */
    @GetMapping(
            value = "/command/pushAvl",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Reads in a single AVL report specified by the query string parameters",
            description = "Reads in a single AVL report specified by the query string parameters.",
            tags = {"operation", "vehicle", "avl"})
    ResponseEntity<ApiCommandAck> pushAvlData(
            StandardParameters stdParameters,
            @Parameter(description = "VehicleId. Unique identifier of the vehicle.", required = true)
            @RequestParam(value = "v")
            String vehicleId,
            @Parameter(description = "GPS epoch time in msec.", required = true) @RequestParam(value = "t") long time,
            @Parameter(description = "Latitude of AVL reporte. Decimal degrees.", required = true)
            @RequestParam(value = "lat")
            double lat,
            @Parameter(description = "Longitude of AVL reporte. Decimal degrees.", required = true)
            @RequestParam(value = "lon")
            double lon,
            @Parameter(description = "Speed of AVL reporte. m/s.", required = false)
            @RequestParam(value = "s", defaultValue = "NaN")
            float speed,
            @Parameter(
                    description = "Heading of AVL report. Degrees. 0 degrees=North. Should be set"
                            + " to Float.NaN if speed not available",
                    required = false)
            @RequestParam(value = "h", defaultValue = "NaN")
            float heading,
            @Parameter(
                    description = "Indicates the assignmet id of the AVL report according to the"
                            + " assingment tyoe. For example, if assingment type is"
                            + " ROUTE_ID, the assingment ID should be one route_id"
                            + " loaded in the system.",
                    required = false)
            @RequestParam(value = "assignmentId")
            String assignmentId,
            @Parameter(
                    description = "Indicates the assignmet type of the AV report. This parameter"
                            + " can take the next values:"
                            + " <ul><li>ROUTE_ID</li><li>TRIP_ID</li>TRIP_SHORT_NAME</li>"
                            + " </ul>")
            @RequestParam(value = "assignmentType")
            String assignmentTypeStr);

    /**
     * Processes a POST http request contain AVL data in the message body in JSON format. The data
     * format is:
     *
     * <p>{avl: [{v: "vehicleId1", t: epochTimeMsec, lat: latitude, lon: longitude,
     * s:speed(optional), h:heading(optional)}, {v: "vehicleId2", t: epochTimeMsec, lat: latitude,
     * lon: longitude, s: speed(optional), h: heading(optional)}, {etc...} ] }
     *
     * <p>Note: can also specify assignment info using "assignmentId: 4321, assignmentType: TRIP_ID"
     * where assignmentType can be BLOCK_ID, ROUTE_ID, TRIP_ID, or TRIP_SHORT_NAME.
     *
     * @param stdParameters
     * @param requestBody
     *
     * @return ApiCommandAck response indicating whether successful
     *
     * @
     */
    @PostMapping(
            value = "/command/pushAvl",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Reads in a single AVL report in the message body.",
            description = "Reads in a single AVL report specified by the query string parameters."
                    + " <p>{avl: [{v: \"vehicleId1\", t: epochTimeMsec, lat: latitude, lon:"
                    + " longitude, s:speed(optional), h:heading(optional)},\r\n"
                    + "  {v: \"vehicleId2\", t: epochTimeMsec, lat: latitude, lon: longitude, "
                    + " s: speed(optional), h: heading(optional)},  {etc...}]</p>. Can also"
                    + " specify assignment info using  \"assignmentId: 4321, assignmentType:"
                    + " TRIP_ID\"  where assignmentType can be BLOCK_ID, ROUTE_ID, TRIP_ID, or "
                    + " TRIP_SHORT_NAME.",
            tags = {"operation", "vehicle", "avl"})
    ResponseEntity<ApiCommandAck> pushAvlData(
            StandardParameters stdParameters,
            @Parameter(description = "Json of avl report.", required = true) InputStream requestBody);

    @GetMapping(
            value = "/command/resetVehicle",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Reset a vehicle",
            description = "This is to give the means of manually setting a vehicle unpredictable and"
                    + " unassigned so it will be reassigned quickly.",
            tags = {"command", "vehicle"})
    ResponseEntity<ApiCommandAck> getVehicles(
            StandardParameters stdParameters,
            @Parameter(description = "List of vechilesId.") @RequestParam(value = "v") List<String> vehicleIds);

    /**
     * Reads in information from request and stores arrival information into db.
     *
     * @param stdParameters
     * @param routeId
     * @param stopId
     *
     * @return
     *
     * @
     */
    @GetMapping(
            value = "/command/pushMeasuredArrivalTime",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Reads in information from request and stores arrival information into db",
            description = "For storing a measured arrival time so that can see if measured arrival time"
                    + " via GPS is accurate.",
            tags = {"command"})
    ResponseEntity<ApiCommandAck> pushAvlData(
            StandardParameters stdParameters,
            @Parameter(description = "Route id", required = true) @RequestParam(value = "r") String routeId,
            @Parameter(description = "Route short name.", required = true) @RequestParam(value = "rShortName")
            String routeShortName,
            @Parameter(description = "Route stop id.", required = true) @RequestParam(value = "s") String stopId,
            @Parameter(description = "Direcction id.", required = true) @RequestParam(value = "d") String directionId,
            @Parameter(description = "headsign.", required = true) @RequestParam(value = "headsign") String headsign);

    // WORK IN PROGRESS
    @PostMapping(
            value = "/command/cancelTrip/{tripId}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Cancel a trip in order to be shown in GTFS realtime.",
            description = "<font color=\"#FF0000\">Experimental. It will work olny with the correct"
                    + " version.</font> It cancel a trip that has no vechilce assigned.",
            tags = {"command", "trip"})
    ResponseEntity<ApiCommandAck> cancelTrip(
            StandardParameters stdParameters,
            @Parameter(description = "tripId to be marked as canceled.", required = true) @PathVariable("tripId")
            String tripId,
            @Parameter(description = "start trip time", required = false) @RequestParam(value = "at") DateTimeParam at);

    @PostMapping(
            value = "/command/reenableTrip/{tripId}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Cancel a trip in order to be shown in GTFS realtime.",
            description = "<font color=\"#FF0000\">Experimental. It will work olny with the correct"
                    + " version.</font> It cancel a trip that has no vechilce assigned.",
            tags = {"command", "trip"})
    ResponseEntity<ApiCommandAck> reenableTrip(
            StandardParameters stdParameters,
            @Parameter(description = "tripId to remove calceled satate.", required = true) @PathVariable("tripId")
            String tripId,
            @Parameter(description = "start trip time", required = false) @RequestParam(value = "at") DateTimeParam at);

    @Operation(
            summary = "Add vehicles to block",
            description = "Add vehicles to block",
            tags = {"vehicle", "block"})
    @PostMapping(
            value = "/command/vehicleToBlock",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    ResponseEntity<ApiCommandAck> addVehicleToBlock(
            StandardParameters stdParameters,
            @Parameter(description = "Json of vehicle to block.", required = true) InputStream requestBody);

    @Operation(
            summary = "Add vehicles to block",
            description = "Add vehicles to block",
            tags = {"vehicle", "block"})
    @GetMapping(value = "/command/removeVehicleToBlock/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<ApiCommandAck> removeVehicleToBlock(
            StandardParameters stdParameters,
            @Parameter(description = "vehicle to block id to remove.", required = true) @PathVariable("id") long id);

    @Operation(
            summary = "Add AVL export",
            description = "Add AVL export",
            tags = {"report", "avl"})
    @PostMapping(value = "/command/addAVLExport",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<ApiCommandAck> addAVLReport(
            StandardParameters stdParameters,
            @Parameter(description = "AVL date(MM-DD-YYYY).") @RequestParam(value = "avlDate") String avlDate);
}
