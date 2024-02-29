/* (C)2023 */
package org.transitclock.api.utils;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.PatternProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.service.*;
import org.transitclock.service.contract.*;

/**
 * For getting the standard parameters from the URI used to access the feed. Includes the key,
 * agency, and the media type (JSON or XML). Does not include command specific parameters.
 *
 * @author SkiBu Smith
 */
public class StandardParameters {
//    @PathParam("key")
    @HeaderParam("X-ApiKey")
    @Parameter(name = "X-ApiKey", description = "Application key to access this api.", in = ParameterIn.HEADER, required = true, allowEmptyValue = false)
    private String key;

//    @PathParam("agency")
    @HeaderParam("X-AgencyId")
    @Parameter(name = "X-AgencyId", description = "Specify the agency the request is intended to.", in = ParameterIn.HEADER, required = true, allowEmptyValue = true)
    private String agencyId;

    @QueryParam("format")
    private String formatOverride;

    // Note: Specifying a default value so that don't get a
    // 400 bad request when using wget and headers not set. But
    // this isn't enough. Still getting Bad Request. But leaving
    // this in as documentation that it was tried.
    @HeaderParam("accept")
    @DefaultValue("application/json")
    @Parameter(name = "Accept", in = ParameterIn.HEADER, required = false)
    String acceptHeader;

    @Context
    HttpServletRequest request;


    /**
     * Returns the media type to use for the response based on optional accept header and the
     * optional format specification in the query string of the URL. Setting format in query string
     * overrides what is set in accept header. This way it is always simple to generate a http get
     * for particular format simply by setting query string.
     *
     * <p>If format specification is incorrect then BadRequest WebApplicationException is thrown.
     *
     * <p>The media type is not determined in the constructor because then an exception would cause
     * an ugly error message because it would be handled before the root-resource class get method
     * is being called.
     *
     * @return The resulting media type
     */
    @Hidden
    public String getMediaType() throws WebApplicationException {
        // Use default of APPLICATION_JSON
        String mediaType = MediaType.APPLICATION_JSON;

        // If mediaType specified (to something besides "*/*") in accept
        // header then start with it.
        if (acceptHeader != null && !acceptHeader.contains("*/*")) {
            if (acceptHeader.contains(MediaType.APPLICATION_JSON))
                mediaType = MediaType.APPLICATION_JSON;
            else if (acceptHeader.contains(MediaType.APPLICATION_XML))
                mediaType = MediaType.APPLICATION_XML;
            else
                throw WebUtils.badRequestException("Accept header \"Accept: "
                        + acceptHeader
                        + "\" is not valid. Must be \""
                        + MediaType.APPLICATION_JSON
                        + "\" or \""
                        + MediaType.APPLICATION_XML
                        + "\"");
        }

        // If mediaType format is overridden using the query string format
        // parameter then use it.
        if (formatOverride != null) {
            // Always use lower case
            formatOverride = formatOverride.toLowerCase();

            // If mediaType override set properly then use it
            mediaType = switch (formatOverride) {
                case "json" -> MediaType.APPLICATION_JSON;
                case "xml" -> MediaType.APPLICATION_XML;
                case "human" -> MediaType.TEXT_PLAIN;
                default -> throw WebUtils.badRequestException("Format \"format="
                        + formatOverride
                        + "\" from query string not valid. "
                        + "Format must be \"json\" or \"xml\"");
            };
        }

        return mediaType;
    }

    /**
     * Makes sure not access feed too much and that the key is valid. If there is a problem then
     * throws a WebApplicationException.
     *
     * @throws WebApplicationException
     */
    @Hidden
    public void validate() throws WebApplicationException {
        // Make sure the application key is valid
        ApiKeyManager manager = ApiKeyManager.getInstance();
        if (!manager.isKeyValid(getKey())) {
            throw WebUtils.badRequestException(
                    Status.UNAUTHORIZED.getStatusCode(), "Application key \"" + getKey() + "\" is not valid.");
        }
    }

