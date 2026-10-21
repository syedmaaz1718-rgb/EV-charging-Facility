<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Reports &amp; Demand"/><c:set var="active" value="reports"/>
<%@ include file="../header.jspf" %>
<div style="margin-bottom:16px;display:flex;gap:10px">
<a href="/export/sessions.csv" class="btn" style="text-decoration:none">Export sessions CSV</a>
<a href="/admin/backup.sql" class="btn ghost" style="text-decoration:none">Download database backup</a></div>
<div class="grid2"><div class="panel"><h3>Daily report (last 31 days)</h3>
<c:choose><c:when test="${empty daily}"><div class="sub">No completed sessions yet - daily reports build up automatically.</div></c:when>
<c:otherwise><table><tr><th>Day</th><th>Sessions</th><th>Energy (kWh)</th><th>Revenue</th></tr>
<c:forEach items="${daily}" var="d"><tr>
<td>${d.report_day}</td><td>${d.sessions}</td><td>${d.energy}</td><td>&#8377;${d.revenue}</td></tr></c:forEach></table></c:otherwise></c:choose></div>
<div class="panel"><h3>Monthly demand</h3>
<c:choose><c:when test="${empty monthly}"><div class="sub">No data yet.</div></c:when>
<c:otherwise><table><tr><th>Month</th><th>Sessions</th><th>Energy (kWh)</th><th>Revenue</th></tr>
<c:forEach items="${monthly}" var="mo"><tr>
<td>${mo.ym}</td><td>${mo.sessions}</td><td>${mo.energy}</td><td>&#8377;${mo.revenue}</td></tr></c:forEach></table></c:otherwise></c:choose></div></div>
<div class="panel"><h3>Station utilization</h3>
<c:choose><c:when test="${empty utilization}"><div class="sub">No stations yet.</div></c:when>
<c:otherwise><table><tr><th>Station</th><th>Sessions</th><th>Energy (kWh)</th><th>Revenue</th></tr>
<c:forEach items="${utilization}" var="u"><tr>
<td>${u.name}</td><td>${u.sessions}</td><td>${u.energy}</td><td>&#8377;${u.revenue}</td></tr></c:forEach></table></c:otherwise></c:choose></div>
<%@ include file="../footer.jspf" %>
