<%@ page pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                    <%@ taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>

                        <c:set var="dataExibidaAnterior" value="" />
                        <c:forEach var="jogo" items="${jogos}" varStatus="loop">
                            <fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
                            <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />

                            <%-- Logica de Agrupamento por Data (Fix UX 2026): 1. Se a data mudou e não é o primeiro
                                item, fecha o bloco (portlet/tabela) anterior. 2. Se a data mudou ou é o primeiro item,
                                abre um novo bloco de portlet para o dia. Utiliza string formatada para evitar falha de
                                comparação por componentes de tempo. --%>
                                <c:if
                                    test="${not empty dataExibidaAnterior and dataExibidaAnterior ne dataJogoFormatada}">
                                    </table>
                                    </div>
                                    </div>
                                    </div>
                                    <span class="spacer spacer-sm"></span>
                                </c:if>

                                <c:if test="${empty dataExibidaAnterior or dataExibidaAnterior ne dataJogoFormatada}">
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
                                <c:set var="autorizacaoPalpite" value="${autorizacoesPalpitePorJogo[jogo.id]}" />
                                <c:set var="palpitePermitido" value="${not empty autorizacaoPalpite and autorizacaoPalpite.permitido}" />
                                <c:set var="palpiteStatus" value="${not empty autorizacaoPalpite ? autorizacaoPalpite.status.key : (not empty palpiteUsuario ? 'registered' : (jogo.podeDarPalpite ? 'pending' : 'locked'))}" />
                                <c:set var="palpiteBloqueioMotivo" value="${not empty autorizacaoPalpite and not empty autorizacaoPalpite.reason and not empty autorizacaoPalpite.reason.key ? autorizacaoPalpite.reason.key : ''}" />
                                <%--
                                  Fallback defensivo:
                                  Em casos raros de divergência entre contexto de segurança da action e da view
                                  (ex.: ROLE_USER reconhecida na request, mas auth nula no backend parcial),
                                  evitamos bloquear toda a data como "Edição encerrada".
                                  Regra: só libera por fallback para ROLE_USER sem ROLE_ADMIN e respeitando janela temporal.
                                --%>
                                <c:set var="usuarioRoleUserRequest" value="${pageContext.request.isUserInRole('ROLE_USER')}" />
                                <c:set var="usuarioRoleAdminRequest" value="${pageContext.request.isUserInRole('ROLE_ADMIN')}" />
                                <c:if test="${(empty autorizacaoPalpite or palpiteBloqueioMotivo eq 'roleMissing') and usuarioRoleUserRequest and not usuarioRoleAdminRequest}">
                                    <c:set var="palpitePermitido" value="${jogo.podeDarPalpite}" />
                                    <c:set var="palpiteStatus" value="${not empty palpiteUsuario ? 'registered' : (palpitePermitido ? 'pending' : 'locked')}" />
                                    <c:set var="palpiteBloqueioMotivo" value="${palpitePermitido ? '' : 'timeWindow'}" />
                                </c:if>
                                <fmt:message key="match.tip.status.${palpiteStatus}" var="palpiteStatusLabel" />
                                <fmt:message key="match.tip.none" var="palpiteSemRegistro" />

                                <tbody>
                                    <c:choose>
                                        <c:when test="${adminResultadoView}">
                                            <%@include file="/WEB-INF/content/admin/partials/admin-match-row.jsp" %>
                                        </c:when>
                                        <c:otherwise>
                                            <%@include file="/WEB-INF/content/seguro/partials/match-row.jspf" %>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>

                                <%-- Atualiza a data de referência para a próxima iteração --%>
                                    <c:set var="dataExibidaAnterior" value="${dataJogoFormatada}" />

                                    <c:if test="${loop.last}">
                                        </table>
                                        </div>
                                        </div>
                                        </div>

                                        <%-- A paginação incremental ("Carregar Próxima Data") --%>
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
