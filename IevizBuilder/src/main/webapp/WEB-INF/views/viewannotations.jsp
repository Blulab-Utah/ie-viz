<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@include file="/WEB-INF/includes/header.jsp" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/plain; charset=ISO-8859-1">
    <title>View Annotations</title>
</head>
<body>
User: <strong>${user}</strong>
<form method="GET" action="<c:url value="/getOperations"/>" enctype="multipart/form-data">
    <input type="submit" value="operations">
</form>
<a href="<c:url value="/logout" />">Logout</a>

<br/> <b>View Annotations</b><br/>

${content}

<br/>

</body>
</html>
<%@include file="/WEB-INF/includes/footer.jsp" %>