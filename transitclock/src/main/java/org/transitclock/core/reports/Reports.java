package org.transitclock.core.reports;

import java.text.ParseException;

import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.utils.Time;
import org.transitclock.core.reports.SqlUtils;

public class Reports {
	
private static final int MAX_ROWS = 50000;

private static final int MAX_NUM_DAYS = 7;
	
	/**
	 * Queries agency for AVL data and returns result as a JSON string. Limited
	 * to returning MAX_ROWS (50,000) data points.
	 * 
	 * @param agencyId
	 * @param vehicleId
	 *            Which vehicle to get data for. Set to null or empty string to
	 *            get data for all vehicles
	 * @param beginDate
	 *            date to start query
	 * @param numdays
	 *            of days to collect data for
	 * @param beginTime
	 *            optional time of day during the date range
	 * @param endTime
	 *            optional time of day during the date range
	 * @return AVL reports in JSON format. Can be empty JSON array if no data
	 *         meets criteria.
	 */
	public static String getAvlJson(String agencyId, String vehicleId,
			String beginDate, String numdays, String beginTime, String endTime) {
		//Determine the time portion of the SQL
		String timeSql = "";
		WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
		// If beginTime or endTime set but not both then use default values
		if ((beginTime != null && !beginTime.isEmpty())
				|| (endTime != null && !endTime.isEmpty())) {
			if (beginTime == null || beginTime.isEmpty())
				beginTime = "00:00";
			if (endTime == null || endTime.isEmpty())
				endTime = "24:00";
		}
		//cast('2000-01-01 01:12:00'::timestamp as time);
		if (beginTime != null && !beginTime.isEmpty() 
				&& endTime != null && !endTime.isEmpty()) {
			if ("mysql".equals(agency.getDbType())) {
				timeSql = " AND time(time) BETWEEN '" 
						+ beginTime + "' AND '" + endTime + "' ";
			} else {
				timeSql = " AND cast(time::timestamp as time) BETWEEN '" 
						+ beginTime + "' AND '" + endTime + "' ";
			}
				
		}
		
		String sql = "";		
		
		
		if ("mysql".equals(agency.getDbType())) {
			sql = "SELECT vehicleId, name, time, assignmentId, lat, lon, speed, "
				+ "heading, timeProcessed, source "
				+ "FROM avlreports "
				+ "INNER JOIN vehicleconfigs ON vehicleconfigs.id = avlreports.vehicleId "
				+ "WHERE time BETWEEN " + " cast(? as datetime)"
				+ " AND " + "date_add(cast(? as datetime), INTERVAL " + numdays + " day) "
				+ timeSql;
		} else {
			sql = "SELECT vehicleId, name, time, assignmentId, lat, lon, speed, "
				+ "heading, timeProcessed, source "
				+ "FROM avlreports "
				+ "INNER JOIN vehicleconfigs ON vehicleconfigs.id = avlreports.vehicleId "
				+ "WHERE time BETWEEN " + " cast(? as timestamp)"
				+ " AND " + "cast(? as timestamp)"  + " + INTERVAL '" + numdays + " day' "
				+ timeSql;
		}

		// If only want data for single vehicle then specify so in SQL
		if (vehicleId != null && !vehicleId.isEmpty())
			sql += " AND vehicleId='" + vehicleId + "' ";
		
		// Make sure data is ordered by vehicleId so that can draw lines 
		// connecting the AVL reports per vehicle properly. Also then need
		// to order by time to make sure they are in proper order. And
		// lastly, limit AVL reports to 5000 so that someone doesn't try
		// to view too much data at once.

		sql += "ORDER BY vehicleId, time LIMIT " + MAX_ROWS;
		
		String json=null;
		try {
			java.util.Date startdate = Time.parseDate(beginDate);						
			
			json = GenericJsonQuery.getJsonString(agencyId, sql,startdate, startdate);
				
		} catch (ParseException e) {			
			json=e.getMessage();
		}						

		return json;
	}
	
