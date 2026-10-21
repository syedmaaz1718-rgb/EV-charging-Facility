<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Book Charging Slot"/><c:set var="active" value="dashboard"/>
<%@ include file="header.jspf" %>
<div class="grid2"><div>
<div class="panel"><h3>Book a slot</h3>
<c:choose><c:when test="${empty stations}"><div class="sub">No charging stations yet - please check back soon.</div></c:when>
<c:otherwise>
<c:if test="${empty vehicles}">
<div class="sub" style="margin-bottom:14px">Add your vehicle first, then book a slot.</div></c:if>
<form method="post" action="/book">
<label>Vehicle</label>
<select name="vehicleId" ${empty vehicles?'disabled':''}>
<c:forEach items="${vehicles}" var="v"><option value="${v.id()}">${v.label()} (${v.batteryKwh()} kWh)</option></c:forEach>
</select>
<label>Charging station</label>
<select name="stationId">
<c:forEach items="${stations}" var="s">
<option value="${s.id()}">${s.name()} &middot; ${s.area()}<c:if test="${not empty availability[s.id()]}"> &middot; ${availability[s.id()][0]} free</c:if></option>
</c:forEach></select>
<label>Charger type</label>
<select name="chargerType"><option value="FAST">Fast DC 60kW</option><option value="SLOW">Slow AC 7.4kW</option></select>
<label>Date &amp; time</label><input name="slotTime" type="datetime-local" required>
<button class="btn" ${empty vehicles?'disabled':''}>Check availability &amp; book</button></form>
</c:otherwise></c:choose></div>
<div class="panel"><h3>Your vehicles</h3>
<c:if test="${empty vehicles}"><div class="sub">No vehicles yet.</div></c:if>
<c:forEach items="${vehicles}" var="v">
<div class="stationcard"><div><div class="nm">${v.model()}</div><div class="ad">${v.regNo()} &middot; ${v.batteryKwh()} kWh</div></div></div>
</c:forEach>
<form method="post" action="/book/vehicle" style="margin-top:14px">
<label>Vehicle number</label><input name="regNo" placeholder="KA05MN2211" required>
<label>Model</label><input name="model" placeholder="Tata Nexon EV" required>
<label>Battery (kWh)</label><input name="batteryKwh" type="number" step="0.1" value="40" required>
<button class="btn ghost">Add vehicle</button></form></div>
</div><div>
<div class="panel"><h3>Your bookings</h3>
<c:if test="${empty myBookings}"><div class="sub">No bookings yet. Book your first slot!</div></c:if>
<c:forEach items="${myBookings}" var="b">
<div class="stationcard actions"><div>
<div class="nm">Booking #${b.id()} &middot; ${b.chargerType()}
<c:choose>
<c:when test="${b.status()=='ALLOCATED'}"><span class="pill g">charger allocated</span></c:when>
<c:when test="${b.status()=='QUEUED'}"><span class="pill y">in queue &middot; wait ~${b.estWaitMin()} min</span></c:when>
<c:when test="${b.status()=='IN_SESSION'}"><span class="pill b">charging now</span></c:when>
<c:when test="${b.status()=='COMPLETED'}"><span class="pill b">completed</span></c:when>
<c:otherwise><span class="pill r">${b.status()}</span></c:otherwise></c:choose></div>
<div class="ad">${b.slotTime()}</div></div>
<div>
<c:if test="${b.status()=='ALLOCATED'}">
<form method="post" action="/session/start/${b.id()}"><button class="btn">Start charging</button></form>
<form method="post" action="/book/${b.id()}/cancel"><button class="btn ghost">Cancel</button></form></c:if>
<c:if test="${b.status()=='QUEUED'}">
<form method="post" action="/book/${b.id()}/cancel"><button class="btn ghost">Leave queue</button></form></c:if>
</div></div></c:forEach></div>
<div class="panel"><h3>Station availability</h3>
<c:forEach items="${stations}" var="s">
<div class="stationcard"><div><div class="nm">${s.name()}</div><div class="ad">${s.area()}</div></div>
<div style="text-align:right"><span class="pill ${availability[s.id()][0] > 0 ? 'g' : 'y'}">${availability[s.id()][0]} free</span>
<div class="ad" style="margin-top:5px">${availability[s.id()][1]} busy &middot; ${availability[s.id()][2]} faulty</div></div></div>
</c:forEach></div>
</div></div>
<%@ include file="footer.jspf" %>
