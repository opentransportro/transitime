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
<title><fmt:message key="div.ep" /></title>
</head>
<body>
<%@include file="/template/header.jsp" %>
<div id="mainDiv">
<div id="title"><fmt:message key="div.ef" /> <%= WebAgency.getCachedWebAgency(agencyId).getAgencyName() %></div>
<ul class="choicesList">
  <li><a href="../holding/singlestopholding.html?agency=1&route=100&stop=20097&agencyname=VIA&stopname=TEXAS%20MEDICAL%20CENTER&threshold=10000"
    title="Dispatch  Panel">
     <fmt:message key="div.htns" /> </a></li> 
  <li><a href="../holding/singlestopholding.html?agency=1&route=100&stop=93296&agencyname=VIA&stopname=CHESTNUT%20AT%20ELLIS%20ALLEY&threshold=10000"
    title="Dispatch  Panel">
      <fmt:message key="div.htss" /> </a></li>
</ul>
</div>
</body>
</html>