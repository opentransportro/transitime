<%@page import="org.transitclock.domain.webstructs.WebAgency" %>
<%@page import="java.util.Collection" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <%@include file="template/includes.jsp" %>

    <style type="text/css">
        /* center the table */
        #agencyList {
            margin-left: auto;
            margin-right: auto;
        }

        table {
            border-spacing: 0px;
        }

        /* adjust text in table */
        td {
            padding-left: 16px;
            padding-right: 16px;
            padding-top: 4px;
            padding-bottom: 4px;
            text-align: left;
        }

        /* Alternate row colors to make table more readable */
        tr:nth-child(odd) {
            background: #F6F6F6
        }

        tr:nth-child(even) {
            background: #EBEBEB
        }

        /* for handling names that are too long */
        #agencyName {
            width: 300px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

    </style>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Transit Clock</title>
</head>

<body>
<%@include file="template/header.jsp" %>
<div id="mainDiv">
    <div id="title"><fmt:message key="div.agencies"/></div>
    <table id="agencyList">
        <%
            // Output links for all the agencies
            Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
            for (WebAgency webAgency : webAgencies) {
                // Only output active agencies
                if (!webAgency.isActive())
                    continue;
        %>
        <tr>
            <td>
                <div id=agencyName><%= webAgency.getAgencyName() %>
                </div>
            </td>
            <td><a href="<%= request.getContextPath() %>maps/index.jsp?a=<%= webAgency.getAgencyId() %>"><fmt:message
                    key="div.maps"/></a></td><!-- title="Real-time maps" -->
            <td>
                <a href="<%= request.getContextPath() %>reports/index.jsp?a=<%= webAgency.getAgencyId() %>"><fmt:message
                        key="div.reports"/></a></td><!-- title="Reports on historic information" -->
            <!-- <td><a href="<%= request.getContextPath() %>/reports/apiCalls/index.jsp?a=<%= webAgency.getAgencyId() %>" title="API calls"><fmt:message key="div.api" /></a></td>-->
            <td><a href="<%= request.getContextPath() %>/status/index.jsp?a=<%= webAgency.getAgencyId() %>"><fmt:message
                    key="div.status"/></a></td><!-- title="Pages showing current status of system" -->
            <td>
                <a href="<%= request.getContextPath() %>/synoptic/index.jsp?a=<%= webAgency.getAgencyId() %>"><fmt:message
                        key="div.synoptic"/></a></td><!-- title="Real-time synoptic" -->
            <td>
                <a href="<%= request.getContextPath() %>/extensions/index.jsp?a=<%= webAgency.getAgencyId() %>"><fmt:message
                        key="div.extensions"/></a></td><!-- title="Page of links to extension to the system" -->
        </tr>
        <%
            }
        %>
    </table>

</div>

</body>
</html>