//* (C)2023 */
package org.transitclock.api.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.transitclock.api.data.ApiCommandAck;
import org.transitclock.api.resources.request.DateTimeParam;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.domain.GenericQuery;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.domain.structs.AssignmentType;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.domain.structs.ExportTable;
import org.transitclock.domain.structs.Location;
import org.transitclock.domain.structs.MeasuredArrivalTime;
import org.transitclock.service.dto.IpcAvl;
import org.transitclock.service.dto.IpcTrip;

import io.swagger.v3.oas.annotations.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CommandsResource extends BaseApiResource implements CommandsApi {

    private static final String AVL_SOURCE = "API";
    @Autowired
    private DataDbLogger dataDbLogger;

    @Override
    public ResponseEntity<ApiCommandAck> pushAvlData(
            StandardParameters stdParameters,
            String vehicleId,
            long time,
            double lat,
            double lon,
            float speed,
            float heading,
            String assignmentId,
            String assignmentTypeStr) {
        // Make sure request is valid
        validate(stdParameters);

        if (vehicleId == null || vehicleId.isEmpty())
            throw WebUtils.badRequestException("Must specify vehicle ID using " + "\"v=vehicleId\"");
        if (time == 0)
            throw WebUtils.badRequestException(
                    "Must specify GPS epoch time in " + "msec using for example \"t=14212312333\"");

        try {
            // Get RMI interface for sending the command
            // Create and send an IpcAvl report to the server
            AvlReport avlReport = AvlReport.builder()
                    .withVehicleId(vehicleId)
                    .withLocation(new Location(lat, lon))
                    .withTime(new Date(time))
                    .withHeading(heading)
                    .withSpeed(speed)
                    .withSource(AVL_SOURCE)
                    .build();

            // Deal with assignment info if it is set
            if (assignmentId != null) {
                AssignmentType assignmentType = AssignmentType.BLOCK_ID;
                if (assignmentTypeStr.equals("ROUTE_ID")) assignmentType = AssignmentType.ROUTE_ID;
                else if (assignmentTypeStr.equals("TRIP_ID")) assignmentType = AssignmentType.TRIP_ID;
                else if (assignmentTypeStr.equals("TRIP_SHORT_NAME")) assignmentType = AssignmentType.TRIP_SHORT_NAME;

                avlReport.setAssignment(assignmentId, assignmentType);
            }

            IpcAvl ipcAvl = new IpcAvl(avlReport);
            commandsService.pushAvl(ipcAvl);

            // Create the acknowledgment and return it as JSON or XML
            ApiCommandAck ack = new ApiCommandAck(true, "AVL processed");
            return stdParameters.createResponse(ack);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    /**
     * Converts the request body input stream into a JSON object
     *
     * @param requestBody
     *
     * @return the corresponding JSON object
     *
     * @throws IOException
     * @throws JSONException
     */
    private static JSONObject getJsonObject(InputStream requestBody) throws IOException, JSONException {
        // Read in the request body to a string
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            strBuilder.append(line);
        }
        reader.close();

        // Convert string to JSON object
        return new JSONObject(strBuilder.toString());
    }

    @Override
    public ResponseEntity<ApiCommandAck> pushAvlData(
            StandardParameters stdParameters,
            @Parameter(description = "Json of avl report.", required = true) InputStream requestBody) {
        // Make sure request is valid
        validate(stdParameters);

        Collection<IpcAvl> avlData = new ArrayList<IpcAvl>();
        try {
            // Process the AVL report data from the JSON object
            JSONObject jsonObj = getJsonObject(requestBody);
            JSONArray jsonArray = jsonObj.getJSONArray("avl");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject avlJsonObj = jsonArray.getJSONObject(i);
                String vehicleId = avlJsonObj.getString("v");
                long time = avlJsonObj.getLong("t");
                double lat = avlJsonObj.getDouble("lat");
                double lon = avlJsonObj.getDouble("lon");
                float speed = avlJsonObj.has("s") ? (float) avlJsonObj.getDouble("s") : Float.NaN;
                float heading = avlJsonObj.has("h") ? (float) avlJsonObj.getDouble("h") : Float.NaN;

                // Convert the AVL info into a IpcAvl object to sent to server
                AvlReport avlReport = AvlReport.builder()
                        .withVehicleId(vehicleId)
                        .withTime(new Date(time))
                        .withLocation(new Location(lat, lon))
                        .withSpeed(speed)
                        .withHeading(heading)
                        .withSource(AVL_SOURCE)
                        .build();

                // Handle assignment info if there is any
                if (avlJsonObj.has("assignmentId")) {
                    String assignmentId = avlJsonObj.getString("assignmentId");
                    AssignmentType assignmentType = AssignmentType.BLOCK_ID;
                    if (avlJsonObj.has("assignmentType")) {
                        String assignmentTypeStr = avlJsonObj.getString("assignmentType");
                        if (assignmentTypeStr.equals("ROUTE_ID")) assignmentType = AssignmentType.ROUTE_ID;
                        else if (assignmentTypeStr.equals("TRIP_ID")) assignmentType = AssignmentType.TRIP_ID;
                        else if (assignmentTypeStr.equals("TRIP_SHORT_NAME"))
                            assignmentType = AssignmentType.TRIP_SHORT_NAME;
                    }
                    avlReport.setAssignment(assignmentId, assignmentType);
                }

                // Add new IpcAvl report to array of AVL reports to be handled
                avlData.add(new IpcAvl(avlReport));
            }

            // Get RMI interface and send the AVL data to server
            commandsService.pushAvl(avlData);
        } catch (JSONException | IOException e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }

        // Create the acknowledgment and return it as JSON or XML
        ApiCommandAck ack = new ApiCommandAck(true, "AVL processed");
        return stdParameters.createResponse(ack);
    }

    @Override
    public ResponseEntity<ApiCommandAck> getVehicles(StandardParameters stdParameters, List<String> vehicleIds) {
        // Make sure request is valid
        validate(stdParameters);

        for (String vehicleId : vehicleIds) {
            commandsService.setVehicleUnpredictable(vehicleId);
        }

        ApiCommandAck ack = new ApiCommandAck(true, "Vehicle reset");

        return stdParameters.createResponse(ack);
    }

    @Override
    public ResponseEntity<ApiCommandAck> pushAvlData(
            StandardParameters stdParameters, String routeId,
            String routeShortName,
            String stopId,
            String directionId,
            String headsign) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            // Store the arrival time in the db
            String agencyId = stdParameters.getAgencyId();

            MeasuredArrivalTime time =
                    new MeasuredArrivalTime(new Date(), stopId, routeId, routeShortName, directionId, headsign);
            String sql = time.getUpdateSql();
            GenericQuery query = new GenericQuery(agencyId);
            query.doUpdate(sql);

            // Create the acknowledgment and return it as JSON or XML
            ApiCommandAck ack = new ApiCommandAck(true, "MeasuredArrivalTime processed");
            return stdParameters.createResponse(ack);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }

    // WORK IN PROGRESS
    @Override
    public ResponseEntity<ApiCommandAck> cancelTrip(
            StandardParameters stdParameters,
            String tripId,
            DateTimeParam at) {
        validate(stdParameters);
        String result;
        IpcTrip ipcTrip = configService.getTrip(tripId);
        if (ipcTrip == null) {
            throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");
        }
        result = commandsService.cancelTrip(tripId, at == null ? null : at.getDate());

        if (result == null) {
            return stdParameters.createResponse(new ApiCommandAck(true, "Processed"));
        }

        return stdParameters.createResponse(new ApiCommandAck(true, result));
    }

    @Override
    public ResponseEntity<ApiCommandAck> reenableTrip(
            StandardParameters stdParameters,
            String tripId,
            DateTimeParam at) {
        validate(stdParameters);
        String result = null;
        IpcTrip ipcTrip = configService.getTrip(tripId);
        if (ipcTrip == null) {
            throw WebUtils.badRequestException("TripId=" + tripId + " does not exist.");
        }
        result = commandsService.reenableTrip(tripId, at == null ? null : at.getDate());
        if (result == null) {
            return stdParameters.createResponse(new ApiCommandAck(true, "Processed"));
        }

        return stdParameters.createResponse(new ApiCommandAck(true, result));
    }

    @Override
    public ResponseEntity<ApiCommandAck> addVehicleToBlock(
            StandardParameters stdParameters,
            InputStream requestBody) {
        // Make sure request is valid
        validate(stdParameters);
        String result = null;

        try {
            JSONObject jsonObj = getJsonObject(requestBody);
            String vehicleId = jsonObj.getString("vehicleId");
            long validFrom = jsonObj.getLong("validFrom");
            long validTo = jsonObj.getLong("validTo");
            String blockId = jsonObj.getString("blockId");

            result = commandsService.addVehicleToBlock(
                    vehicleId, blockId, "", new Date(), new Date(validFrom * 1000), new Date(validTo * 1000));
        } catch (JSONException | IOException e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
        if (result == null) return stdParameters.createResponse(new ApiCommandAck(true, "Processed"));
        else return stdParameters.createResponse(new ApiCommandAck(true, result));
    }

    @Override
    public ResponseEntity<ApiCommandAck> removeVehicleToBlock(
            StandardParameters stdParameters,
            long id) {
        // Make sure request is valid
        validate(stdParameters);
        try {
            commandsService.removeVehicleToBlock(id);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
        return stdParameters.createResponse(new ApiCommandAck(true, "Processed"));
    }

    @Override
    public ResponseEntity<ApiCommandAck> addAVLReport(
            StandardParameters stdParameters,
            String avlDate) {
        // Make sure request is valid
        validate(stdParameters);

        try {
            ExportTable exportTable = new ExportTable(new SimpleDateFormat("MM-dd-yyyy").parse(avlDate), 1, "avl_" + avlDate + ".csv");
            dataDbLogger.add(exportTable);

        } catch (Exception ex) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(ex);
        }
        return stdParameters.createResponse(new ApiCommandAck(true, "Processed"));
    }
}
