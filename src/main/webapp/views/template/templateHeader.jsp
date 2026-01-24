<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 02-06-25
  Time: 12:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>FSSS</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/Css.css">
    <!-- Ceci a été testé afin de scinder les fichiers css par auteur mais l'idéal est par page pour ne pas avoir 20000 lignes-->
    <c:if test="${not empty pageCss}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/${pageCss}">
    </c:if>
    <script src="${pageContext.request.contextPath}/javascript/Js.js"></script>
    <!-- Font Awesome icons (free version)-->
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <!-- Google fonts-->
    <link href="https://fonts.googleapis.com/css?family=Montserrat:400,700" rel="stylesheet" type="text/css"/>
    <link href="https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic" rel="stylesheet"
          type="text/css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

</head>
<body id="page-top">
<!-- Navbar -->
<jsp:include page="/views/template/navbar.jsp"/>


