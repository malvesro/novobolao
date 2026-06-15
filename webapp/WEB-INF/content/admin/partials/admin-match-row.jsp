<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:if test="${empty base}">
    <c:set var="base" value="${pageContext.request.contextPath eq '/' ? '' : pageContext.request.contextPath}" />
</c:if>
<fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
<fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />

<tr class="${jogo.rowStyleClass} match-row--admin-direct" id="jogoTr_${jogo.id}" data-jogo-id="${jogo.id}">
    <td class="match-table__time">
        <input type="hidden" name="id" value="${jogo.id}" />
        <sec:authorize access="hasRole('ADMIN')">
            <div class="admin-inline-datetime">
            <%-- 
                A data precisa ser ajustável também na edição inline administrativa.
                Mantemos o mesmo contrato HTMX da edição estrutural para salvar de forma
                incremental sem refresh completo da página.
            --%>
            <select name="data" class="form-control-inline"
                    hx-post="${base}/admin/salvarEdicaoEstrutural.action"
                    hx-trigger="change" hx-include="closest tr" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                <c:forTokens var="d" delims="," items="${initParam.datas}">
                    <c:set var="dTrim" value="${fn:trim(d)}" />
                    <option value="${dTrim}" ${dTrim eq dataJogoFormatada ? 'selected' : ''}>${dTrim}</option>
                </c:forTokens>
            </select>
            <select name="hora" class="form-control-inline" 
                    hx-post="${base}/admin/salvarEdicaoEstrutural.action" 
                    hx-trigger="change" hx-include="closest tr" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                <c:forTokens var="h" delims="," items="${initParam.horarios}">
                    <c:set var="hTrim" value="${fn:trim(h)}" />
                    <option value="${hTrim}" ${hTrim eq horaJogoFormatada ? 'selected' : ''}>${hTrim}</option>
                </c:forTokens>
            </select>
            </div>
        </sec:authorize>
        <sec:authorize access="!hasRole('ADMIN')">${dataJogoFormatada} ${horaJogoFormatada}</sec:authorize>
    </td>

    <td class="match-table__location">
        <sec:authorize access="hasRole('ADMIN')">
            <select name="local" class="form-control-inline"
                    hx-post="${base}/admin/salvarEdicaoEstrutural.action" 
                    hx-trigger="change" hx-include="closest tr" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                <c:forTokens var="loc" delims="," items="${initParam.locais}">
                    <c:set var="locTrim" value="${fn:trim(loc)}" />
                    <option value="${locTrim}" ${locTrim eq jogo.local ? 'selected' : ''}>${locTrim}</option>
                </c:forTokens>
            </select>
        </sec:authorize>
        <sec:authorize access="!hasRole('ADMIN')">${jogo.local}</sec:authorize>
    </td>

    <td class="match-table__group">
        <sec:authorize access="hasRole('ADMIN')">
            <select name="fase" class="form-control-inline"
                    hx-post="${base}/admin/salvarEdicaoEstrutural.action" 
                    hx-trigger="change" hx-include="closest tr" hx-swap="outerHTML" hx-target="#jogoTr_${jogo.id}">
                <c:forTokens var="f" items="11,12,13,16,8,4,2,3,1" delims=",">
                    <option value="${f}" ${f eq jogo.fase ? 'selected' : ''}>
                        <fmt:message key="filter.fase.${f}" />
                    </option>
                </c:forTokens>
            </select>
        </sec:authorize>
        <sec:authorize access="!hasRole('ADMIN')">
            <c:choose>
                <c:when test="${jogo.faseDeGrupos and not empty jogo.equipe1.grupo}">
                    <fmt:message key="match.group" var="gLabel" /> ${gLabel} ${jogo.equipe1.grupo}
                </c:when>
                <c:otherwise>${jogo.descricaoFase}</c:otherwise>
            </c:choose>
        </sec:authorize>
    </td>

    <td class="match-table__team match-table__team--home">
        <div class="team-cell text-right">
            <sec:authorize access="hasRole('ADMIN')">
                <select name="equipe1Id" class="form-control-inline team-select-direct"
                        hx-post="${base}/admin/salvarEdicaoEstrutural.action" 
                        hx-trigger="change" hx-include="closest tr" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML">
                    <c:forEach var="equipeItem" items="${equipes}">
                        <option value="${equipeItem.id}" ${equipeItem.id eq jogo.equipe1.id ? 'selected' : ''}>
                            <c:out value="${equipeItem.nomePais}" />
                        </option>
                    </c:forEach>
                </select>
            </sec:authorize>
            <sec:authorize access="!hasRole('ADMIN')"><span>${jogo.equipe1.nomePais}</span></sec:authorize>
            
            <c:choose>
                <c:when test="${not empty jogo.equipe1BandeiraUrl}">
                    <img class="flag-icon icon-inline" src="${base}${jogo.equipe1BandeiraUrl}" width="24" height="18" loading="lazy" alt="Bandeira de ${jogo.equipe1.nomePais}" title="${jogo.equipe1.nomePais}" />
                </c:when>
                <c:when test="${not empty jogo.equipe1EmojiBandeira}">
                    <span class="flag-icon icon-inline" role="img" aria-label="${jogo.equipe1.nomePais}" title="${jogo.equipe1.nomePais}">
                        <c:out value="${jogo.equipe1EmojiBandeira}" />
                    </span>
                </c:when>
                <c:otherwise><span class="flag-icon flag-icon--fallback icon-inline" title="${jogo.equipe1.nomePais}">${jogo.equipe1SiglaPais}</span></c:otherwise>
            </c:choose>

            <sec:authorize access="hasRole('ADMIN')">
                <input type="text" name="golsEquipe1" value="${jogo.golsEquipe1}" class="text score-input input-centered" maxlength="2" size="2"
                       hx-post="${base}/admin/atualizarResultadoJogo.action" hx-trigger="blur" hx-include="closest tr" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML" />
            </sec:authorize>
            <sec:authorize access="!hasRole('ADMIN')"><span class="score-value">${jogo.jaFoiAtualizado() ? jogo.golsEquipe1 : ''}</span></sec:authorize>
        </div>
    </td>

    <td class="match-table__separator">X</td>

    <td class="match-table__team match-table__team--away">
        <div class="team-cell text-left">
            <sec:authorize access="hasRole('ADMIN')">
                <input type="text" name="golsEquipe2" value="${jogo.golsEquipe2}" class="text score-input input-centered" maxlength="2" size="2"
                       hx-post="${base}/admin/atualizarResultadoJogo.action" hx-trigger="blur" hx-include="closest tr" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML" />
            </sec:authorize>
            <sec:authorize access="!hasRole('ADMIN')"><span class="score-value">${jogo.jaFoiAtualizado() ? jogo.golsEquipe2 : ''}</span></sec:authorize>

            <c:choose>
                <c:when test="${not empty jogo.equipe2BandeiraUrl}">
                    <img class="flag-icon icon-inline" src="${base}${jogo.equipe2BandeiraUrl}" width="24" height="18" loading="lazy" alt="Bandeira de ${jogo.equipe2.nomePais}" title="${jogo.equipe2.nomePais}" />
                </c:when>
                <c:when test="${not empty jogo.equipe2EmojiBandeira}">
                    <span class="flag-icon icon-inline" role="img" aria-label="${jogo.equipe2.nomePais}" title="${jogo.equipe2.nomePais}">
                        <c:out value="${jogo.equipe2EmojiBandeira}" />
                    </span>
                </c:when>
                <c:otherwise><span class="flag-icon flag-icon--fallback icon-inline" title="${jogo.equipe2.nomePais}">${jogo.equipe2SiglaPais}</span></c:otherwise>
            </c:choose>

            <sec:authorize access="hasRole('ADMIN')">
                <select name="equipe2Id" class="form-control-inline team-select-direct"
                        hx-post="${base}/admin/salvarEdicaoEstrutural.action" 
                        hx-trigger="change" hx-include="closest tr" hx-target="#jogoTr_${jogo.id}" hx-swap="outerHTML">
                    <c:forEach var="equipeItem" items="${equipes}">
                        <option value="${equipeItem.id}" ${equipeItem.id eq jogo.equipe2.id ? 'selected' : ''}>
                            <c:out value="${equipeItem.nomePais}" />
                        </option>
                    </c:forEach>
                </select>
            </sec:authorize>
            <sec:authorize access="!hasRole('ADMIN')"><span>${jogo.equipe2.nomePais}</span></sec:authorize>
        </div>
    </td>

    <sec:authorize access="hasRole('ADMIN')">
        <td class="match-table__actions">
            <div class="htmx-indicator progress-spinner progress-spinner--mini"></div>
        </td>
    </sec:authorize>
</tr>
