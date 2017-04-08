<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>Create project</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/new_project.css" rel="stylesheet">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <%@include file="/parts/jqueryui.jspf" %>
    <script type="text/javascript">
        $(document).ready(function () {
            var manager, customer;
            $("#datepickerStart").datepicker({
                minDate: 0
//                firstDay:1,
//                dateFormat:"dd.mm.yy"
            });
            $("#datepickerEnd").datepicker({
                minDate: 0
            });
            $("#manager").selectmenu({
                width: 400,
                change: function (event, data) {
                    manager = data.item.value;
                }
            });
            $("#customer").selectmenu({
                width: 400,
                change: function (event, data) {
                    customer = data.item.value;
                }
            });
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
                var posting = $.post("/create/project", {
                    name: $form.find("input[name='name']").val(),
                    description: $form.find("#description").val(),
                    manager: manager,
                    customer: customer,
                    startingDate: $form.find("input[name='startingDate']").val(),
                    endingDate: $form.find("input[name='endingDate']").val()
                });
                posting.done(function (data) {
                    var projectId = parseInt(data);
                    if (isNaN(projectId)) {
                        $("#dialogContent").html(data);
                        $("#dialog").dialog("open");
                    } else {
                        $(location).attr('href', "/project?id=" + projectId);
                    }
                });
            });
        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <form method="post" id="ajax_form" action="">
        <span>Name</span>
        <input type="text" name="name"/>
        <span>Description</span>
        <textarea name="description" id="description"></textarea>

        <span>Project Manager</span>
        <div>
            <select name="manager" id="manager">
                <option disabled selected>Please pick one</option>
                <c:forEach var="manager" items="${requestScope.managers}">
                    <option value="${manager.key}">${manager.value}</option>
                </c:forEach>
            </select>
        </div>

        <span>Customer</span>
        <div>
            <select name="customer" id="customer">
                <option disabled selected>Please pick one</option>
                <c:forEach var="customer" items="${requestScope.customers}">
                    <option value="${customer.key}">${customer.value}</option>
                </c:forEach>
            </select>
        </div>

        <span>Date of the starting</span>
        <input type="text" name="startingDate" id="datepickerStart"/>

        <span>Date of the ending</span>
        <input type="text" name="endingDate" id="datepickerEnd"/>

        <button type="submit" id="buttonSubmit">Create</button>
    </form>
</div>
<div id="dialog" title="Error">
    <div id="dialogContent"></div>
</div>
</body>
</html>