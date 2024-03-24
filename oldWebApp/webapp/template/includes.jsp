<script type="text/javascript" src="https://code.jquery.com/jquery-1.12.4.min.js"></script>

<%-- Load in JQuery UI javascript and css to set general look and feel, such as for tooltips --%>
<script type="text/javascript" src="<%= request.getContextPath() %>/jquery-ui/jquery-ui.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/jquery-ui/jquery-ui.css">

<%-- Load in Transitime css and javascript libraries. Do this after jquery files
     loaded so can override those parameters as necessary. --%>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/general.css">
<script src="<%= request.getContextPath() %>/javascript/transitime.js"></script>

<script type="text/javascript">
    // This needs to match the API key in the database
    //var apiKey = "f78a2e9a"
    const apiKey = "<%=System.getProperty("transitclock.apikey")%>";
    // For accessing the api for an agency command
    const apiUrlPrefixAllAgencies = "/api/v1/key/" + apiKey;
    const apiUrlPrefix = apiUrlPrefixAllAgencies + "/agency/<%= request.getParameter("a") %>";
</script>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="language" value="" scope="session"/>
<fmt:setLocale value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}"/>
<fmt:requestEncoding value="UTF-8"/>
<fmt:setBundle basename="org.transitclock.i18n.text"/>