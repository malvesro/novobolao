<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="jogo" value="${jogoSelecionado}" />
<c:set var="palpiteUsuario" value="${palpiteSelecionado}" />
<c:set var="telaPalpites" value="true" />
<fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
<c:set var="palpiteStatusKey">match.tip.status.${palpiteStatus}</c:set>
<fmt:message key="${palpiteStatusKey}" var="palpiteStatusLabel" />
<fmt:message key="match.tip.none" var="palpiteSemRegistro" />

<%@include file="/WEB-INF/content/seguro/partials/match-row.jspf" %>
