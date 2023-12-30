/* (C)2023 */
package org.transitclock.api.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for web based API.
 *
 * @author SkiBu Smith
 */
public class WebUtils {

    private static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

    /**
     * Provides the API key to be used to access the Transitime API by Transitime web pages.
     *
     * @return API key
     */
    public static String apiKey() {
        return "5ec0de94";
    }

    /**
     * Convenience method for when need to throw a BAD_REQUEST exception response.
     *
     * @param ex the exception which will be logged Message to be provided as part of the response.
     * @return Exception to be thrown
     */
    public static WebApplicationException badRequestException(Throwable ex) {
        logger.error("Bad Request", ex);
        return badRequestException(ex, Status.BAD_REQUEST.getStatusCode(), ex.getMessage());
    }

    /**
     * Convenience method for when need to throw a BAD_REQUEST exception response.
     *
     * @param s Message to be provided as part of the response.
     * @return Exception to be thrown
     */
    public static WebApplicationException badRequestException(String s) {
        return badRequestException(Status.BAD_REQUEST.getStatusCode(), s);
    }

    public static WebApplicationException badRequestException(Throwable cause, int response, String s) {
        return new WebApplicationException(cause, Response.status(response)
                .entity(s)
                .type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Origin", "*")
                .build());
    }

    /**
     * Convenience method for when need to throw a special HTTP response exception, such as 429
     * which means Too Many Requests. See http://en.wikipedia.org/wiki/List_of_HTTP_status_codes for
     * details of possible response codes.
     *
     * @param response
     * @param s
     * @return
     */
    public static WebApplicationException badRequestException(int response, String s) {
        return new WebApplicationException(Response.status(response)
                .entity(s)
                .type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Origin", "*")
                .build());
    }
}
