<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
<head>
    <title>Edit project</title>
    <%@include file="/parts/head.jspf" %>
    <link href="/css/edit_project.css" rel="stylesheet">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
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

            $("#accordion").accordion({
                icons: {
                    header: "ui-icon-circle-arrow-e",
                    activeHeader: "ui-icon-circle-arrow-s"
                },
                heightStyle: "content",
                active: -1
            });
            $("#datepickerStart").datepicker();
            $("#datepickerEnd").datepicker();
            $("#dialog").dialog(dialogConfig);
            $("#dialogAddTask").dialog(dialogConfig);

            $(document).on('submit', '#ajax_form_update_project', function (event) {
                event.preventDefault();
                var $form = $(this);
                var posting = $.post("/update/project", {
                    id: ${requestScope.project.id},
                    name: $form.find("input[name='name']").val(),
                    description: $form.find("#projectDescription").val(),
                    startingDate: $form.find("input[name='startingDate']").val(),
                    endingDate: $form.find("input[name='endingDate']").val()
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
            $(document).on('submit', '#ajax_form_create_sprint', function (e) {
                event.preventDefault();
                var $form = $(this);
                var posting = $.post("/create/sprint", {
                    id: ${requestScope.project.id},
                    description: $form.find("#sprintDescription").val()
                });
                posting.done(function (data) {
                    var sprintId = parseInt(data);
                    if (isNaN(sprintId)) {
                        $("#dialogContent").html(data);
                        $("#dialog").dialog("open");
                    } else {
                        var description = $form.find("#sprintDescription").val();
                        var h3NewSprint = $("#h3NewSprint");
                        var divNewSprint = $("#divNewSprint");

                        $("#h3NewSprint").remove();
                        $("#divNewSprint").remove();

                        var h3Block = $("<h3>Sprint #" + sprintId + "</h3>")
                        var divBlock = $("<div class='contentSprint' data-internalid='" + sprintId + "' data-parent='sprint'/>")
                            .append("<p>" + description + "</p>")
                            .append("<ul class='sprintTasks'></ul>")
                            .append('<div class="addTaskBtn"><button class="buttonAddTask">Add task</button></div>')
                            .append('<div class="deleteSprintBtn"><button class="buttonDeleteSprint">Delete</button>');

                        $("#accordion").append(h3Block);
                        $("#accordion").append(divBlock);

                        $("#accordion").append(h3NewSprint);
                        $("#accordion").append(divNewSprint);
                        $form.find("#sprintDescription").val("");
                        $("#accordion").accordion("refresh");
                    }
                });
            });

            $(document).on('click', '.deleteSprintBtn', function (e) {
                var divSpring = $(this).parent(".contentSprint");
                var id = divSpring.attr('data-internalid');

                var posting = $.post("/delete/sprint", {
                    projectId: ${requestScope.project.id},
                    sprintId: id
                });
                posting.done(function (data) {
                    if (data) {
                        divSpring.prev().remove();
                        divSpring.remove();
                        $("#accordion").accordion("refresh");
                    }
                });
            })
            $(document).on('click', '.addTaskBtn', function (e) {
                e.preventDefault();
                var divSprint = $(this).parent(".contentSprint");
                addTask(divSprint.data("internalid"), divSprint.data("parent"), "Task for sprint#" + divSprint.data("internalid"))
                    .on("taskHasAdded", divSprint.find(".sprintTasks"), taskHasAdded);
            });
            $(document).on("click", ".contentSprint .btnDelete", function (e) {
                var divSprint = $(this).parent("li").parent(".sprintTasks").parent(".contentSprint");
                var aTask = $(this).siblings("a");
                deleteTask($(this).parent("li"), divSprint.data("internalid"), divSprint.data("parent"), aTask.data("internalid"));
            });

            function addTask(internalid, parent, title) {
                var newTaskDialog = $("#dialogAddTaskExample").children().clone().appendTo("#dialogs");
                var employees = [];
                var subtasks = [];
                var dependencies = [];
                var qualification;
                newTaskDialog.find("form").data("parent_id", internalid);
                newTaskDialog.find("form").data("parent", parent);
                newTaskDialog.attr("title", title);
                newTaskDialog.dialog(dialogConfig);
                newTaskDialog.dialog("open");
                newTaskDialog.on("dialogclose", function (event, ui) {
                    newTaskDialog.remove();
                });
                newTaskDialog.find(".taskDatepickerStart").datepicker({
                    minDate: 0
                });
                newTaskDialog.find(".taskSpinner").spinner({
                    min: 1,
                    step: 1,
                }).val(10);
                newTaskDialog.find(".taskQualification").selectmenu({
                    width: 400,
                    change: function (event, data) {
                        qualification = data.item.value;
                    }
                });
                newTaskDialog.find(".taskEmployeesInput").autocomplete({
                    source: function (request, response) {
                        $.get("/get/employees", {
                            query: request.term.toLowerCase()
                        }, function (data) {
                            var responseArray = [];
                            $.each(jQuery.parseJSON(data), function (i, item) {
                                var isElementSelect = false;
                                employees.forEach(function (element, index, array) {
                                    if (element == item.id) {
                                        isElementSelect = true;
                                    }
                                })
                                if (!isElementSelect) {
                                    responseArray.push({
                                        value: item,
                                        label: item.name + " " + item.surname
                                    });

                                }
                            });
                            response(responseArray);
                        });
                    },
                    minLength: 2,
                    select: function (event, ui) {
                        var aEmployee = $("<a href='/profile?id=" + ui.item.value.id + "' target='_blank' data-internalid='" + ui.item.value.id + "'/>")
                            .append("<img width='16' height='16' src='" + ui.item.value.photoUrl + "' />")
                            .append("<span class='sprintTasks'>" + ui.item.label + "</span>");
                        $("<li/>")
                            .append(aEmployee)
                            .append("<span class='btnDelete'></span>")
                            .appendTo(newTaskDialog.find(".taskEmployees"));
                        employees.push(ui.item.value.id);
                        $(this).val('');
                        return false;
                    }
                });
                newTaskDialog.find(".taskDependenciesInput").autocomplete({
                    source: function (request, response) {
                        $.get("/get/tasks", {
                            query: request.term
                        }, function (data) {
                            var responseArray = [];
                            $.each(jQuery.parseJSON(data), function (i, item) {
                                var isElementSelect = false;
                                dependencies.forEach(function (element, index, array) {
                                    if (element == item) {
                                        isElementSelect = true;
                                    }
                                });
                                if (!isElementSelect) {
                                    responseArray.push({
                                        value: item,
                                        label: "Task #" + item
                                    });

                                }
                            });
                            response(responseArray);
                        });
                    },
                    minLength: 1,
                    select: function (event, ui) {
                        var a = $("<a href='/task?id=" + ui.item.value + "' data-internalid='" + ui.item.value + "' >Task #" + ui.item.value + "</a>")
                        var li = $("<li/>").append(a).append("<span class='btnDelete'></span>").appendTo(newTaskDialog.find(".taskDependencies"));
                        dependencies.push(ui.item.value);
                        $(this).val('');
                        return false;
                    }
                });
                newTaskDialog.on('click', '.addSubTask', function (event) {
                    event.preventDefault();
                    addTask(0, "task", "Subtask for new task").on("taskHasAdded", newTaskDialog.find(".subtasks"),
                        function (event, taskId) {
                            subtasks.push(taskId);
                            taskHasAdded(event, taskId);
                        });
                });
                newTaskDialog.on('click', '.btnDelete', function (event) {
                    var id = $(this).siblings("a").data("internalid");
                    switch ($(this).parent("li").parent("ul").data("parent")) {
                        case "employees":
                            employees.splice(employees.indexOf(id), 1);
                            $(this).parent("li").remove();
                            break;
                        case "subtasks":
                            subtasks.splice(subtasks.indexOf(id), 1);
                            deleteTask($(this).parent("li"), 0, "subtask", id)
                            break;
                        case "dependencies":
                            dependencies.splice(dependencies.indexOf(id), 1);
                            $(this).parent("li").remove();
                            break;
                    }
                });
                newTaskDialog.on('submit', '.ajax_form_add_task', function (event) {
                    event.preventDefault();
                    var $form = $(this);
//                    $form.cursor("wait");
                    $form.css( 'cursor', 'wait' );
                    var posting = $.post("/create/task", {
                        projectId: ${requestScope.project.id},
                        parentId: $form.data("parent_id"),
                        parent: $form.data("parent"),
                        description: $form.find(".taskDescription").val(),
                        startingDate: $form.find("input[name='startingDate']").val(),
                        estimate: $form.find(".taskSpinner").spinner("value"),
                        qualification: qualification,
                        employees: employees.toString(),
                        subtasks: subtasks.toString(),
                        dependencies: dependencies.toString()
                    });
                    posting.done(function (data) {
//                        $form.cursor("default");
                        $form.css( 'cursor', 'default' );
                        var taskId = parseInt(data);
                        if (isNaN(taskId)) {
                            $("#dialogContent").html(data);
                            $("#dialog").dialog("open");
                        } else {
                            newTaskDialog.find(".ajax_form_add_task").trigger("taskHasAdded", [taskId]);
                            newTaskDialog.trigger("dialogclose");
                        }
                    });
                });
                return newTaskDialog.find(".ajax_form_add_task");
            }

            function taskHasAdded(event, taskId) {
                var a = $("<a href='/task?id=" + taskId + "' data-internalid='" + taskId + "' >Task #" + taskId + "</a>")
                var li = $("<li/>").append(a).append("<span class='btnDelete'></span>").appendTo(event.data);
//                    var win = window.open("/task?id=" + taskId, "_blank");
//                    if (win) {
//                        win.focus();
//                    } else {
//                        alert("Please allow popups for this website");
//                    }
            }

            function deleteTask(element, parentId, parentType, taskId) {
                $.post("/delete/task", {
                    projectId: ${requestScope.project.id},
                    parentId: parentId,
                    parentType: parentType,
                    taskId: taskId
                }, function (data) {
                    element.remove();
                });
            }

        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <h1><a href="/project?id=${requestScope.project.id}">Project # ${requestScope.project.id}</a></h1>
    <form method="post" id="ajax_form_update_project" action="">
        <span>Name:</span>
        <input type="text" name="name" value="${requestScope.project.name}"/>
        <span>Description:</span>
        <textarea id="projectDescription">${requestScope.project.description}</textarea>
        <span>The date of the starting</span>
        <input type="text" name="startingDate" id="datepickerStart" value="${requestScope.startingDate}"/>
        <span>The date of the ending</span>
        <input type="text" name="endingDate" id="datepickerEnd" value="${requestScope.endingDate}"/>
        <div class="updateProjectBtn">
            <button type="submit" id="buttonUpdateProject">Update</button>
        </div>
    </form>
    <div id="accordion">
        <c:forEach var="sprint" items="${requestScope.project.sprints}">
            <h3>Sprint #${sprint.id}</h3>
            <div class="contentSprint" data-internalid="${sprint.id}" data-parent="sprint">
                <p>${sprint.description}</p>
                <ul class="sprintTasks">
                    <c:forEach var="task" items="${sprint.tasks}">
                        <li><a href="/task?id=${task.id}" data-internalid="${task.id}" >Task #${task.id}</a><span class="btnDelete"></span></li>
                    </c:forEach>
                </ul>
                <div class="addTaskBtn">
                    <button type="button" class="buttonAddTask">Add task</button>
                </div>
                <div class="deleteSprintBtn">
                    <button type="button" class="buttonDeleteSprint">Delete sprint</button>
                </div>
            </div>
        </c:forEach>
        <h3 id="h3NewSprint">Add new sprint</h3>
        <div id="divNewSprint">
            <form method="post" id="ajax_form_create_sprint" action="">
                <span>Description</span>
                <textarea name="sprintDescription" id="sprintDescription"></textarea>
                <div class="addSprintBtn">
                    <button type="submit" id="addSprintButton">Create</button>
                </div>
            </form>
        </div>
    </div>
    <div id="dialogs">
        <%@include file="/parts/add_new_task.jspf" %>
    </div>
</div>
<div id="dialog" title="Error">
    <div id="dialogContent"></div>
</div>
</body>
</html>