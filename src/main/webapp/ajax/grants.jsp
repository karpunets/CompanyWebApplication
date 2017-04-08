<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<table>
    <thead>
    <tr>
        <th>Id</th>
        <th>Name</th>
        <th>Surname</th>
        <th>Email</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${requestScope.grantList}">
        <a href="profile?id=${item.id}">
            <tr>
                <td><a href="/profile?id=${item.id}">${item.id}</a></td>
                <td>${item.name}</td>
                <td>${item.surname}</td>
                <td><a href="mailto:${item.email}">${item.email}</a></td>
            </tr>
        </a>
    </c:forEach>
    </tbody>
</table>
