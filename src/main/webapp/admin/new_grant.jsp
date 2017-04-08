<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>Create grant</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/new_grant.css" rel="stylesheet">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <%@include file="/parts/jqueryui.jspf" %>
    <script type="text/javascript">
        $(document).ready(function () {
            var grant, qualification;
            $("select[name='grant']").selectmenu({
                width:  400,
                change: function (event, data) {
                    grant = data.item.value;
                    if (grant == "manager" || grant == "employee") {
                        $("#selectQualification").show();
                    } else {
                        $("#selectQualification").hide();
                    }
                }
            });
            $("select[name='qualification']").selectmenu({
                width: 400,
                change: function (event, data) {
                    qualification = data.item.value;
                }
            });
            $("#buttonSubmit").button({
                icon: "ui-icon-disk"
            })
            $("#selectQualification").hide();
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
            $("#ajax_form").submit(function (event) {
                event.preventDefault();
                var $form = $(this);
                var posting = $.post("/create/grant", {
                    login: $form.find("input[name='login']").val(),
                    password: $form.find("input[name='password']").val(),
                    name: $form.find("input[name='name']").val(),
                    surname: $form.find("input[name='surname']").val(),
                    grant: grant,
                    qualification: qualification
                });
                posting.done(function (data) {
                    var profileId = parseInt(data);
                    if (isNaN(profileId)) {
                        $("#dialogContent").html(data);
                        $("#dialog").dialog("open");
                    } else {
                        $(location).attr('href', "/profile?id=" + profileId);
                    }
                });
//                    .fail(function (data) {
//
//                    $("#dialogContent").html(data);
//                    $("#dialog").dialog("open");
//                    alert("error" + data);
//                });
            });
        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <form method="post" id="ajax_form" action="">
        <span>Login</span>
        <input type="text" name="login" value="${requestScope.login}"/>
        <span>Password</span>
        <input type="password" name="password" value="${requestScope.password}"/>
        <span>Name</span>
        <input type="text" name="name" value="${requestScope.name}"/>
        <span>Surname</span>
        <input type="text" name="surname" value="${requestScope.surname}"/>
        <div class="select" id="selectGrant">
            <select name="grant">
                <option disabled selected>Please pick grant</option>
                <option value="administrator">Administrator</option>
                <option value="manager">Manager</option>
                <option value="employee">Employee</option>
                <option value="customer">Customer</option>
            </select>
        </div>
        <div class="select" id="selectQualification">
            <select  name="qualification">
                <option disabled selected>Please pick qualification</option>
                <c:forEach var="qualification" items="${requestScope.qualifications}">
                    <option>${qualification}</option>
                </c:forEach>
            </select>
        </div>
        <button type="submit">Create</button>
    </form>
</div>
<div id="dialog" title="Error">
    <div id="dialogContent"></div>
</div>
</body>
</html>