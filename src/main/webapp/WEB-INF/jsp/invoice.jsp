<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Invoice"/><c:set var="active" value="history"/>
<%@ include file="header.jspf" %>
<div class="inv">
<h3 style="margin-bottom:4px">${inv.invoiceNo()}</h3>
<div class="sub" style="margin-bottom:16px">Issued ${inv.issuedAt()} &middot; ${inv.status()}</div>
<div class="row"><span>Station</span><b>${station.name()}</b></div>
<div class="row"><span>Charger</span><b>${charger.code()} (${charger.type()} ${charger.powerKw()} kW)</b></div>
<div class="row"><span>Started</span><b>${s.startTime()}</b></div>
<div class="row"><span>Ended</span><b>${s.endTime()}</b></div>
<div class="row"><span>Energy delivered</span><b>${s.energyKwh()} kWh</b></div>
<div class="row"><span>Rate</span><b>&#8377;${charger.ratePerKwh()} / kWh</b></div>
<div class="row tot"><span>Total</span><span>&#8377;${inv.amount()}</span></div>
<div style="display:flex;gap:10px;margin-top:18px">
<c:if test="${inv.status()=='UNPAID'}">
<form method="post" action="/invoice/pay/${inv.id()}"><button class="btn">Pay now</button></form></c:if>
<button class="btn ghost" onclick="window.print()">Print / save PDF</button>
<a href="/history" class="btn ghost" style="text-decoration:none;line-height:2.3">Back</a></div></div>
<%@ include file="footer.jspf" %>
