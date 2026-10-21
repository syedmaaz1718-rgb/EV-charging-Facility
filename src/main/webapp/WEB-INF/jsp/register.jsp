<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Register - EV ChargeNet</title><link rel="stylesheet" href="/css/app.css">
<style>.flash{padding:12px 16px;border-radius:10px;margin-bottom:16px;font-size:14px;background:#3a1414;color:#f87171}</style></head><body>
<div class="loginwrap"><div class="lleft">
<div class="logo" style="padding:0 0 30px">&#9889; EV<span>ChargeNet</span></div>
<h1>Join the charging<br>network</h1>
<p>Create your account, register your EV, and book your first charging slot in minutes.</p>
<p style="margin-top:26px"><a href="/" style="color:#4ade80">&larr; Back to home</a></p></div>
<div class="lright"><div class="lbox">
<h2>Create account</h2><div class="sub">For EV owners</div>
<c:if test="${not empty error}"><div class="flash" style="margin-top:14px">${error}</div></c:if>
<form method="post" action="/register" style="margin-top:18px">
<label>Full name</label><input name="name" required>
<label>Email</label><input name="email" type="email" required>
<label>Phone</label><input name="phone">
<label>Password (min 6 chars)</label><input name="password" type="password" minlength="6" required>
<button class="btn" style="width:100%;padding:13px">Register</button></form>
<div class="sub" style="text-align:center;margin-top:16px">Have an account? <a href="/login" style="color:#22c55e;font-weight:600">Sign in</a></div>
</div></div></div></body></html>
