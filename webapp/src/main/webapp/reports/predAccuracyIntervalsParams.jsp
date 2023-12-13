<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><fmt:message key="div.SpecifyParameters" /></title>

  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
  <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
  
  <link href="params/reportParams.css" rel="stylesheet"/>  
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   <fmt:message key="div.sppaic" />
</div>
   
<div id="mainDiv">
<form action="predAccuracyIntervalsChart.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/routeAllOrSingle.jsp" />
 
   <jsp:include page="params/fromDateNumDaysTime.jsp" />
   
   <jsp:include page="params/predictionSource.jsp" />
 
   <div class="param">
     <label for="predictionType"><fmt:message key="div.psource" />:</label> 
     <select id="predictionType" name="predictionType" 
     	title="Specifies whether or not to show prediction accuracy for 
     	predictions that were affected by a layover. Select 'All' to show
     	data for predictions, 'Affected by layover' to only see data where
     	predictions affected by when a driver is scheduled to leave a layover, 
     	or 'Not affected by layover' if you only want data for predictions 
     	that were not affected by layovers.">
       <option value=""><fmt:message key="div.pall" /></option>
       <option value="AffectedByWaitStop"><fmt:message key="div.paff" /></option>
       <option value="NotAffectedByWaitStop"><fmt:message key="div.pnaff" /></option>
     </select>
   </div>
 
   <div class="param">
     <label for="intervalsType"><fmt:message key="div.intt" />:</label> 
     <select id="intervalsType" name="intervalsType"
     	title="Specifies the type of graph to be displayed. By selecting
     		'Percentage only' the charge will display the prediction accuracy
     		range that is within the percentages specified below. By selecting
     		'Standard Deviation only' the chart will display the prediction
     		accuracy range for predictions within a standard deviation of the 
     		mean. This method is experimental and might be removed. By
     		selecting 'Percentage only and Standard Deviation' the chart will
     		display both a percentage interval and a standard deviation 
     		interval.">
       <option value="PERCENTAGE"><fmt:message key="div.pon" /></option>
       <option value="STD_DEV"><fmt:message key="div.sdon" /></option>
       <option value="BOTH"><fmt:message key="div.ponsd" /></option>
     </select>
   </div>
 
   <div class="param">
    <label for="intervalPercentage1"><fmt:message key="div.iper" />1:</label>
    <input id="intervalPercentage1" name="intervalPercentage1"
    	title="For when using a 'Percentage' interval type. Specifies the 
    	       percent of predictions that should lie within the minimum 
    	       and maximum intervals." 
    	type="number"
    	value="70"
    	min="0"
    	max="100"/> <span class="note">%</span>
  </div>
 
   <div class="param">
    <label for="intervalPercentage2"><fmt:message key="div.iper" />2:</label>
    <input id="intervalPercentage2" name="intervalPercentage2"
    	title="Optional value for when using a 'Percentage' interval type. 
    		Specifies a second percent of predictions that should lie within 
    		the minimum and maximum intervals." 
    	type="number"
    	min="0"
    	max="100"/> <span class="note">%</span>
  </div>
    
    <jsp:include page="params/submitReport.jsp" />
  </form>
</div>

</body>
</html>