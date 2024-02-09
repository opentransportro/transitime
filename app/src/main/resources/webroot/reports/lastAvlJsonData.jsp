<%@ page  contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitclock.core.reports.GenericJsonQuery" %>
<%@ page import="org.transitclock.domain.webstructs.WebAgency" %>
<%
String agencyId = request.getParameter("a");
WebAgency agency = WebAgency.getCachedWebAgency(agencyId);
String dbtype = agency.getDbType();
String sql = null;
if(dbtype.equals("mysql")){
	sql = 
	"SELECT a.vehicle_id, vC.name, max_time, lat, lon "
	+ "FROM " 
	+ "(SELECT vehicle_id, max(time) AS max_time "
	+ "FROM avl_reports WHERE time > date_sub(now(), interval 1 day) "
	+ "GROUP BY vehicle_id) a "
	+ "JOIN avl_reports b ON a.vehicle_id=b.vehicle_id AND a.max_time = b.time "
	+ "JOIN vehicle_configs vC ON a.vehicleId=vC.id";
}
if(dbtype.equals("postgresql"))
{
	sql =
					"select a.vehicle_id as \"vehicleId\", vC.name as \"name\", a.maxTime as \"maxTime\", lat, lon from ( "
					+ "SELECT vehicle_id, max(time) AS maxTime "
					+ "FROM avl_reports WHERE time > now() + '-24 hours' "
					+ "GROUP BY vehicle_id) a "
					+ "JOIN avl_reports b ON a.vehicle_id=b.vehicle_id AND a.maxTime = b.time "
					+ "JOIN vehicle_configs vC ON a.vehicle_id=vC.id";
}
	
	
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>