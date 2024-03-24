package org.transitclock.api.resources;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.transitclock.api.reports.ChartGenericJsonQuery;
import org.transitclock.api.reports.PredAccuracyIntervalQuery;
import org.transitclock.api.reports.PredAccuracyRangeQuery;
import org.transitclock.api.reports.PredictionAccuracyQuery.IntervalsType;
import org.transitclock.api.reports.ScheduleAdherenceController;
import org.transitclock.core.reports.SqlUtils;
import org.transitclock.utils.Time;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public class ReportsResource extends BaseApiResource {
    @GetMapping(value = "/reports/predAccuracyIntervalsData.jsp")
    public ResponseEntity<String> predAccuracyIntervalsData(HttpServletRequest request) throws SQLException, ParseException {
        // Get params from the query string
        String agencyId = request.getParameter("a");
        String beginDate = request.getParameter("beginDate");
        String numDays = request.getParameter("numDays");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");

        String[] routeIds = request.getParameterValues("r");
        // source can be "" (for all), "Transitime", or "Other";
        String source = request.getParameter("source");

        String predictionType = request.getParameter("predictionType");

        IntervalsType intervalsType = IntervalsType
                .createIntervalsType(request.getParameter("intervalsType"));

        double intervalPercentage1 = 0.68; // Default value
        String intervalPercentage1Str = request.getParameter("intervalPercentage1");
        try {
            if (intervalPercentage1Str != null && !intervalPercentage1Str.isEmpty())
                intervalPercentage1 = Double.parseDouble(intervalPercentage1Str);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .body("Could not parse Interval Percentage 1 of " + intervalPercentage1Str);
        }

        double intervalPercentage2 = Double.NaN; // Default value
        String intervalPercentage2Str = request.getParameter("intervalPercentage2");
        try {
            if (intervalPercentage2Str != null && !intervalPercentage2Str.isEmpty())
                intervalPercentage2 = Double.parseDouble(intervalPercentage2Str);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .body("Could not parse Interval Percentage 2 of " + intervalPercentage2Str);
        }

        if (agencyId == null || beginDate == null || numDays == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("For predAccuracyIntervalsData.jsp must "
                                  + "specify parameters 'a' (agencyId), 'beginDate', "
                                  + "and 'numDays'.");
        }

        // Perform the query and convert results of query to a JSON string
        PredAccuracyIntervalQuery query = new PredAccuracyIntervalQuery(agencyId);
        String jsonString = query
                .getJson(beginDate, numDays, beginTime, endTime,
                         routeIds, source, predictionType,
                         intervalsType, intervalPercentage1,
                         intervalPercentage2);

        // If no data then return error status with an error message
        if (jsonString == null || jsonString.isEmpty()) {
            String message = "No data for beginDate=" + beginDate
                    + " numDays=" + numDays
                    + " beginTime=" + beginTime
                    + " endTime=" + endTime
                    + " routeIds=" + Arrays.asList(routeIds)
                    + " source=" + source
                    + " predictionType=" + predictionType
                    + " intervalsType=" + intervalsType;
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .body(message);
        }

        // Respond with the JSON string
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.OK)
                .body(jsonString);
        return response;
    }

    @GetMapping(value = "/reports/predAccuracyRangeData.jsp")
    public ResponseEntity<String> predAccuracyRangeData(HttpServletRequest request) throws SQLException, ParseException {
        // Get params from the query string
        String agencyId = request.getParameter("a");

        String beginDate = request.getParameter("beginDate");
        String numDays = request.getParameter("numDays");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");

        String[] routeIds = request.getParameterValues("r");
        // source can be "" (for all), "Transitime", or "Other";
        String source = request.getParameter("source");

        String predictionType = request.getParameter("predictionType");

        int allowableEarlySec = (int) 1.5 * Time.SEC_PER_MIN; // Default value
        String allowableEarlyStr = request.getParameter("allowableEarly");
        try {
            if (allowableEarlyStr != null && !allowableEarlyStr.isEmpty()) {
                allowableEarlySec = (int) Double.parseDouble(allowableEarlyStr) * Time.SEC_PER_MIN;
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .body("Could not parse Allowable Early value of " + allowableEarlyStr);
        }

        int allowableLateSec = (int) 4.0 * Time.SEC_PER_MIN; // Default value
        String allowableLateStr = request.getParameter("allowableLate");
        try {
            if (allowableLateStr != null && !allowableLateStr.isEmpty()) {
                allowableLateSec = (int) Double.parseDouble(allowableLateStr) * Time.SEC_PER_MIN;
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .body("Could not parse Allowable Late value of " + allowableLateStr);
        }

        if (agencyId == null || beginDate == null || numDays == null) {
            return ResponseEntity.badRequest()
                    .body("For predAccuracyRangeData.jsp must specify parameters 'a' (agencyId), 'beginDate', and 'numDays'.");
        }

        // Make sure not trying to get data for too long of a time span since
        // that could bog down the database.
        if (Integer.parseInt(numDays) > 31) {
            throw new ParseException("Number of days of " + numDays + " spans more than a month", 0);
        }

        // Perform the query.
        PredAccuracyRangeQuery query = new PredAccuracyRangeQuery(agencyId);

        // Convert results of query to a JSON string
        String jsonString = query.getJson(beginDate, numDays, beginTime, endTime,
                                          routeIds, source, predictionType,
                                          allowableEarlySec, allowableLateSec);

        // If no data then return error status with an error message
        if (jsonString == null || jsonString.isEmpty()) {
            String message = "No data for beginDate=" + beginDate
                    + " numDays=" + numDays
                    + " beginTime=" + beginTime
                    + " endTime=" + endTime
                    + " routeIds=" + Arrays.asList(routeIds)
                    + " source=" + source
                    + " allowableEarlyMsec=" + allowableEarlySec
                    + " allowableLateMsec=" + allowableLateSec;
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .body(message);
        }

        return ResponseEntity.ok(jsonString);
    }

    @GetMapping(value = "/reports/data/summaryScheduleAdherence.jsp")
    public ResponseEntity<List<Integer>> summaryScheduleAdherence(HttpServletRequest request) throws ParseException {
        String startDateStr = request.getParameter("beginDate");
        String numDaysStr = request.getParameter("numDays");
        String startTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        String earlyLimitStr = request.getParameter("allowableEarly");
        String lateLimitStr = request.getParameter("allowableLate");
        double earlyLimit = -60.0;
        double lateLimit = 60.0;

        if (StringUtils.hasText(startTime)) {
            startTime = "00:00:00";
        } else {
            startTime += ":00";
        }

        if (StringUtils.hasText(endTime)) {
            endTime = "23:59:59";
        } else {
            endTime += ":00";
        }

        if (!StringUtils.hasText(earlyLimitStr)) {
            earlyLimit = Double.parseDouble(earlyLimitStr) * 60;
        }
        if (!StringUtils.hasText(lateLimitStr)) {
            lateLimit = Double.parseDouble(lateLimitStr) * 60;
        }


        String routeIdList = request.getParameter("r");
        List<String> routeIds = routeIdList == null ? null : Arrays.asList(routeIdList.split(","));


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateFormat.parse(startDateStr);

        List<Integer> results = ScheduleAdherenceController.routeScheduleAdherenceSummary(startDate,
                                                                                          Integer.parseInt(numDaysStr),
                                                                                          startTime, endTime,
                                                                                          earlyLimit, lateLimit,
                                                                                          routeIds);

        return ResponseEntity.ok(results);
    }


    @GetMapping(value = "/reports/predAccuracyScatterData.jsp")
    public ResponseEntity<String> predAccuracyScatterData(HttpServletRequest request) throws ParseException, SQLException {
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
            return ResponseEntity.badRequest()
                    .body("For predAccuracyScatterData.jsp must specify parameters 'a' (agencyId), 'beginDate', and 'numDays'.");
        }

        // Make sure not trying to get data for too long of a time span since
        // that could bog down the database.
        if (Integer.parseInt(numDays) > 31) {
            throw new ParseException("Number of days of " + numDays + " spans more than a month", 0);
        }

        // Determine the time portion of the SQL
        String timeSql = "";
        if ((beginTime != null && !beginTime.isEmpty()) || (endTime != null && !endTime.isEmpty())) {
            // If only begin or only end time set then use default value
            if (beginTime == null || beginTime.isEmpty()) {
                beginTime = "00:00:00";
            }
            if (endTime == null || endTime.isEmpty()) {
                endTime = "23:59:59";
            }

            timeSql = SqlUtils.timeRangeClause(request, "arrival_depature_time", Integer.parseInt(numDays));
        }

        // Determine route portion of SQL. Default is to provide info for all routes.
        String routeSql = "";
        if (routeId != null && !routeId.trim().isEmpty()) {
            routeSql = "  AND route_id='" + routeId + "' ";
        }

        // Determine the source portion of the SQL. Default is to provide predictions for all sources
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

        // Determine SQL for prediction type
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
            return ResponseEntity.badRequest().body(message);
        }

        // Return the JSON data
        return ResponseEntity.ok(jsonString);
    }

}
