/* (C)2023 */
package org.transitclock.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for web based API.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class WebUtils {
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
        return new WebApplicationException(
                cause,
                Response.status(response)
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

    /**
     * Goes through all the request parameters, such as from the query string, and puts them into a
     * String version of a JSON set of key values. This string can be used as the data parameter for
     * a JQuery AJAX call to forward all parameters to the page being requested via AJAX.
     *
     * @param request
     * @return The parameters to be used as data for an AJAX call
     */
    public static String getAjaxDataString(HttpServletRequest request) {
        String queryStringParams = "";
        java.util.Map<String, String[]> paramsMap = request.getParameterMap();
        boolean firstParam = true;
        for (String paramName : paramsMap.keySet()) {
            if (!firstParam) queryStringParams += ", ";
            firstParam = false;

            queryStringParams += paramName + ":[";
            String[] paramValues = paramsMap.get(paramName);
            boolean firstValue = true;
            for (String paramValue : paramValues) {
                if (!firstValue) queryStringParams += ", ";
                firstValue = false;

                queryStringParams += "\"" + paramValue + "\"";
            }
            queryStringParams += "]";
        }

        return queryStringParams;
    }

    public static String getQueryParamsString(HttpServletRequest request) {
        String queryStringParams = "";
        java.util.Map<String, String[]> paramsMap = request.getParameterMap();
        boolean firstParam = true;
        for (String paramName : paramsMap.keySet()) {
            if (!firstParam) queryStringParams += "&";
            firstParam = false;

            queryStringParams += paramName + "=";
            String[] paramValues = paramsMap.get(paramName);
            boolean firstValue = true;
            for (String paramValue : paramValues) {
                if (!firstValue) queryStringParams += ",";
                firstValue = false;

                queryStringParams += paramValue;
            }
        }

        return queryStringParams;
    }
}
