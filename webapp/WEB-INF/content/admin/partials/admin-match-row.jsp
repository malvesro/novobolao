<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:if test="${empty base}">
    <c:set var="base" value="${pageContext.request.contextPath eq '/' ? '' : pageContext.request.contextPath}" />
</c:if>
<fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
<fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
<fmt:message key="admin.result.locked.untilStart.short" var="adminResultLockedShortLabel">
    <fmt:param value="${horaJogoFormatada}" />
</fmt:message>
<fmt:message key="admin.result.locked.untilStart.aria" var="adminResultLockedAriaLabel">
    <fmt:param value="${horaJogoFormatada}" />
</fmt:message>
<fmt:message key="admin.match.delete.confirm" var="adminDeleteConfirmLabel" />
<fmt:message key="admin.match.delete.action" var="adminDeleteActionLabel" />
<fmt:message key="admin.match.details.show" var="adminDetailsShowLabel" />
<fmt:message key="admin.match.details.hide" var="adminDetailsHideLabel" />
<fmt:message key="admin.match.details.title" var="adminDetailsTitleLabel" />
<fmt:message key="admin.match.field.date" var="adminDateFieldLabel" />
<fmt:message key="match.where" var="matchWhereLabel" />
<fmt:message key="match.group" var="matchGroupLabel" />
<fmt:message key="match.teams" var="matchTeamsLabel" />
<c:set var="jogoPodeSerExcluido" value="${elegibilidadeExclusaoPorJogo[jogo.id] eq true}" />
<c:set var="equipe1NomeEsc" value="${fn:escapeXml(jogo.equipe1.nomePais)}" />
<c:set var="equipe2NomeEsc" value="${fn:escapeXml(jogo.equipe2.nomePais)}" />

