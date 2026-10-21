<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Charging Stations"/><c:set var="active" value="stations"/>
<%@ include file="../header.jspf" %>
<div class="panel"><h3>All stations</h3>
<c:choose><c:when test="${empty stations}"><div class="sub">No stations yet. Add your first station below.</div></c:when>
<c:otherwise><table><tr><th>Station</th><th>Area</th><th>Free</th><th>Busy</th><th>Faulty</th><th>Maint.</th><th>Status</th></tr>
<c:forEach items="${stations}" var="s"><tr>
<td><b>${s.name()}</b></td><td>${s.area()}</td>
<td>${availability[s.id()][0]}</td><td>${availability[s.id()][1]}</td>
<td>${availability[s.id()][2]}</td><td>${availability[s.id()][3]}</td>
<td><span class="pill ${s.status()=='ONLINE'?'g':'y'}">${s.status()}</span></td></tr></c:forEach></table></c:otherwise></c:choose></div>
<div class="grid2"><div class="panel"><h3>Add station</h3>
<form method="post" action="/admin/stations">
<label>Station name</label><input name="name" placeholder="e.g. JP Nagar EV Hub" required>
<label>Area</label><input name="area" placeholder="JP Nagar, Bengaluru" required>
<label>Address</label><input name="address">
<div style="display:flex;gap:12px">
<div style="flex:1"><label>Fast chargers (60 kW)</label><input name="fast" type="number" min="0" value="2"></div>
<div style="flex:1"><label>Slow chargers (7.4 kW)</label><input name="slow" type="number" min="0" value="2"></div></div>
<div style="display:flex;gap:12px">
<div style="flex:1"><label>Fast rate (&#8377;/kWh)</label><input name="fastRate" type="number" step="0.5" value="18"></div>
<div style="flex:1"><label>Slow rate (&#8377;/kWh)</label><input name="slowRate" type="number" step="0.5" value="9"></div></div>
<button class="btn">Save station</button></form></div>
<div class="panel"><h3>Add charger</h3>
<c:choose><c:when test="${empty stations}"><div class="sub">Add a station first.</div></c:when>
<c:otherwise><form method="post" action="/admin/chargers">
<label>Station</label><select name="stationId">
<c:forEach items="${stations}" var="s"><option value="${s.id()}">${s.name()}</option></c:forEach></select>
<label>Code</label><input name="code" placeholder="F5" required>
<label>Type</label><select name="type"><option value="FAST">FAST</option><option value="SLOW">SLOW</option></select>
<div style="display:flex;gap:12px">
<div style="flex:1"><label>Power (kW)</label><input name="powerKw" type="number" step="0.1" value="60" required></div>
<div style="flex:1"><label>Rate (&#8377;/kWh)</label><input name="rate" type="number" step="0.5" value="18" required></div></div>
<button class="btn">Add charger</button></form></c:otherwise></c:choose></div></div>
<div class="panel"><h3>Charger status</h3>
<c:choose><c:when test="${empty chargers}"><div class="sub">No chargers yet.</div></c:when>
<c:otherwise><table><tr><th>Charger</th><th>Station</th><th>Type</th><th>Power</th><th>Rate</th><th>Status</th><th>Set status</th></tr>
<c:forEach items="${chargers}" var="c"><tr>
<td><b>${c.code()}</b></td><td>${c.stationId()}</td><td>${c.type()}</td><td>${c.powerKw()} kW</td><td>&#8377;${c.ratePerKwh()}</td>
<td><span class="pill ${c.status()=='FREE'?'g':c.status()=='OCCUPIED'?'b':'r'}">${c.status()}</span></td>
<td><form method="post" action="/admin/chargers/${c.id()}/status" style="display:flex;gap:6px">
<select name="status" style="width:130px;margin:0;padding:6px">
<option ${c.status()=='FREE'?'selected':''}>FREE</option>
<option ${c.status()=='FAULTY'?'selected':''}>FAULTY</option>
<option ${c.status()=='MAINTENANCE'?'selected':''}>MAINTENANCE</option></select>
<button class="btn ghost" style="padding:6px 12px">Set</button></form></td></tr></c:forEach></table></c:otherwise></c:choose></div>
<%@ include file="../footer.jspf" %>
