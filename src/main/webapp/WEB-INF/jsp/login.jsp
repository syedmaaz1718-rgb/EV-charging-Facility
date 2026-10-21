<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Sign in - EV ChargeNet</title><link rel="stylesheet" href="/css/app.css">
<style>.flash{padding:12px 16px;border-radius:10px;margin-bottom:16px;font-size:14px;background:#3a1414;color:#f87171}</style></head><body>
<div class="loginwrap"><div class="lleft">
<div class="logo" style="padding:0 0 30px">&#9889; EV<span>ChargeNet</span></div>
<h1>Smart EV Charging<br>Network Platform</h1>
<p>Book charging slots, track live queues, and manage charging sessions. Secure, fast, always on.</p>
<p style="margin-top:26px"><a href="/" style="color:#4ade80">&larr; Back to home</a></p></div>
<div class="lright"><div class="lbox">
<h2>Welcome back</h2><div class="sub">Sign in to your account</div>
<c:if test="${not empty error}"><div class="flash" style="margin-top:14px">${error}</div></c:if>
<form method="post" action="/login" style="margin-top:18px">
<label>Email</label><input name="email" type="email" required autocomplete="email">
<label>Password</label><input name="password" type="password" required autocomplete="current-password">
<button class="btn" style="width:100%;padding:13px">Sign In</button></form>
<div class="sub" style="text-align:center;margin-top:16px">New here? <a href="/register" style="color:#22c55e;font-weight:600">Register your EV</a></div>
</div></div></div></body></html>
