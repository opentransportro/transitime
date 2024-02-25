/* (C)2023 */
package org.transitclock.api.utils;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

/**
 * For getting the standard parameters from the URI used to access the feed. Includes the key,
 * agency, and the media type (JSON or XML). Does not include command specific parameters.
 *
 * @author SkiBu Smith
 */
public class StandardParameters {
    @PathParam("key")
    @Parameter(description = "Application key to access this api.")
    private String key;

    @PathParam("agency")
    @Parameter(description = "Specify the agency the request is intended to.")
    private String agencyId;

    @QueryParam("format")
    private String formatOverride;

    // Note: Specifying a default value so that don't get a
    // 400 bad request when using wget and headers not set. But
    // this isn't enough. Still getting Bad Request. But leaving
    // this in as documentation that it was tried.
    @HeaderParam("accept")
    @DefaultValue("application/json")
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
     * For creating a Response of a single object of the appropriate media type.
     *
     * @param object Object to be returned in XML or JSON
     * @return The created response in the proper media type.
     */
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
    public HttpServletRequest getRequest() {
        return request;
    }
}
