<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--<%@include file="/WEB-INF/includes/header.jsp" %>--%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/plain" charset=utf-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>Corpus Management</title>

<body>
Dear <strong>${user}</strong>, Welcome to Corpus Management page.
<a href="<c:url value="/logout" />">Logout</a>

<br/> <br/>
<b>Create New Corpus</b><br/><br/>
<form method="post" action="<c:url value="/createCorpus"/>" enctype="multipart/form-data">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    Enter Corpus Name: <input type="text" name="corpusName"><br/><br/><br/>
    Upload Corpus Files: <input type="file" name="corpusFiles" size="600" multiple="multiple"><br/><br/><br/>
    <input type="submit" value="Upload">
</form>

<br/>
<br/>
<b>Upload Files to Existing Corpora</b>
<%--<form method="post" items="${countryList}" multiple="true" />--%>
<form method="get" action="<c:url value="/manageCorpora"/>" enctype="multipart/form-data">
    <input type="submit" value="Refresh Corpora">
</form>
<br/>

<form method="post" action="<c:url value="/createCorpus"/>" enctype="multipart/form-data">
    <table>
        <tr>
            <td>Select Corpus :</td>
            <td>
                <form:select path="corpusList" name="corpusName">
                    <form:option value="NONE" label="--- Select ---"/>
                    <form:options items="${corpusList}"/>
                </form:select>
                <input type="hidden" value="${corpusList}" name="cc">
                <form:errors path="corpusList" cssClass="error"/>
            </td>
            <td><form:errors path="corpusList" cssClass="error"/></td>
            <td>Upload Corpus Files: <input type="file" name="corpusFiles" size="600" multiple="multiple"></td>
        </tr>
    </table>
    <br/>
    <input type="submit" value="UpdateCorpus">
</form>
</body>


<form method="GET" action="<c:url value="/admin"/>" enctype="multipart/form-data">
    <input type="submit" value="Home">
</form>
</html>
<%--<%@include file="/WEB-INF/includes/footer.jsp" %>--%>

