<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="title" value="Customers"/><c:set var="active" value="customers"/>
<%@ include file="../header.jspf" %>
<div class="panel"><h3>Registered customers</h3>
<c:choose><c:when test="${empty customers}"><div class="sub">No customers registered yet.</div></c:when>
<c:otherwise><table><tr><th>Name</th><th>Email</th><th>Phone</th><th>Joined</th></tr>
<c:forEach items="${customers}" var="c"><tr>
<td><b>${c.name()}</b></td><td>${c.email()}</td><td>${c.phone()}</td><td>${c.createdAt()}</td></tr></c:forEach></table></c:otherwise></c:choose></div>
<%@ include file="../footer.jspf" %>
