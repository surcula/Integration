<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/views/template/templateHeader.jsp"/>

<%
    String content = (String) request.getAttribute("content");
    if (content == null) {
        content = request.getParameter("content");
    }

    if (content != null && !content.contains("src/main") && content.endsWith(".jsp")) {
%>
<jsp:include page="<%= content %>"/>
<%
} else {
%>
<p>Aucun contenu disponible.</p>
<%
    }
%>

<jsp:include page="/views/template/templateFooter.jsp"/>

