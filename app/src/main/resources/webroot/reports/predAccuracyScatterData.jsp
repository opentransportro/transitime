<%@ page contentType="application/json; charset=UTF-8" %>
<%@ page import="org.transitclock.api.reports.ChartGenericJsonQuery" %>
<%@ page import="org.transitclock.core.reports.SqlUtils" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.util.Objects" %>
<%
    // Parameters from request
    String agencyId = request.getParameter("a");
    String beginDate = request.getParameter("beginDate");
    String numDays = request.getParameter("numDays");
    String beginTime = request.getParameter("beginTime");
    String endTime = request.getParameter("endTime");
    String routeId = request.getParameter("r");
    String source = request.getParameter("source");
    String predictionType = request.getParameter("predictionType");

    boolean showTooltips = true;
    String showTooltipsStr = request.getParameter("tooltips");
    if (showTooltipsStr != null && showTooltipsStr.equalsIgnoreCase("false"))
        showTooltips = false;

    if (agencyId == null || beginDate == null || numDays == null) {
        response.getWriter().write("For predAccuracyScatterData.jsp must "
                + "specify parameters 'a' (agencyId), "
                + "'beginDate', and 'numDays'.");
        return;
    }

// Make sure not trying to get data for too long of a time span since
// that could bog down the database.
    if (Integer.parseInt(numDays) > 31) {
        throw new ParseException("Number of days of " + numDays + " spans more than a month", 0);
    }

// Determine the time portion of the SQL
    String timeSql = "";
    if ((beginTime != null && !beginTime.isEmpty())
            || (endTime != null && !endTime.isEmpty())) {
        // If only begin or only end time set then use default value
        if (beginTime == null || beginTime.isEmpty())
            beginTime = "00:00:00";
        if (endTime == null || endTime.isEmpty())
            endTime = "23:59:59";

        timeSql = SqlUtils.timeRangeClause(request, "arrival_depature_time", Integer.parseInt(numDays));
    }

// Determine route portion of SQL. Default is to provide info for
// all routes.
    String routeSql = "";
    if (routeId != null && !routeId.trim().isEmpty()) {
        routeSql = "  AND route_id='" + routeId + "' ";
    }

// Determine the source portion of the SQL. Default is to provide
// predictions for all sources
    String sourceSql = "";
    if (source != null && !source.isEmpty()) {
        if (source.equals("Transitime")) {
            // Only "Transitime" predictions
            sourceSql = " AND prediction_source='Transitime'";
        } else {
            // Anything but "Transitime"
            sourceSql = " AND prediction_source<>'Transitime'";
        }
    }

// Determine SQL for prediction type ()
    String predTypeSql = "";
    if (predictionType != null && !predictionType.isEmpty()) {
        if (Objects.equals(source, "AffectedByWaitStop")) {
            // Only "AffectedByLayover" predictions
            predTypeSql = " AND affected_by_wait_stop = true ";
        } else {
            // Only "NotAffectedByLayover" predictions
            predTypeSql = " AND affected_by_wait_stop = false ";
        }
    }

    String tooltipsSql = "";
    if (showTooltips)
        tooltipsSql =
                ", format(E'predAccuracy= %s\\n"
                        + "prediction=%s\\n"
                        + "stopId=%s\\n"
                        + "routeId=%s\\n"
                        + "tripId=%s\\n"
                        + "arrDepTime=%s\\n"
                        + "predTime=%s\\n"
                        + "predReadTime=%s\\n"
                        + "vehicleId=%s\\n"
                        + "source=%s\\n"
                        + "affectedByLayover=%s', "
                        + "   CAST(prediction_accuracy_msecs || ' msec' AS INTERVAL), predicted_time-prediction_read_time,"
                        + "   stop_id, route_id, trip_id, "
                        + "   to_char(arrival_departure_time, 'HH24:MI:SS.MS MM/DD/YYYY'),"
                        + "   to_char(predicted_time, 'HH24:MI:SS.MS'),"
                        + "   to_char(prediction_read_time, 'HH24:MI:SS.MS'),"
                        + "   vehicle_id,"
                        + "   prediction_Source,"
                        + "   CASE WHEN affected_by_wait_stop THEN 'True' ELSE 'False' END) AS tooltip ";

    String predLengthSql = "     to_char(predicted_time-prediction_read_time, 'SSSS')::integer ";
    String predAccuracySql = "     prediction_accuracy_msecs/1000 as predAccuracy ";

    String sql = "SELECT "
            + predLengthSql + " as predLength,"
            + predAccuracySql
            + tooltipsSql
            + " FROM prediction_accuracy "
            + "WHERE "
            + "1=1 "
            + SqlUtils.timeRangeClause(request, "arrival_departure_time", 30)
            + "  AND " + predLengthSql + " < 900 "
            + routeSql
            + sourceSql
            + predTypeSql
            // Filter out MBTA_seconds source since it is isn't significantly different from MBTA_epoch.
            // TODO should clean this up by not having MBTA_seconds source at all
            // in the prediction accuracy module for MBTA.
            + "  AND prediction_source <> 'MBTA_seconds' ";


    // Determine the json data by running the query
    String jsonString = ChartGenericJsonQuery.getJsonString(agencyId, sql);


// If no data then return error status with an error message
    if (jsonString == null || jsonString.isEmpty()) {
        String message = "No data for beginDate=" + beginDate
                + " numDays=" + numDays
                + " beginTime=" + beginTime
                + " endTime=" + endTime
                + " routeId=" + routeId
                + " source=" + source
                + " predictionType=" + predictionType;
        response.setStatus(400);
        response.getWriter().write(message);
        return;
    }

// Return the JSON data
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(jsonString);
%>
