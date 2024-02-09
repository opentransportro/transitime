<%-- Provides schedule adherence data in JSON format. Provides for
     the specified route the number arrivals/departures that
     are early, number late, number on time, and number total for each
     direction for each stop. 
     Request parameters are:
       a - agency ID
       r - route ID or route short name. 
       dateRange - in format "xx/xx/xx to yy/yy/yy"
       beginDate - date to begin query. For if dateRange not used.
       numDays - number of days can do query. Limited to 31 days. For if dateRange not used.
       beginTime - for optionally specifying time of day for query for each day
       endTime - for optionally specifying time of day for query for each day
       allowableEarlyMinutes - how early vehicle can be and still be OK.  Decimal format OK. 
       allowableLateMinutes - how early vehicle can be and still be OK. Decimal format OK.
--%>
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="org.transitclock.core.reports.GenericJsonQuery" %>
<%@ page import="org.transitclock.core.reports.SqlUtils" %>
<%
    try {
        String allowableEarlyStr = request.getParameter("allowableEarly");
        if (allowableEarlyStr == null || allowableEarlyStr.isEmpty()) {
            allowableEarlyStr = "1.0";
        }
        String allowableEarlyMinutesStr = "'" + SqlUtils.convertMinutesToSecs(allowableEarlyStr) + " seconds'";

        String allowableLateStr = request.getParameter("allowableLate");
        if (allowableLateStr == null || allowableLateStr.isEmpty()) {
            allowableLateStr = "4.0";
        }
        String allowableLateMinutesStr = "'" + SqlUtils.convertMinutesToSecs(allowableLateStr) + " seconds'";

        String sql =
                "WITH trips_early_query AS ( SELECT "
                        + "	 array_to_string(array_agg(distinct trip_id::text order by trip_id::text), '; ') AS trips_early, \n"
                        + "	 s.id AS stop_id, \n"
                        + "	 ad.stop_order AS stop_order \n"
                        + " 	FROM arrivals_departures ad, stops s  \n"
                        + "WHERE "
                        // To get stop name
                        + " ad.config_rev = s.config_rev \n"
                        + " AND ad.stop_id = s.id \n"
                        // Only need arrivals/departures that have a schedule time
                        + " AND ad.scheduled_time IS NOT NULL \n"
                        // Specifies which routes to provide data for
                        + SqlUtils.routeClause(request.getParameter("r"), "ad") + "\n"
                        + SqlUtils.timeRangeClause(request, "ad.time", 7) + "\n"
                        + " AND scheduled_time-time > " + allowableEarlyMinutesStr + " \n"
                        + "	 GROUP BY direction_id, s.name, s.id, ad.stop_order \n"
                        + "	 ORDER BY direction_id, ad.stop_order, s.name \n"
                        + "), \n"

                        + "trips_late_query AS ( SELECT "
                        + "	 array_to_string(array_agg(distinct trip_id::text order by trip_id::text), '; ') AS trips_late, \n"
                        + "	 s.id AS stop_id, \n"
                        + "	 ad.stop_order AS stop_order \n"
                        + "	FROM arrivals_departures ad, stops s  \n"
                        + "WHERE "
                        // To get stop name
                        + " ad.config_rev = s.config_rev \n"
                        + " AND ad.stop_id = s.id \n"
                        // Only need arrivals/departures that have a schedule time
                        + " AND ad.scheduled_time IS NOT NULL \n"
                        // Specifies which routes to provide data for
                        + SqlUtils.routeClause(request.getParameter("r"), "ad") + "\n"
                        + SqlUtils.timeRangeClause(request, "ad.time", 7) + "\n"
                        + " AND time-scheduled_time > " + allowableLateMinutesStr + " \n"
                        + "	 GROUP BY direction_id, s.name, s.id, ad.stop_order \n"
                        + "	 ORDER BY direction_id, ad.stop_order, s.name \n"
                        + ") \n"
                        + "SELECT "
                        + "     COUNT(CASE WHEN scheduled_time-time > " + allowableEarlyMinutesStr + " THEN 1 ELSE null END) as early, \n"
                        + "     COUNT(CASE WHEN scheduled_time-time <= " + allowableEarlyMinutesStr + " AND time-scheduled_time <= "
                        + allowableLateMinutesStr + " THEN 1 ELSE null END) AS ontime, \n"
                        + "     COUNT(CASE WHEN time-scheduled_time > " + allowableLateMinutesStr + " THEN 1 ELSE null END) AS late, \n"
                        + "     COUNT(*) AS total, \n"
                        + "     s.name AS stop_name, \n"
                        + "     ad.direction_id AS direction_id, \n"
                        + " 	trips_early_query.trips_early as trips_early, \n"
                        + " 	trips_late_query.trips_late as trips_late \n"
                        + "FROM arrivals_departures ad"
                        + "	INNER JOIN Stops s ON ad.stop_id = s.id \n"
                        + "	LEFT JOIN trips_early_query ON s.id = trips_early_query.stop_id AND ad.stop_order = trips_early_query.stop_order \n"
                        + "	LEFT JOIN trips_late_query ON s.id = trips_late_query.stop_id AND ad.stop_order = trips_late_query.stop_order \n"
                        + "WHERE "
                        // To get stop name
                        + " ad.config_rev = s.config_rev \n"
                        + " AND ad.stop_id = s.id \n"
                        // Only need arrivals/departures that have a schedule time
                        + " AND ad.scheduled_time IS NOT NULL \n"
                        // Specifies which routes to provide data for
                        + SqlUtils.routeClause(request.getParameter("r"), "ad") + "\n"
                        + SqlUtils.timeRangeClause(request, "ad.time", 7) + "\n"
                        // Grouping and ordering is a bit complicated since might also be looking
                        // at old arrival/departure data that doen't have stoporder defined. Also,
                        // when configuration changes happen then the stop order can change.
                        // Therefore want to group by directionId and stop name. Need to also
                        // group by stop order so that can output it, which can be useful for
                        // debugging, plus need to order by stop order. For the ORDER BY clause
                        // need to order by direction id and stop order, but also the stop name
                        // as a backup for if stoporder not defined for data and is therefore
                        // always the same and doesn't provide any ordering info.
                        + " GROUP BY direction_id, s.name, s.id, ad.stop_order, trips_early_query.trips_early, trips_late_query.trips_late \n"
                        + " ORDER BY direction_id, ad.stop_order, s.name";

        String agencyId = request.getParameter("a");
        String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(jsonString);
    } catch (Exception e) {
        response.setStatus(400);
        response.getWriter().write(e.getMessage());
        return;
    }%>