	/* Provides schedule adherence data in JSON format. Provides for
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
	 */
	public static String getScheduleAdhByStops(String agencyId, String route, String beginDate,
			String allowableEarly, String allowableLate, String beginTime, String endTime, int numDays) {
		if (allowableEarly == null || allowableEarly.isEmpty())
			allowableEarly = "1.0";
		String allowableEarlyMinutesStr = "'" + SqlUtils.convertMinutesToSecs(allowableEarly) + " seconds'";

		if (allowableLate == null || allowableLate.isEmpty())
			allowableLate = "4.0";
		String allowableLateMinutesStr = "'" + SqlUtils.convertMinutesToSecs(allowableLate) + " seconds'";
   		   
		String sql =
			"WITH trips_early_query AS ( SELECT "
			+ "	 array_to_string(array_agg(distinct tripid::text order by tripid::text), '; ') AS trips_early, \n"
			+ "	 s.id AS stop_id, \n"
			+ "	 ad.stopOrder AS stop_order \n"
			+ " 	FROM ArrivalsDepartures ad, Stops s  \n"
					+ "WHERE "
				    // To get stop name
				    + " ad.configRev = s.configRev \n"
				    + " AND ad.stopId = s.id \n"
				    // Only need arrivals/departures that have a schedule time
				    + " AND ad.scheduledTime IS NOT NULL \n"
				    // Specifies which routes to provide data for
				    + SqlUtils.routeClause(route, "ad") + "\n"
				    + SqlUtils.timeRangeClause(agencyId, "ad.time", MAX_NUM_DAYS, numDays, beginTime, endTime, beginDate) + "\n"
				    + " AND scheduledTime-time > " + allowableEarlyMinutesStr + " \n"
			+ "	 GROUP BY directionid, s.name, s.id, ad.stopOrder \n"
			+ "	 ORDER BY directionid, ad.stopOrder, s.name \n"
			+ "), \n"
			
			+ "trips_late_query AS ( SELECT "
			+ "	 array_to_string(array_agg(distinct tripid::text order by tripid::text), '; ') AS trips_late, \n"
			+ "	 s.id AS stop_id, \n"
			+ "	 ad.stopOrder AS stop_order \n"
			+ "	FROM ArrivalsDepartures ad, Stops s  \n"
					+ "WHERE "
				    // To get stop name
				    + " ad.configRev = s.configRev \n"
				    + " AND ad.stopId = s.id \n"
				    // Only need arrivals/departures that have a schedule time
				    + " AND ad.scheduledTime IS NOT NULL \n"
				    // Specifies which routes to provide data for
				    + SqlUtils.routeClause(route, "ad") + "\n"
				    + SqlUtils.timeRangeClause(agencyId, "ad.time", MAX_NUM_DAYS, numDays, beginTime, endTime, beginDate) + "\n"
				    + " AND time-scheduledTime > " + allowableLateMinutesStr + " \n"
			+ "	 GROUP BY directionid, s.name, s.id, ad.stopOrder \n"
			+ "	 ORDER BY directionid, ad.stopOrder, s.name \n"
			+ ") \n"
			+ "SELECT " 
			+ "     COUNT(CASE WHEN scheduledTime-time > " + allowableEarlyMinutesStr + " THEN 1 ELSE null END) as early, \n"
			+ "     COUNT(CASE WHEN scheduledTime-time <= " + allowableEarlyMinutesStr + " AND time-scheduledTime <= " 
						+ allowableLateMinutesStr + " THEN 1 ELSE null END) AS ontime, \n" 
		   + "     COUNT(CASE WHEN time-scheduledTime > " + allowableLateMinutesStr + " THEN 1 ELSE null END) AS late, \n" 
		   + "     COUNT(*) AS total, \n"
		   + "     s.name AS stop_name, \n"
		   + "     ad.directionid AS direction_id, \n"
		   + " 	trips_early_query.trips_early as trips_early, \n"
		   + " 	trips_late_query.trips_late as trips_late \n"
		   + "FROM ArrivalsDepartures ad"
		   + "	INNER JOIN Stops s ON ad.stopId = s.id \n"
		   + "	LEFT JOIN trips_early_query ON s.id = trips_early_query.stop_id AND ad.stopOrder = trips_early_query.stop_order \n"
		   + "	LEFT JOIN trips_late_query ON s.id = trips_late_query.stop_id AND ad.stopOrder = trips_late_query.stop_order \n"
		   + "WHERE "
		   // To get stop name
		   + " ad.configRev = s.configRev \n"
		   + " AND ad.stopId = s.id \n"
		   // Only need arrivals/departures that have a schedule time
		   + " AND ad.scheduledTime IS NOT NULL \n"
		   // Specifies which routes to provide data for
		   + SqlUtils.routeClause(route, "ad") + "\n"
		   + SqlUtils.timeRangeClause(agencyId, "ad.time", MAX_NUM_DAYS, numDays, beginTime, endTime, beginDate) + "\n"
		   // Grouping and ordering is a bit complicated since might also be looking
		   // at old arrival/departure data that doen't have stoporder defined. Also,
		   // when configuration changes happen then the stop order can change. 
		   // Therefore want to group by directionId and stop name. Need to also 
		   // group by stop order so that can output it, which can be useful for 
		   // debugging, plus need to order by stop order. For the ORDER BY clause
		   // need to order by direction id and stop order, but also the stop name
		   // as a backup for if stoporder not defined for data and is therefore 
		   // always the same and doesn't provide any ordering info.
		   + " GROUP BY directionid, s.name, s.id, ad.stopOrder, trips_early_query.trips_early, trips_late_query.trips_late \n"
		   + " ORDER BY directionid, ad.stopOrder, s.name";
		   		
		//Do the query and return result in JSON format    
			String jsonString = "";
			jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
			return jsonString;
	}
		
}
