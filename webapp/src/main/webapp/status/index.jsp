<%@page import="org.transitclock.db.webstructs.WebAgency"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String agencyId = request.getParameter("a");
if (agencyId == null || agencyId.isEmpty()) {
    response.getWriter().write("You must specify agency in query string (e.g. ?a=mbta)");
    return;
}
%>
<html>
<head>
<%@include file="/template/includes.jsp" %> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><fmt:message key="div.statuspages" /></title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title"><fmt:message key="div.srf" /> <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
<ul class="choicesList">
  <li><a href="activeBlocks.jsp?a=<%= agencyId %>"
    title="Shows how many block assignments are currently active and if they have assigned vehicles">
      <fmt:message key="div.acbiveblock" /></a></li>
  <li><a href="../maps/schAdhMap.jsp?a=<%= agencyId %>"
    title="Shows current real-time schedule adherence of vehicles in map">
      <fmt:message key="div.ScheduleAdherenceMap" /></a></li>
  <li><a href="serverStatus.jsp?a=<%= agencyId %>"
    title="Shows how well system is running, including the AVL feed">
      <fmt:message key="div.ss" /></a></li>
  <li><a href="dbDiskSpace.jsp?a=<%= agencyId %>"
    title="Shows how much disk space is being used by the database. Currently only works for agencies where PostgreSQL database is used.">
      <fmt:message key="div.ddsu" /></a></li>
</ul>
</div>
</body>
</html>
