<%@ page  contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.transitclock.core.reports.GenericJsonQuery" %>
<%@ page import="org.transitclock.domain.webstructs.WebAgency" %>
<%
String agencyId = request.getParameter("a");
String sql =
        "select a.vehicle_id as \"vehicleId\", vC.name as \"name\", a.maxTime as \"maxTime\", lat, lon from ( "
        + "SELECT vehicle_id, max(time) AS maxTime "
        + "FROM avl_reports WHERE time > now() + '-24 hours' "
        + "GROUP BY vehicle_id) a "
        + "JOIN avl_reports b ON a.vehicle_id=b.vehicle_id AND a.maxTime = b.time "
        + "JOIN vehicle_configs vC ON a.vehicle_id=vC.id";

	
String jsonString = GenericJsonQuery.getJsonString(agencyId, sql);
response.setHeader("Access-Control-Allow-Origin", "*");
response.getWriter().write(jsonString);
%>