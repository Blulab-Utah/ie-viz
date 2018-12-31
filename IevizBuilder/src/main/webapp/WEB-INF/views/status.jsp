<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@include file="/WEB-INF/includes/header.jsp" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/plain; charset=ISO-8859-1">
    <title>Information Extraction and Visualization Toolkit</title>
</head>
<body>
User: <strong>${user}</strong>
<a href="<c:url value="/logout" />">Logout</a>

<br/> <br/>

Status: <strong>${status}</strong>

<br/>
<form method="GET" action="<c:url value="/admin"/>" enctype="multipart/form-data">
    <input type="submit" value="Home">
</form>
</body>
</html>
<%@include file="/WEB-INF/includes/footer.jsp" %>