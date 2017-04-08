<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>Project #${requestScope.project.id}</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/project.css" rel="stylesheet">
    <%@include file="/parts/jqueryui.jspf" %>
    <script type="text/javascript">
        $(document).ready(function () {
            var dialogConfig = {
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
            };

            var progressbar = $("#progressbar"),
                progressLabel = $(".progress-label");
            $(progressbar).progressbar({
                change: function () {
                    if (progressbar.progressbar("value")) {
                        progressLabel.text(progressbar.progressbar("value") + "%");
                    }
                }
            });
            if (${requestScope.progress}) {
                progressbar.progressbar("value", ${requestScope.progress});
                if (100 <= ${requestScope.progress}) {
                    progressbar.find(".ui-progressbar-value").css({
                        "background": '#bb7171'
                    });
                }
            } else {
                progressbar.progressbar("value", false);
            }
            if (${requestScope.project.isFinished()}) {
                progressLabel.text("Complete!");
                progressbar.find(".ui-progressbar-value").css({
                    "background": '#6ac196'
                });
            }


            $("#accordion").accordion({
                icons: {
                    header: "ui-icon-circle-arrow-e",
                    activeHeader: "ui-icon-circle-arrow-s"
                },
                heightStyle: "content"
            });

            <c:if test="${not empty requestScope.isProjectManager}">
            $(document).on('click', '.finishProjectBtn button', function (event) {
                var posting = $.post("/update/project/finish", {
                    projectId: ${requestScope.project.id}
                });
                posting.done(function (data) {
                    if (data) {
                        var ul = $("<ul/>")
                        ul.append($("<li/>").html("All tasks haven't finished yet"));
                        $.each(jQuery.parseJSON(data), function (i, item) {
                            var li = $("<li/>").append($("<a href='/task?id=" + item + "' target='_blank'/>").html("Task #" + item));
                            ul.append(li)
                        });
                        $("#dialogContent").append(ul);
                        $("#dialog").dialog("open");
                    } else {
                        window.location.reload();
                    }
                });
            });
            </c:if>

            $("#dialog").dialog(dialogConfig);
        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <h1>Project # ${requestScope.project.id}</h1>
    <h2 class="information_name">${requestScope.project.name}</h2>
    <c:if test="${not empty requestScope.isProjectManager}">
        <a href="/manage/project_edit?id=${requestScope.project.id}">
            <button type="button">Edit project</button>
        </a>
        <div class="finishProjectBtn">
            <button type="button">Finish</button>
        </div>
    </c:if>
    <div id="progressbar">
        <div class="progress-label">Waiting for the beginning project...</div>
    </div>
    <c:if test="${not empty requestScope.project.description}">
        <div class="information_description">${requestScope.project.description}</div>
    </c:if>
    <div id="accordion">
        <c:forEach var="sprint" items="${requestScope.project.sprints}">
            <h3>Sprint #${sprint.id}</h3>
            <div>
                <p>${sprint.description}</p>
                <c:forEach var="task" items="${sprint.tasks}">
                    <a href="/task?id=${task.id}">Task #${task.id}</a>
                </c:forEach>
            </div>
        </c:forEach>
    </div>
    <div id="dialog" title="Error">
        <div id="dialogContent"></div>
    </div>
</div>
</body>
</html>