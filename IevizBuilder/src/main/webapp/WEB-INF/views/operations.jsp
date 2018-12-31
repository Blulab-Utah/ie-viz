<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@include file="/WEB-INF/includes/header.jsp" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Information Extraction and Visualization Toolkit</title>
</head>
<body>
User: <strong>${user}</strong>
<a href="<c:url value="/logout" />">Logout</a>

<br/> <br/>
<b>IEVIZ Annotations Processing</b><br/><br/>

<form method="post" action="<c:url value="/processAnnotations"/>" enctype="multipart/form-data">
    Upload Ontology Files: <input type="file" name="ontologies" size="600" multiple="multiple"><br/>
    <table>
        <tr>
            <td>Select Annotation Tool :</td>
            <td>
                <form:select path="nlpAppList" name="appName">
                    <form:option value="NONE" label="--- Select ---"/>
                    <form:options items="${nlpAppList}"/>
                </form:select>
                <input type="hidden" value="${nlpAppList}" name="cc">
                <form:errors path="nlpAppList" cssClass="error"/>
            </td>
        </tr>
    </table>
    <br/>
    <table>
        <tr>
            <td>Select Corpus :</td>
            <td>
                <form:select path="corpusList" name="corpusName" multiple="true">
                    <form:option value="NONE" label="--- Select ---"/>
                    <form:options items="${corpusList}"/>
                </form:select>
                <input type="hidden" value="${corpusList}" name="cc">
                <form:errors path="corpusList" cssClass="error"/>
            </td>
        </tr>
    </table>
    <br/>
    <input type="submit" value="View Annotations">
</form>

<%--<form method="POST" action="<c:url value="/processAnnotations"/>" enctype="multipart/form-data">--%>
    <%--Upload Ontology Files: <input type="file" name="ontFile" size="600" multiple="multiple"><br/><br/><br/>--%>
    <%--Upload Corpus Files: <input type="file" name="ip" size="600" multiple="multiple"><br/><br/><br/>--%>
    <%--<input type="submit" value="Upload">--%>
<%--</form>--%>
<form method="GET" action="<c:url value="/admin"/>" enctype="text/plain">
    <input type="submit" value="Home">
</form>

</body>
</html>
<%@include file="/WEB-INF/includes/footer.jsp" %>