/* (C)2023 */
package org.transitclock.utils.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author SkiBu Smith
 */
public class WebUtils {

    /********************** Member Functions **************************/

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
            String paramValues[] = paramsMap.get(paramName);
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
            String paramValues[] = paramsMap.get(paramName);
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
