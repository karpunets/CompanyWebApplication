<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Task#${requestScope.task.id}</title>
    <%@include file="/parts/head.jspf" %>
    <%@include file="/parts/jqueryui.jspf" %>
    <link href="/css/task.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/autosize.js/1.18.4/jquery.autosize.min.js"></script>
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
            var currentEmployee = {
                id: ${requestScope.grant.id},
                ns: "${requestScope.grant.name} ${requestScope.grant.surname}",
                photoUrl: "${requestScope.grantPhotoUrl}"
            }

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
            if (${requestScope.task.isFinished()}) {
                progressLabel.text("Complete!");
                progressbar.find(".ui-progressbar-value").css({
                    "background": '#6ac196'
                });
            }

            <c:if test="${requestScope.canEdit}">
            $("#dialogEditTask").find(".taskDatepickerStart").datepicker({
                minDate: 0
            });
            $("#dialogEditTask").dialog(dialogConfig);
            $("#dialogEditTask").find(".taskSpinner").spinner({
                min: 1,
                step: 1,
            }).val(10);
            $(document).on('click', '.editTaskBtn', function (event) {
                $("#dialogEditTask").dialog("open");
            });

            $(document).on('submit', '#ajax_form_edit_task', function (event) {
                event.preventDefault();
                var $form = $(this);
                var posting = $.post("/update/task", {
                    taskId: ${requestScope.task.id},
                    description: $form.find(".taskDescription").val(),
                    startingDate: $form.find("input[name='startingDate']").val(),
                    estimate: $form.find(".taskSpinner").spinner("value")
                });
                posting.done(function (data) {
                    if (data) {
                        $("#dialogContent").html(data);
                        $("#dialog").dialog("open");
                    } else {
                        window.location.reload();
                    }
                });

            });
            </c:if>

            $(document).on('click', '.finishTaskBtn', function (event) {
                var posting = $.post("/update/task/finish", {
                    taskId: ${requestScope.task.id}
                });
                posting.done(function (data) {
                    window.location.reload();
                });
            });

            $(document).on('submit', '#ajax_form_finish_task', function (event) {
                event.preventDefault();
                var posting = $.post("/update/finish/task", {
                    taskId: ${requestScope.task.id}
                });
                posting.done(function (data) {
                    window.location.reload();
                });
            });
            if ($('.dialog_content ul li:last').is()) {
                $(".dialog_content ul").scrollTop($('.dialog_content ul li:last').offset().top);
            }

            $(document).on('submit', '#ajax_form_sent_message', function (event) {
                event.preventDefault();
                var $form = $(this);
                var text = $form.find("textarea[name='message_text']").val();

                if (!text) {
                    $("#dialogContent").html("You must type something");
                    $("#dialog").dialog("open");
                } else {
                    var posting = $.post("/create/message", {
                        taskId: ${requestScope.task.id},
                        text: text
                    });
                    posting.done(function (data) {
                        addMessage(currentEmployee.id, currentEmployee.ns, currentEmployee.photoUrl, text);
                        $form.find("textarea[name='message_text']").val("");
                        if ($('.dialog_content ul li:last').is()) {
                            $(".dialog_content ul").scrollTop($('.dialog_content ul li:last').offset().top);
                        }
                    });
                }
            });

            $("#dialog").dialog(dialogConfig);

            updater.init();
            updater.start();

            $('.sentMessage textarea').autosize()
        });

        var updater = new Updater();

        function Updater() {
            this.params = {
                period: 3000,
                url: 'get/newMessage'
            };

            this.interval = null;
            this.lengthMessage = 0;

            <c:if test="${not empty requestScope.task.dialog.messages}">
            this.lengthMessage = ${requestScope.task.dialog.messages.size()};
            </c:if>

            this.incMessage = function () {
                this.lengthMessage++;
            };

            this.init = function (params) {
                var me = this;
                this.params = $.extend(this.params, params);

                $(window).blur(function () {
                    me.pause()
                });
                $(window).focus(function () {
                    me.resume()
                });
            };

            this.start = function () {
                var me = this;
                this.interval = setInterval(function () {
                    me.doUpdate()
                }, this.params.period);
            };

            this.resume = function () {
                if (this.interval != null) return;
                this.start();
            }

            this.pause = function () {
                clearInterval(this.interval);
                this.interval = null;
            }

            this.doUpdate = function () {
                var posting = $.get("/get/messages", {
                    dialogId: ${requestScope.task.dialog.id},
                    lengthMessage: this.lengthMessage
                });
                posting.done(function (data) {
                    if (data) {
                        $.each(jQuery.parseJSON(data), function (i, item) {
                            addMessage(item.employeeId, item.employeeName, item.photoUrl, item.text);
                        });
                    }
                });
            };


        }

        function addMessage(authorId, author, authorPhotoUrl, text) {
            var spanAuthor = $("<span/>").html(author);
            var divtext = $("<div class='text'/>").html(text);
            var aAuthor = $("<a class='author' href='/profile?id=" + authorId + "' target='_blank'/>")
                .append("<img width='16' height='16' src='" + authorPhotoUrl + "' />")
                .append(spanAuthor);
            $("<li/>").append(aAuthor).append(divtext).appendTo("#messages");
            updater.incMessage();

        }


    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <div class="left_content">
        <h1 class="task_name"><span>Task #</span>${requestScope.task.id}</h1>
        <c:if test="${requestScope.canEdit}">
            <div id="dialogEditTask" title="Edit task">
                <div class="dialogContentEditTask">
                    <form method="post" id="ajax_form_edit_task" action="">
                        <span>Description:</span>
                        <textarea class="taskDescription">${requestScope.task.description}</textarea>
                        <span>The date of the starting</span>
                        <input type="text" name="startingDate" class="taskDatepickerStart"
                               value="${requestScope.startingDate}"/>
                        <span>Estimate</span>
                        <div>
                            <input type="text" name="estimate" class="taskSpinner"
                                   value="${requestScope.task.estimate}"/>
                            <span class="hourLabel">hour(s)</span></div>
                        <div class="editTask">
                            <button type="submit">Edit</button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="editTaskBtn">
                <button type="button">EditTask</button>
            </div>
        </c:if>
        <div class="task_qualification"><span>Qualification: </span>${requestScope.task.qualification}</div>
        <div class="task_starting_data"><span>Date of the starting: </span>${requestScope.startingDate}</div>
        <div class="task_estimate"><span>Estimate: </span>${requestScope.task.estimate}</div>

        <div id="progressbar">
            <div class="progress-label">Waiting for the beginning task...</div>
        </div>

        <div class="task_dependencies">
            <span>Dependency(ies):</span>
            <ul>
                <c:forEach var="dependency" items="${requestScope.task.dependencies}">
                    <li><a href="/task?id=${dependency.id}">Task#${dependency.id}</a></li>
                </c:forEach>
            </ul>
        </div>

        <div class="task_subtasks">
            <span>Subtask(s):</span>
            <ul>
                <c:forEach var="subtask" items="${requestScope.task.subtasks}">
                    <li><a href="/task?id=${subtask.id}">Task#${subtask.id}</a></li>
                </c:forEach>
            </ul>
        </div>

        <div class="task_description">${requestScope.task.description}</div>

    </div>
    <div class="right_content">
        <div class="dialog">
            <div class="dialog_header"><span>Dialog #${requestScope.task.dialog.id}</span></div>
            <div class="dialog_content">
                <ul id="messages">
                    <c:forEach var="message" items="${requestScope.task.dialog.messages}">
                        <li>
                            <a class="author" href="/profile?id=${message.author.id}" target="_blank">
                                <img width='16' height='16' src="${message.author.photoUrl}"/>
                                <span>${message.author.name} ${message.author.surname}</span>
                            </a>
                            <div class="text">${message.text}</div>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div class="sentMessage">
                <form method="post" id="ajax_form_sent_message" action="">
                    <div>
                        <textarea name="message_text"></textarea>
                        <div class="sentMessageBtn">
                            <button id="buttonSentMessage">Sent</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="finishTaskBtn">
            <button type="button">Finish</button>
        </div>
    </div>

    <div id="dialog" title="Error">
        <div id="dialogContent"></div>
    </div>
</div>
</body>
</html>