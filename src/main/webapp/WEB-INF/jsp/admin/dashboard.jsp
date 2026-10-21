<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Admin Dashboard"/><c:set var="active" value="dashboard"/>
<%@ include file="../header.jspf" %>
<div class="cards">
<div class="card"><div class="k">Active sessions</div><div class="v">${stats.activeSessions}</div></div>
<div class="card"><div class="k">Chargers online</div><div class="v">${stats.chargersTotal - stats.chargersFaulty - stats.chargersMaint} / ${stats.chargersTotal}</div>
<div class="d warn">${stats.chargersFaulty} faulty &middot; ${stats.chargersMaint} maintenance</div></div>
<div class="card"><div class="k">Revenue today</div><div class="v">&#8377;${stats.revenueToday}</div></div>
<div class="card"><div class="k">In queue</div><div class="v">${stats.inQueue}</div></div>
<div class="card"><div class="k">Customers</div><div class="v">${stats.customers}</div></div></div>
<div class="grid2"><div class="panel"><h3>Charging demand by hour (kWh)</h3>
<c:choose><c:when test="${maxDemand == 0}"><div class="sub">No completed sessions yet - the chart fills in as charging happens.</div></c:when>
<c:otherwise><div class="bars">
<c:forEach items="${demandByHour}" var="v" varStatus="h">
<div class="bar"><i style="height:${maxDemand > 0 ? (v / maxDemand * 100) : 0}%"></i><span>${h.index}</span></div>
</c:forEach></div></c:otherwise></c:choose></div>
<div class="panel"><h3>Charger status</h3>
<c:choose><c:when test="${stats.chargersTotal == 0}"><div class="sub">No chargers configured yet. Add a station first.</div></c:when>
<c:otherwise><table>
<tr><th>Free</th><td>${stats.chargersFree}</td></tr>
<tr><th>Occupied</th><td>${stats.chargersTotal - stats.chargersFree - stats.chargersFaulty - stats.chargersMaint}</td></tr>
<tr><th>Faulty</th><td>${stats.chargersFaulty}</td></tr>
<tr><th>Maintenance</th><td>${stats.chargersMaint}</td></tr></table></c:otherwise></c:choose></div></div>
<div class="grid2"><div class="panel"><h3>Live queue</h3>
<c:choose><c:when test="${empty queue}"><div class="sub">Queue is empty.</div></c:when>
<c:otherwise><table><tr><th>#</th><th>Booking</th><th>Station</th><th>Type</th><th>Est. wait</th><th>Status</th></tr>
<c:forEach items="${queue}" var="b" varStatus="i"><tr>
<td>${i.index+1}</td><td>#${b.id()}</td><td>Station ${b.stationId()}</td><td>${b.chargerType()}</td>
<td>~${b.estWaitMin()} min</td><td><span class="pill y">waiting</span></td></tr></c:forEach></table></c:otherwise></c:choose></div>
<div class="panel"><h3>Running sessions</h3>
<c:choose><c:when test="${empty running}"><div class="sub">No active charging sessions.</div></c:when>
<c:otherwise><table><tr><th>Customer</th><th>Station</th><th>Charger</th><th>Started</th></tr>
<c:forEach items="${running}" var="r"><tr>
<td>${r.customer}</td><td>${r.station}</td><td>${r.charger_code}</td><td>${r.start_time}</td></tr></c:forEach></table></c:otherwise></c:choose></div></div>
<div class="panel"><h3>Recent audit log</h3>
<c:choose><c:when test="${empty audit}"><div class="sub">No activity yet.</div></c:when>
<c:otherwise><table><tr><th>Time</th><th>User</th><th>Action</th></tr>
<c:forEach items="${audit}" var="a"><tr><td>${a.created_at}</td><td>${a.actor}</td><td>${a.action}</td></tr></c:forEach></table></c:otherwise></c:choose></div>
<%@ include file="../footer.jspf" %>
