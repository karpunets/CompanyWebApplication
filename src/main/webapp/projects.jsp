<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.karpunets.pojo.Project" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>My projects</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/projects.css" rel="stylesheet">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <script>
        function deleteProject(projectId) {
            var posting = $.post("/update/grant/project", {
                projectId: projectId
            });
            posting.done(function (data) {
                $("tr[data-projectId = " + projectId + "]").remove();
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
                <th>Name</th>
                <th>The date of the starting</th>
                <th>The date of the ending</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:if test="${not empty requestScope.newProjects}">
                <tr>
                    <td class="thead_project" colspan="5">New project</td>
                </tr>
            </c:if>
            <%SimpleDateFormat dateFormat = (SimpleDateFormat) request.getAttribute("dateFormat");%>
            <%for (Project project : (Set<Project>) request.getAttribute("newProjects")) {%>
            <tr>
                <td>
                    <a href="/project?id=<%=project.getId()%>"><%=project.getId()%>
                    </a>
                </td>
                <td><%=project.getName()%>
                </td>
                <td>
                    <% if (project.getStartingDate() != null) {%>
                    <%=dateFormat.format(project.getStartingDate())%>
                    <%}%>
                </td>
                <td>
                    <% if (project.getEndingDate() != null) {%>
                    <%=dateFormat.format(project.getEndingDate())%>
                    <%}%>
                </td>
                <td></td>
            </tr>
            <%}%>
            <c:if test="${not empty requestScope.projects}">
                <tr>
                    <td class="thead_project" colspan="5">Project</td>
                </tr>
            </c:if>
            <%for (Project project : (Set<Project>) request.getAttribute("projects")) {%>
            <% if (project.isFinished()) {%>
                <tr class="finished" data-projectId="<%=project.getId()%>">
            <%} else {%>
                <tr>
            <%}%>
                <td>
                    <a href="/project?id=<%=project.getId()%>"><%=project.getId()%>
                    </a>
                </td>
                <td><%=project.getName()%>
                </td>
                <td>
                    <% if (project.getStartingDate() != null) {%>
                    <%=dateFormat.format(project.getStartingDate())%>
                    <%}%>
                </td>
                <td>
                    <% if (project.getEndingDate() != null) {%>
                    <%=dateFormat.format(project.getEndingDate())%>
                    <%}%>
                </td>
                <td>
                    <% if (project.isFinished()) {%>
                    <div class="deleteProjectBtn">
                        <span onclick="deleteProject(<%=project.getId()%>)"></span>
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
