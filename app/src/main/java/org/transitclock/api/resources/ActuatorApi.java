package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.transitclock.api.data.ApiCurrentServerDate;
import org.transitclock.api.data.ApiServerStatus;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.service.contract.ServerStatusInterface;
import org.transitclock.service.dto.IpcServerStatus;

import java.rmi.RemoteException;
import java.util.Date;

@Path("actuator")
@Tag(name = "actuator", description = "The actuator API")
public class ActuatorApi {
    /**
     * Returns status about the specified agency server. Currently provides info on the DbLogger
     * queue.
     *
     * @param stdParameters
     * @return
     * @throws WebApplicationException
     */
    @Path("status")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Retrieves server status information.",
            description = "Retrieves server status information.",
            tags = {"actuator"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = ApiServerStatus.class))
            })
    })
    public Response getServerStatus(@BeanParam StandardParameters stdParameters) throws WebApplicationException {

        // Make sure request is valid
        stdParameters.validate();

        try {
            // Get status information from server
            ServerStatusInterface inter = stdParameters.getServerStatusInterface();
            IpcServerStatus ipcServerStatus = inter.get();

            // Create and return ApiServerStatus response
            ApiServerStatus apiServerStatus = new ApiServerStatus(stdParameters.getAgencyId(), ipcServerStatus);
            return stdParameters.createResponse(apiServerStatus);
        } catch (Exception e) {
            // If problem getting data then return a Bad Request
            throw WebUtils.badRequestException(e);
        }
    }


    @Path("server-time")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Retrieves server time.",
            description = "Retrieves server time",
            tags = {"actuator"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = ApiCurrentServerDate.class))
            })
    })
    public Response getCurrentServerTime(@BeanParam StandardParameters stdParameters)
            throws WebApplicationException, RemoteException {
        // Make sure request is valid
        stdParameters.validate();
        ServerStatusInterface inter = stdParameters.getServerStatusInterface();
        Date currentTime = inter.getCurrentServerTime();

        return stdParameters.createResponse(new ApiCurrentServerDate(currentTime));
    }
}
