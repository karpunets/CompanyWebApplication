<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@include file="/parts/head.jspf" %>
    <title>Grants</title>
    <link href="/css/grants.css" rel="stylesheet">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <%@include file="/parts/jqueryui.jspf" %>
    <script type="text/javascript">
        $(document).ready(function () {
            $("#grantName").selectmenu({
                change: function (event, data) {
                    $.get('get/grants', {
                        grantName: data.item.value
                    }, function (responseText) {
                        $('#ajaxGetGrantsResponse').html(responseText);
                    });
                }
            });
        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <select name="grant" id="grantName">
        <option disabled selected>Please pick grant</option>
        <option value="administrator">Administrator</option>
        <option value="manager">Manager</option>
        <option value="employee">Employee</option>
        <option value="customer">Customer</option>
    </select>
    <div id="ajaxGetGrantsResponse"></div>
</div>
</body>
</html>