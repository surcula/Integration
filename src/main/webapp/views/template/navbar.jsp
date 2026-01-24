<%--
  Created by IntelliJ IDEA.
  User: david
  Date: 6/22/2025
  Time: 6:40 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Navigation-->
<nav class="navbar navbar-expand-lg bg-secondary text-uppercase sticky-top" id="mainNav">
    <div class="container">
        <a class="navbar-brand" href="#page-top">FSSS</a>
        <button class="navbar-toggler text-uppercase font-weight-bold bg-primary text-white rounded" type="button"
                data-bs-toggle="collapse" data-bs-target="#navbarResponsive" aria-controls="navbarResponsive"
                aria-expanded="false" aria-label="Toggle navigation">
            Menu
            <i class="fas fa-bars"></i>
        </button>
        <div class="collapse navbar-collapse" id="navbarResponsive">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item mx-0 mx-lg-1"><a class="nav-link py-3 px-0 px-lg-3 rounded"
                                                     href="${pageContext.request.contextPath}/home">Accueil</a>
                </li>
                <li class="nav-item mx-0 mx-lg-1"><a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/reservation">Réservation</a>
                </li>
                <li class="nav-item mx-0 mx-lg-1"><a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/event">Evènements</a>
                </li>
                <li class="nav-item mx-0 mx-lg-1"><a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/subscription">Abonnements</a>
                </li>
                <li class="nav-item mx-0 mx-lg-1"><a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/infos">Infos pratiques</a>
                </li>
                <li class="nav-item mx-0 mx-lg-1"><a class="nav-link py-3 px-0 px-lg-3 rounded" href="#">Sponsors</a>
                </li>

                <!-- Lien admin -->

                <c:if test="${sessionScope.role == 'ADMIN'
             or sessionScope.role == 'BARMAN'
             or sessionScope.role == 'SECRETARY'}">
                    <li class="nav-item">
                        <a class="nav-link fw-bold text-warning" href="${pageContext.request.contextPath}/infos">
                            ADMIN
                        </a>
                    </li>
                </c:if>

                <c:if test="${sessionScope.role == 'admin' || sessionScope.role == 'secretaire' || sessionScope.role == 'barman'}">
                    <li class="nav-item">
                        <a class="nav-link fw-bold text-warning" href="${pageContext.request.contextPath}/users">Gestion utilisateurs</a>
                    </li>
                </c:if>

                <!-- Mon profil  -->
                <c:if test="${not empty sessionScope.userId}">
                    <li class="nav-item">
                        <a class="nav-link fw-bold text-warning" href="${pageContext.request.contextPath}/profile">Mon profil</a>
                    </li>
                </c:if>

                <!-- Badge rôle -->
                <c:if test="${not empty sessionScope.role}">
                    <li class="nav-item mx-1">
                        <span class="badge bg-light text-dark text-uppercase">${sessionScope.role}</span>
                    </li>
                </c:if>

                <!-- Connexion / Déconnexion -->
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <li class="nav-item mx-0 mx-lg-1">
                            <a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/login?action=logout">Se déconnecter</a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item mx-0 mx-lg-1">
                            <a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/login">Se connecter</a>
                        </li>
                        <li class="nav-item mx-0 mx-lg-1">
                            <a class="nav-link py-3 px-0 px-lg-3 rounded" href="${pageContext.request.contextPath}/user">S’inscrire</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