<tr class="${jogo.rowStyleClass} match-row--admin-direct"
    id="jogoTr_${jogo.id}"
    data-jogo-id="${jogo.id}"
    data-jogo-date="${dataJogoFormatada}">
    <td class="match-table__time">
        <input type="hidden" id="jogoId_${jogo.id}" name="id" value="${jogo.id}" />
        <sec:authorize access="hasRole('ADMIN')">
            <div class="admin-inline-datetime">
            <select id="adminHora_${jogo.id}" name="hora" class="form-control-inline" 
                    hx-post="${base}/admin/salvarEdicaoEstrutural.action" 
                    hx-trigger="change" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #adminData_${jogo.id}, #adminHora_${jogo.id}, #adminLocal_${jogo.id}, #adminFase_${jogo.id}, #adminEquipe1_${jogo.id}, #adminEquipe2_${jogo.id}" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                <c:forTokens var="h" delims="," items="${initParam.horarios}">
                    <c:set var="hTrim" value="${fn:trim(h)}" />
                    <option value="${hTrim}" ${hTrim eq horaJogoFormatada ? 'selected' : ''}>${hTrim}</option>
                </c:forTokens>
            </select>
            </div>
        </sec:authorize>
        <sec:authorize access="!hasRole('ADMIN')">${dataJogoFormatada} ${horaJogoFormatada}</sec:authorize>
    </td>

    <td class="match-table__team match-table__team--home">
        <div class="team-cell text-right">
            <span title="${equipe1NomeEsc}"><c:out value="${jogo.equipe1.nomePais}" /></span>
            
            <c:choose>
                <c:when test="${not empty jogo.equipe1BandeiraUrl}">
                    <img class="flag-icon icon-inline" src="${base}${jogo.equipe1BandeiraUrl}" width="24" height="18" loading="lazy" alt="Bandeira de ${equipe1NomeEsc}" title="${equipe1NomeEsc}" />
                </c:when>
                <c:when test="${not empty jogo.equipe1EmojiBandeira}">
                    <span class="flag-icon icon-inline" role="img" aria-label="${equipe1NomeEsc}" title="${equipe1NomeEsc}">
                        <c:out value="${jogo.equipe1EmojiBandeira}" />
                    </span>
                </c:when>
                <c:otherwise><span class="flag-icon flag-icon--fallback icon-inline" title="${equipe1NomeEsc}"><c:out value="${jogo.equipe1SiglaPais}" /></span></c:otherwise>
            </c:choose>

            <sec:authorize access="hasRole('ADMIN')">
                <c:choose>
                    <c:when test="${jogo.podeAtualizarResultado}">
                        <input id="golsEquipe1_${jogo.id}" type="text" name="golsEquipe1" value="${jogo.golsEquipe1}" class="text score-input input-centered" maxlength="2" size="2"
                               hx-post="${base}/admin/atualizarResultadoJogo.action" hx-trigger="blur" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #golsEquipe1_${jogo.id}, #golsEquipe2_${jogo.id}" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML" />
                    </c:when>
                    <c:otherwise>
                        <span class="score-value">
                            <c:choose>
                                <c:when test="${not empty jogo.golsEquipe1 or jogo.golsEquipe1 eq 0}">
                                    ${jogo.golsEquipe1}
                                </c:when>
                                <c:otherwise></c:otherwise>
                            </c:choose>
                        </span>
                    </c:otherwise>
                </c:choose>
            </sec:authorize>
            <sec:authorize access="!hasRole('ADMIN')">
                <span class="score-value">
                    <c:choose>
                        <c:when test="${not empty jogo.golsEquipe1 or jogo.golsEquipe1 eq 0}">
                            ${jogo.golsEquipe1}
                        </c:when>
                        <c:otherwise></c:otherwise>
                    </c:choose>
                </span>
            </sec:authorize>
        </div>
    </td>

    <td class="match-table__separator">X</td>

    <td class="match-table__team match-table__team--away">
        <div class="team-cell text-left">
            <sec:authorize access="hasRole('ADMIN')">
                <c:choose>
                    <c:when test="${jogo.podeAtualizarResultado}">
                        <input id="golsEquipe2_${jogo.id}" type="text" name="golsEquipe2" value="${jogo.golsEquipe2}" class="text score-input input-centered" maxlength="2" size="2"
                               hx-post="${base}/admin/atualizarResultadoJogo.action" hx-trigger="blur" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #golsEquipe1_${jogo.id}, #golsEquipe2_${jogo.id}" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML" />
                    </c:when>
                    <c:otherwise>
                        <span class="score-value">
                            <c:choose>
                                <c:when test="${not empty jogo.golsEquipe2 or jogo.golsEquipe2 eq 0}">
                                    ${jogo.golsEquipe2}
                                </c:when>
                                <c:otherwise></c:otherwise>
                            </c:choose>
                        </span>
                    </c:otherwise>
                </c:choose>
            </sec:authorize>
            <sec:authorize access="!hasRole('ADMIN')">
                <span class="score-value">
                    <c:choose>
                        <c:when test="${not empty jogo.golsEquipe2 or jogo.golsEquipe2 eq 0}">
                            ${jogo.golsEquipe2}
                        </c:when>
                        <c:otherwise></c:otherwise>
                    </c:choose>
                </span>
            </sec:authorize>

            <c:choose>
                <c:when test="${not empty jogo.equipe2BandeiraUrl}">
                    <img class="flag-icon icon-inline" src="${base}${jogo.equipe2BandeiraUrl}" width="24" height="18" loading="lazy" alt="Bandeira de ${equipe2NomeEsc}" title="${equipe2NomeEsc}" />
                </c:when>
                <c:when test="${not empty jogo.equipe2EmojiBandeira}">
                    <span class="flag-icon icon-inline" role="img" aria-label="${equipe2NomeEsc}" title="${equipe2NomeEsc}">
                        <c:out value="${jogo.equipe2EmojiBandeira}" />
                    </span>
                </c:when>
                <c:otherwise><span class="flag-icon flag-icon--fallback icon-inline" title="${equipe2NomeEsc}"><c:out value="${jogo.equipe2SiglaPais}" /></span></c:otherwise>
            </c:choose>

            <span title="${equipe2NomeEsc}"><c:out value="${jogo.equipe2.nomePais}" /></span>
        </div>
    </td>

    <sec:authorize access="hasRole('ADMIN')">
        <td class="match-table__actions">
            <fmt:message key="general.retry" var="retryLabel" />
            <div class="admin-actions-inline">
                <span id="admin-save-status_${jogo.id}" class="admin-row-status ${not jogo.podeAtualizarResultado ? 'admin-row-status--locked' : ''}" role="status" aria-live="polite"><c:if test="${not jogo.podeAtualizarResultado}">${adminResultLockedShortLabel}</c:if></span>
                <button type="button"
                        class="button button-ghost admin-row-details-toggle"
                        data-js="toggle-admin-details"
                        data-target="#adminDetails_${jogo.id}"
                        data-label-open="${adminDetailsShowLabel}"
                        data-label-close="${adminDetailsHideLabel}"
                        aria-expanded="false"
                        aria-controls="adminDetails_${jogo.id}">
                    <c:out value="${adminDetailsShowLabel}" />
                </button>
                <c:if test="${jogoPodeSerExcluido}">
                    <button type="button"
                            class="button button-ghost admin-row-delete"
                            hx-post="${base}/admin/excluirJogo.action"
                            hx-target="closest tr"
                            hx-swap="delete"
                            hx-confirm="${adminDeleteConfirmLabel}"
                            hx-include="#csrfTokenField, #jogoDeleteId_${jogo.id}"
                            aria-label="${adminDeleteActionLabel}">
                        <c:out value="${adminDeleteActionLabel}" />
                    </button>
                    <input type="hidden" id="jogoDeleteId_${jogo.id}" name="id" value="${jogo.id}" />
                </c:if>
                <button type="button" class="button button-ghost admin-row-retry" data-js="retry-admin-save" hidden>${retryLabel}</button>
                <div class="htmx-indicator progress-spinner progress-spinner--mini"></div>
            </div>
            <div id="adminDetails_${jogo.id}" class="admin-structural-panel" data-js="admin-structural-panel" hidden>
                <span class="admin-structural-panel__title"><c:out value="${adminDetailsTitleLabel}" /></span>
                <div class="admin-structural-grid">
                    <label for="adminData_${jogo.id}" class="admin-structural-label"><c:out value="${adminDateFieldLabel}" /></label>
                    <select id="adminData_${jogo.id}" name="data" class="form-control-inline"
                            hx-post="${base}/admin/salvarEdicaoEstrutural.action"
                            hx-trigger="change" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #adminData_${jogo.id}, #adminHora_${jogo.id}, #adminLocal_${jogo.id}, #adminFase_${jogo.id}, #adminEquipe1_${jogo.id}, #adminEquipe2_${jogo.id}" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                        <c:forTokens var="d" delims="," items="${initParam.datas}">
                            <c:set var="dTrim" value="${fn:trim(d)}" />
                            <option value="${dTrim}" ${dTrim eq dataJogoFormatada ? 'selected' : ''}>${dTrim}</option>
                        </c:forTokens>
                    </select>
                    <label for="adminLocal_${jogo.id}" class="admin-structural-label"><c:out value="${matchWhereLabel}" /></label>
                    <select id="adminLocal_${jogo.id}" name="local" class="form-control-inline"
                            hx-post="${base}/admin/salvarEdicaoEstrutural.action"
                            hx-trigger="change" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #adminData_${jogo.id}, #adminHora_${jogo.id}, #adminLocal_${jogo.id}, #adminFase_${jogo.id}, #adminEquipe1_${jogo.id}, #adminEquipe2_${jogo.id}" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                        <c:forTokens var="loc" delims="," items="${initParam.locais}">
                            <c:set var="locTrim" value="${fn:trim(loc)}" />
                            <option value="${locTrim}" ${locTrim eq jogo.local ? 'selected' : ''}>${locTrim}</option>
                        </c:forTokens>
                    </select>
                    <label for="adminFase_${jogo.id}" class="admin-structural-label"><c:out value="${matchGroupLabel}" /></label>
                    <select id="adminFase_${jogo.id}" name="fase" class="form-control-inline"
                            hx-post="${base}/admin/salvarEdicaoEstrutural.action"
                            hx-trigger="change" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #adminData_${jogo.id}, #adminHora_${jogo.id}, #adminLocal_${jogo.id}, #adminFase_${jogo.id}, #adminEquipe1_${jogo.id}, #adminEquipe2_${jogo.id}" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                        <c:forTokens var="f" items="11,12,13,16,8,4,2,3,1" delims=",">
                            <option value="${f}" ${f eq jogo.fase ? 'selected' : ''}>
                                <fmt:message key="filter.fase.${f}" />
                            </option>
                        </c:forTokens>
                    </select>
                    <label for="adminEquipe1_${jogo.id}" class="admin-structural-label"><c:out value="${matchTeamsLabel}" /> 1</label>
                    <select id="adminEquipe1_${jogo.id}" name="equipe1Id" class="form-control-inline team-select-direct"
                            hx-post="${base}/admin/salvarEdicaoEstrutural.action"
                            hx-trigger="change" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #adminData_${jogo.id}, #adminHora_${jogo.id}, #adminLocal_${jogo.id}, #adminFase_${jogo.id}, #adminEquipe1_${jogo.id}, #adminEquipe2_${jogo.id}" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML">
                        <c:forEach var="equipeItem" items="${equipes}">
                            <option value="${equipeItem.id}" ${equipeItem.id eq jogo.equipe1.id ? 'selected' : ''}>
                                <c:out value="${equipeItem.nomePais}" />
                            </option>
                        </c:forEach>
                    </select>
                    <label for="adminEquipe2_${jogo.id}" class="admin-structural-label"><c:out value="${matchTeamsLabel}" /> 2</label>
                    <select id="adminEquipe2_${jogo.id}" name="equipe2Id" class="form-control-inline team-select-direct"
                            hx-post="${base}/admin/salvarEdicaoEstrutural.action"
                            hx-trigger="change" hx-include="#csrfTokenField, #jogoId_${jogo.id}, #adminData_${jogo.id}, #adminHora_${jogo.id}, #adminLocal_${jogo.id}, #adminFase_${jogo.id}, #adminEquipe1_${jogo.id}, #adminEquipe2_${jogo.id}" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML">
                        <c:forEach var="equipeItem" items="${equipes}">
                            <option value="${equipeItem.id}" ${equipeItem.id eq jogo.equipe2.id ? 'selected' : ''}>
                                <c:out value="${equipeItem.nomePais}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </td>
    </sec:authorize>
</tr>
