<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>Home</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/home.css" rel="stylesheet">
    <%@include file="/parts/jqueryui.jspf" %>
    <script type="text/javascript">

        $(document).ready(function () {
            $("#dialog").dialog({
                minWidth: 500,
                autoOpen: false,
                show: {
                    effect: "fade",
                    duration: 500
                },
                hide: {
                    effect: "fade",
                    duration: 500
                }
            });
            $(".notices").on("click", function () {
                $("#dialog").dialog("open");
            });
        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <c:if test="${not empty requestScope.notices}">
        <div class="notices box"><span>${requestScope.notices.size()} notices</span></div>
        <div id="dialog" title="Notice">
            <c:forEach var="item" items="${requestScope.notices}">
                <a href="${item.value}">${item.key}</a><br>
            </c:forEach>
        </div>
    </c:if>
    <c:forEach var="item" items="${requestScope.menuMap}">
        <a class="box" href="${item.value}"><span>${item.key}</span></a>
    </c:forEach>
</div>
</body>
</html>
