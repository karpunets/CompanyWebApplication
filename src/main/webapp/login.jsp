<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>InCompany</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/login.css" rel="stylesheet">
</head>
<body>
<%@include file="/parts/header.jspf"%>
<div class="content">
    <form method="post" action="j_security_check">
        <div class="enter">Enter</div>
        <input type="text" class="text" name="j_username" placeholder="Enter login"/>
        <input type="password" class="text" name="j_password" placeholder="Enter password"/>
        <label>
            <input type="checkbox" class="checkbox" name="remember" value="true">
            <div class="remember">Remember me</div>
        </label>
        <button type="submit">Login in</button>
        <c:if test="${requestScope['javax.servlet.forward.request_uri'] eq '/j_security_check'}">
            <div id="message">Login failed</div>
        </c:if>
    </form>
</div>
</body>
</html>