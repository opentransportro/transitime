<%@page import="org.transitclock.domain.webstructs.WebAgency" %>

<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
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
    <title><fmt:message key="div.statuspages"/></title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
    <div id="title"><fmt:message key="div.realtime"/> <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %>
    </div>
    <ul class="choicesList">
        <li><a href="../maps/map.jsp?verbose=true&a=<%= agencyId %>"
               title="Real-time map for selected route">
            <fmt:message key="div.mapfor"/></a></li>
        <li><a href="../maps/map.jsp?verbose=true&a=<%= agencyId %>&showUnassignedVehicles=true"
               title="Real-time map for selected route but also shows vehicles not currently assigned to a route">
            <fmt:message key="div.mapincluding"/></a></li>
        <li><a href="../maps/schAdhMap.jsp?a=<%= agencyId %>"
               title="Shows current real-time schedule adherence of vehicles in map">
            <fmt:message key="div.ScheduleAdherenceMap"/></a></li>
        <li><a href="../maps/schAdhMap_v2.jsp?a=<%= agencyId %>"
               title="Shows current real-time schedule adherence of vehicles in map">
            <fmt:message key="div.ScheduleAdherenceMap"/> v2</a></li>
    </ul>
</div>
</body>
</html>