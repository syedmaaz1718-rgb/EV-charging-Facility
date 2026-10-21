<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Charging History"/><c:set var="active" value="history"/>
<%@ include file="header.jspf" %>
<div class="panel"><h3>Past sessions</h3>
<c:choose><c:when test="${empty rows}"><div class="sub">No charging sessions yet. Your completed sessions and invoices will appear here.</div></c:when>
<c:otherwise><table><tr><th>Date</th><th>Station</th><th>Charger</th><th>Energy</th><th>Cost</th><th>Invoice</th><th></th></tr>
<c:forEach items="${rows}" var="r"><tr>
<td>${r.start_time}</td><td>${r.station}</td><td>${r.charger_code} (${r.charger_type})</td>
<td>${r.energy_kwh} kWh</td><td>&#8377;${r.amount}</td>
<td><c:choose><c:when test="${r.invoice_status=='PAID'}"><span class="pill g">paid</span></c:when>
<c:otherwise><span class="pill y">unpaid</span></c:otherwise></c:choose></td>
<td><a href="/invoice/${r.invoice_id}" class="pill b" style="text-decoration:none">view</a></td>
</tr></c:forEach></table></c:otherwise></c:choose></div>
<%@ include file="footer.jspf" %>
