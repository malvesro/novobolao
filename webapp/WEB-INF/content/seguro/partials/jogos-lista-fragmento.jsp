<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                <%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
                    <%@ taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>

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
                                                        <th scope="col">
                                                            <fmt:message key="match.hour" />
                                                        </th>
                                                        <th scope="col">
                                                            <fmt:message key="match.where" />
                                                        </th>
                                                        <th scope="col">
                                                            <fmt:message key="match.group" />
                                                        </th>
                                                        <th scope="colgroup" colspan="3">
                                                            <fmt:message key="match.teams" />
                                                        </th>
                                                        <c:if test="${telaPalpites}">
                                                            <th scope="col">
                                                                <fmt:message key="match.tip.mine" />
                                                            </th>
                                                            <th scope="col"></th>
                                                        </c:if>
                                                        <c:if test="${adminResultadoView}">
                                                            <%-- Coluna de ações/spinner da linha administrativa. --%>
                                                                <th scope="col"></th>
                                                        </c:if>
                                                    </tr>
                                                </thead>
                            </c:if>

                            <c:set var="rowIndex" value="${loop.index}" />
                            <c:choose>
                                <c:when
                                    test="${jogo.equipe1.nomePais eq 'Brasil' or jogo.equipe2.nomePais eq 'Brasil'}">
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
                            <c:set var="palpiteStatus"
                                value="${not empty palpiteUsuario ? 'registered' : (palpitePermitido ? 'pending' : 'locked')}" />
                            <fmt:message key="match.tip.status.${palpiteStatus}" var="palpiteStatusLabel" />
                            <fmt:message key="match.tip.none" var="palpiteSemRegistro" />

                            <tbody>
                                <%-- Contexto compartilhado: /seguro usa `match-row.jspf` (palpite) e
                                    /admin/jogos.action precisa da linha administrativa com inputs de placar. A flag
                                    `adminResultadoView` é definida pela AdminAction para eliminar ambiguidade de
                                    renderização. --%>
                                    <c:choose>
                                        <c:when test="${adminResultadoView}">
                                            <%@include file="/WEB-INF/content/admin/partials/admin-match-row.jsp" %>
                                        </c:when>
                                        <c:otherwise>
                                            <%@include file="/WEB-INF/content/seguro/partials/match-row.jspf" %>
                                        </c:otherwise>
                                    </c:choose>
                            </tbody>

                            <c:set var="dataJogo" value="${jogo.data}" />
                            <c:if test="${loop.last}">
                                </table>
                                </div>
                                </div>
                                </div>

                                <%-- A paginação incremental ("Carregar Próxima Data") agora disponível também no perfil
                                    administrativo para facilitar navegação em ambientes restritos. --%>
                                    <c:set var="mostrarBotaoMaisJogos"
                                        value="${telaPalpites or (adminResultadoView and not adminMostrandoTodos)}" />
                                    <c:if test="${mostrarBotaoMaisJogos}">
                                        <div id="load-more-container" class="load-more-section">
                                            <fmt:formatDate var="ultimaData" value="${jogo.data}"
                                                pattern="dd/MM/yyyy" />
                                            <c:choose>
                                                <c:when test="${adminResultadoView}">
                                                    <c:set var="hxGetUrl"
                                                        value="${base}/admin/jogosMaisJogosPartial.action?data=${ultimaData}" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="hxGetUrl"
                                                        value="${base}/seguro/palpitesMaisJogosPartial.action?dataInicial=${ultimaData}" />
                                                </c:otherwise>
                                            </c:choose>
                                            <button class="button button--secondary button--full-width"
                                                hx-get="${hxGetUrl}" hx-target="#load-more-container"
                                                hx-swap="outerHTML" hx-indicator="#loading-more-indicator">
                                                <img id="loading-more-indicator" src="${base}/img/loading.gif"
                                                    class="htmx-indicator icon-inline" alt="" />
                                                Carregar Próxima Data
                                            </button>
                                        </div>
                                    </c:if>
                            </c:if>
                        </c:forEach>