    /**
     * For creating a Response of a single object of the appropriate media type.
     *
     * @param object Object to be returned in XML or JSON
     * @return The created response in the proper media type.
     */
    @Hidden
    public Response createResponse(Object object) {
        // Start building the response
        ResponseBuilder responseBuilder = Response.ok(object);

        // Since this is a truly open API intended to be used by
        // other web pages allow cross-origin requests.
        responseBuilder.header("Access-Control-Allow-Origin", "*");

        // Specify media type of XML or JSON
        responseBuilder.type(getMediaType());

        // Return the response
        return responseBuilder.build();
    }

    /**
     * Gets the VehiclesInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The VehiclesInterface
     */
    @Hidden
    public VehiclesInterface getVehiclesInterface() throws WebApplicationException {
        VehiclesInterface vehiclesInterface = VehiclesServiceImpl.instance();
        if (vehiclesInterface == null) throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return vehiclesInterface;
    }

    /**
     * Gets the CommandsInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The CommandsInterface
     */
    @Hidden
    public CommandsInterface getCommandsInterface() throws WebApplicationException {
        CommandsInterface commandsInterface = CommandsServiceImpl.instance();
        if (commandsInterface == null) throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return commandsInterface;
    }

    /**
     * Gets the PredictionsInterface for the agencyId specified as part of the standard parameters.
     * If not valid then throws WebApplicationException.
     *
     * @return The VehiclesInterface
     */
    @Hidden
    public PredictionsInterface getPredictionsInterface() throws WebApplicationException {
        PredictionsInterface predictionsInterface = PredictionsServiceImpl.instance();
        if (predictionsInterface == null) throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return predictionsInterface;
    }

    /**
     * Gets the ConfigInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The VehiclesInterface
     */
    @Hidden
    public ConfigInterface getConfigInterface() throws WebApplicationException {
        ConfigInterface configInterface = ConfigServiceImpl.instance();
        if (configInterface == null) {
            throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");
        }

        return configInterface;
    }

    /**
     * Gets the ServerStatusInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The VehiclesInterface
     */
    @Hidden
    public ServerStatusInterface getServerStatusInterface() throws WebApplicationException {
        ServerStatusInterface serverStatusInterface = ServerStatusServiceImpl.instance();
        if (serverStatusInterface == null)
            throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return serverStatusInterface;
    }

    /**
     * Gets the CacheQueryInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The CacheQueryInterface
     */
    @Hidden
    public CacheQueryInterface getCacheQueryInterface() throws WebApplicationException {
        CacheQueryInterface cachequeryInterface = CacheQueryServiceImpl.instance();
        if (cachequeryInterface == null) throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return cachequeryInterface;
    }

    /**
     * Gets the PredictionAnalysisInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The PredictionAnalysisInterface
     */
    @Hidden
    public PredictionAnalysisInterface getPredictionAnalysisInterface() throws WebApplicationException {
        PredictionAnalysisInterface predictionAnalysisInterface = PredictionAnalysisServiceImpl.instance();
        if (predictionAnalysisInterface == null)
            throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return predictionAnalysisInterface;
    }

    /**
     * Gets the HoldingTimeInterface for the specified agencyId. If not valid then throws
     * WebApplicationException.
     *
     * @return The PredictionAnalysisInterface
     */
    @Hidden
    public HoldingTimeInterface getHoldingTimeInterface() {
        HoldingTimeInterface holdingTimeInterface = HoldingTimeServiceImpl.instance();
        if (holdingTimeInterface == null) throw WebUtils.badRequestException("Agency ID " + agencyId + " is not valid");

        return holdingTimeInterface;
    }

    /**
     * Simple getter for the key
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Simple getter for the agency ID
     *
     * @return
     */
    public String getAgencyId() {
        return agencyId;
    }

    /**
     * Returns the HttpServletRequest.
     *
     * @return
     */
    @Hidden
    public HttpServletRequest getRequest() {
        return request;
    }
}
