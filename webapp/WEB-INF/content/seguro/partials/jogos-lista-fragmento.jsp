<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="opendev" uri="/opendev-tags" %>

<c:forEach var="jogo" items="${jogos}" varStatus="loop">
    <fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
    <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
    
    <c:if test="${empty dataJogo or dataJogo ne jogo.data}">
        <div id="jogos_${dataJogoFormatada}_portlet" class="portlet collapsible-portlet">
            <div class="title collapsible-portlet__header">
                <img alt="Alternar exibição" src="${base}/img/arrow_down.png"
                    class="collapse-toggle icon-inline-top icon-button"
                    data-target="jogos_${dataJogoFormatada}_portlet" />
                <fmt:message key="matchs.day">
                    <fmt:param value="${dataJogoFormatada}" />
                </fmt:message>
            </div>
            <div class="content collapsible-portlet__content"
                id="jogos_${dataJogoFormatada}_portlet_content">
                <div class="table-responsive">
                    <table class="table conteudo match-table">
                        <thead>
                            <tr>
                                <th scope="col"><fmt:message key="match.hour" /></th>
                                <th scope="col"><fmt:message key="match.where" /></th>
                                <th scope="col"><fmt:message key="match.group" /></th>
                                <th scope="colgroup" colspan="3"><fmt:message key="match.teams" /></th>
                                <c:if test="${telaPalpites}">
                                    <th scope="col"><fmt:message key="match.tip.mine" /></th>
                                    <th scope="col"></th>
                                </c:if>
                            </tr>
                        </thead>
    </c:if>

    <c:set var="rowIndex" value="${loop.index}" />
    <c:choose>
        <c:when test="${jogo.equipe1.nomePais eq 'Brasil' or jogo.equipe2.nomePais eq 'Brasil'}">
            <c:set var="rowStyleClass" value="brasil" />
        </c:when>
        <c:when test="${rowIndex mod 2 eq 0}">
            <c:set var="rowStyleClass" value="impar" />
        </c:when>
        <c:otherwise>
            <c:set var="rowStyleClass" value="par" />
        </c:otherwise>
    </c:choose>

    <c:set var="palpiteUsuario" value="${palpitesUsuario[jogo.id]}" />
    <c:set var="podeRegistrarPalpite" value="false" />
    <sec:authorize access="hasAnyRole('USER', 'ADMIN')">
        <c:set var="podeRegistrarPalpite" value="true" />
    </sec:authorize>
    <c:set var="palpitePermitido" value="${podeRegistrarPalpite and jogo.podeDarPalpite}" />
    <c:set var="palpiteStatus" value="${not empty palpiteUsuario ? 'registered' : (palpitePermitido ? 'pending' : 'locked')}" />
    <fmt:message key="match.tip.status.${palpiteStatus}" var="palpiteStatusLabel" />
    <fmt:message key="match.tip.none" var="palpiteSemRegistro" />

    <tbody>
        <%@include file="/WEB-INF/content/seguro/partials/match-row.jspf" %>
    </tbody>

    <c:set var="dataJogo" value="${jogo.data}" />
    <c:if test="${loop.last}">
                    </table>
                </div>
            </div>
        </div>
        
        <%-- Container para a próxima carga --%>
        <div id="load-more-container" class="load-more-section">
            <fmt:formatDate var="ultimaData" value="${jogo.data}" pattern="dd/MM/yyyy" />
            <button class="button button--secondary button--full-width"
                    hx-get="${base}/seguro/palpitesMaisJogosPartial.action?dataInicial=${ultimaData}"
                    hx-target="#load-more-container"
                    hx-swap="outerHTML"
                    hx-indicator="#loading-more-indicator">
                <img id="loading-more-indicator" src="${base}/img/loading.gif" class="htmx-indicator icon-inline" alt="" />
                Carregar Próxima Data
            </button>
        </div>
    </c:if>
</c:forEach>
