<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.karpunets.pojo.Task" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>My tasks</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/tasks.css" rel="stylesheet">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <script>
        function deleteTask(taskId) {
            var posting = $.post("/update/grant/task", {
                taskId: taskId
            });
            posting.done(function (data) {
                $("tr[data-taskId = " + taskId + "]").remove();
            });
        }
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <div class="content_wrapper">
        <table>
            <thead>
            <tr>
                <th>Id</th>
                <th>The date of the starting</th>
                <th>Estimate</th>
                <th>Qualification</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:if test="${not empty requestScope.newTasks}">
                <tr>
                    <td class="thead_project" colspan="5">New taks</td>
                </tr>
            </c:if>
            <%SimpleDateFormat dateFormat = (SimpleDateFormat) request.getAttribute("dateFormat");%>
            <%for (Task task : (Set<Task>) request.getAttribute("newTasks")) {%>
            <tr>
                <td>
                    <a href="/task?id=<%=task.getId()%>"><%=task.getId()%>
                    </a>
                </td>
                <td>
                    <% if (task.getStartingDate() != null) {%>
                    <%=dateFormat.format(task.getStartingDate())%>
                    <%}%>
                </td>
                <td><%=task.getEstimate()%>
                </td>
                <td><%=task.getQualification()%>
                </td>
                <td></td>
            </tr>
            <%}%>
            <c:if test="${not empty requestScope.tasks}">
                <tr>
                    <td class="thead_project" colspan="5">Task</td>
                </tr>
            </c:if>
            <%for (Task task : (Set<Task>) request.getAttribute("tasks")) {%>
            <% if (task.isFinished()) {%>
                <tr class="finished" data-taskId="<%=task.getId()%>">
            <%} else {%>
                <tr>
            <%}%>
                <td>
                    <a href="/task?id=<%=task.getId()%>"><%=task.getId()%>
                    </a>
                </td>
                <td>
                    <% if (task.getStartingDate() != null) {%>
                    <%=dateFormat.format(task.getStartingDate())%>
                    <%}%>
                </td>
                <td><%=task.getEstimate()%>
                </td>
                <td><%=task.getQualification()%>
                </td>
                <td>
                <% if (task.isFinished()) {%>
                <div class="deleteTaskBtn">
                    <span onclick="deleteTask(<%=task.getId()%>)"></span>
                </div>
                <%}%>
                </td>
            </tr>
            <%}%>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
