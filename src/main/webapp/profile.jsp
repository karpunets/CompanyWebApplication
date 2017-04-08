<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Profile</title>
    <%@include file="/parts/head.jspf" %>
    <%@include file="/parts/jqueryui.jspf" %>
    <link href="/css/profile.css" rel="stylesheet">
    <script type="text/javascript">

        $(document).ready(function () {
            var dialogInformation = {
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

            <c:if test="${requestScope.canEditInformation}">
            $("#ajax_form_edit_information").submit(function (event) {
                event.preventDefault();
                var $form = $(this);
                var posting = $.post("/update/grant/information", {
                    email: $form.find("input[name='email']").val(),
                    number: $form.find("input[name='number']").val(),
                    oldPassword: $form.find("input[name='oldPassword']").val(),
                    newPassword: $form.find("input[name='newPassword']").val(),
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
            $("#dialogEditInformation").dialog(dialogInformation);
            $("#editInformation").click(function () {
                $("#dialogEditInformation").dialog("open");
            });
            $("#dialogEditAvatar").dialog(dialogInformation);
            $("#editAvatar").click(function () {
                $("#dialogEditAvatar").dialog("open");
            });
            </c:if>

            <c:if test="${requestScope.canEditAccountInformation}">
            var grant = "${requestScope.grantClass.toLowerCase()}",
                qualification = "${requestScope.qualification}";
            $("#dialogEditAccountInformation").dialog(dialogInformation);
            $("#editAccountInformation").click(function () {
                $("#dialogEditAccountInformation").dialog("open");
            });
            $("select[name='grant']").val(grant);
            $("select[name='grant']").selectmenu({
                change: function (event, data) {
                    grant = data.item.value;
                }
            });

            $("select[name='grant']").selectmenu();
            if (grant == "manager" || grant == "employee") {
                $("select[name='qualification']").val(qualification);
            }
            $("select[name='qualification']").selectmenu({
                change: function (event, data) {
                    qualification = data.item.value;
                }
            });
            $("#ajax_form_edit_account").submit(function (event) {
                event.preventDefault();
                var $form = $(this);
                var posting = $.post("/update/grant/account", {
                    id: ${requestScope.grant.id},
                    name: $form.find("input[name='name']").val(),
                    surname: $form.find("input[name='surname']").val(),
                    grant: grant,
                    qualification: qualification,
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
            $("#dialog").dialog(dialogInformation);
        });
    </script>
</head>
<body>
<%@include file="/parts/header.jspf" %>
<div class="content">
    <div class="avatar_content">
        <div class="avatar_img_wrapper">
            <img src="${requestScope.grant.photoUrl}">
        </div>
        <c:if test="${requestScope.canEditInformation}">
        <div class="avatar_btn_wrapper">
            <button id="editAvatar">Edit avatar</button>
        </div>
        <div class="upload_image" id="dialogEditAvatar" title="Edit avatar">
            <form method="post" id="ajax_form_edit_avatar" action="/upload/avatar" enctype="multipart/form-data">
                <input type="file" name="imageFile" accept="image/*">
                <button type="submit">Update</button>
            </form>
        </div>
        </c:if>
    </div>
    <div class="information_content_wrapper">
        <div class="information_content">
            <div class="information_name"><span>Grant:</span> ${requestScope.grantClass} #${requestScope.grant.id}</div>
            <c:if test="${not empty requestScope.qualification}">
                <div class="information_name"><span>Qualification:</span> ${requestScope.qualification}</div>
            </c:if>
            <div class="information_name"><span>Name:</span> ${requestScope.grant.name}</div>
            <div class="information_name"><span>Surname:</span> ${requestScope.grant.surname}</div>
            <div class="information_name"><span>Email:</span> <a
                    href="mailto:${requestScope.grant.email}">${requestScope.grant.email}</a></div>
            <div class="information_name"><span>Number:</span> <a
                    href="tel:${requestScope.grant.number}">${requestScope.grant.number}</a></div>
        </div>
        <c:if test="${requestScope.canEditInformation}">
            <div class="information_btn_edit">
                <button id="editInformation">Edit information</button>
            </div>
            <div id="dialogEditInformation" title="Edit information">
                <form class="edit_information" method="post" id="ajax_form_edit_information" action="">
                    <div class="information_input">
                        <label>Email:</label>
                        <input type="email" name="email" value="${requestScope.grant.email}"/>
                    </div>
                    <div class="information_input">
                        <label>Number:</label>
                        <input type="text" name="number" value="${requestScope.grant.number}"/>
                    </div>
                    <div class="information_input">
                        <label>Old password:</label>
                        <input type="password" name="oldPassword"/>
                    </div>
                    <div class="information_input">
                        <label>New password:</label>
                        <input type="password" name="newPassword"/>
                    </div>
                    <div class="information_input_btn">
                        <button type="submit">Update</button>
                    </div>
                </form>
            </div>
        </c:if>
        <c:if test="${requestScope.canEditAccountInformation}">
            <div class="information_btn_edit_account">
                <button id="editAccountInformation">Edit account information</button>
            </div>
            <div id="dialogEditAccountInformation" title="Edit account information">
                <form class="edit_information_account" method="post" id="ajax_form_edit_account" action="">
                    <div class="information_account_input">
                        <label>Name: </label>
                        <input type="text" name="name" value="${requestScope.grant.name}"/>
                    </div>
                    <div class="information_account_input">
                        <label>Surname: </label>
                        <input type="text" name="surname" value="${requestScope.grant.surname}"/>
                    </div>
                    <c:if test="${not empty requestScope.qualification}">
                        <div class="select" id="select">
                            <select name="grant">
                                <option disabled selected>Please pick grant</option>
                                <option value="manager">Manager</option>
                                <option value="employee">Employee</option>
                            </select>
                        </div>
                        <div class="select" id="selectQualification">
                            <select name="qualification">
                                <option disabled selected>Please pick qualification</option>
                                <c:forEach var="qualification" items="${requestScope.qualifications}">
                                    <option>${qualification}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>
                    <div class="information_input_account_btn">
                        <button type="submit">Update</button>
                    </div>
                </form>
            </div>
        </c:if>
    </div>
    <div id="dialog" title="Error">
        <div id="dialogContent"></div>
    </div>
</div>
</body>
</html>