<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="jogo" value="${jogos[0]}" />
<fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
<fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />

<div class="drawer-header">
    <h2>Gerenciar Jogo #${jogo.id}</h2>
    <button type="button" class="drawer-close-btn" 
            onclick="closeDrawer()">&times;</button>
</div>

<div class="drawer-body">
    <form hx-post="${pageContext.request.contextPath}/admin/salvarEdicaoEstrutural.action"
          hx-target="#jogoTr_${jogo.id}" 
          hx-swap="outerHTML" 
          hx-indicator="#drawer-save-indicator"
          hx-on::after-request="if(event.detail.successful) closeDrawer()"
          class="admin-edit-form-vertical">

        <input type="hidden" name="id" value="${jogo.id}" />
        <c:if test="${not empty _csrf}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </c:if>

        <div class="form-group-stack">
            <%-- Informações Básicas --%>
            <div class="form-row-vertical">
                <label><fmt:message key="match.day" /></label>
                <select name="data" class="form-control">
                    <c:forTokens var="d" delims="," items="${initParam.datas}">
                        <option value="${d}" ${d eq dataJogoFormatada ? 'selected' : ''}>${d}</option>
                    </c:forTokens>
                </select>
            </div>

            <div class="form-row-vertical">
                <label><fmt:message key="match.hour" /></label>
                <select name="hora" class="form-control">
                    <c:forTokens var="h" delims="," items="${initParam.horarios}">
                        <option value="${h}" ${h eq horaJogoFormatada ? 'selected' : ''}>${h}</option>
                    </c:forTokens>
                </select>
            </div>

            <div class="form-row-vertical">
                <label><fmt:message key="match.where" /></label>
                <input type="text" name="local" value="${jogo.local}" maxlength="120" class="form-control" />
            </div>

            <div class="form-row-vertical">
                <label><fmt:message key="match.phase" /></label>
                <select name="fase" class="form-control">
                    <c:forTokens var="f" items="11,12,13,16,8,4,2,3,1" delims=",">
                        <option value="${f}" ${f eq jogo.fase ? 'selected' : ''}>
                            <fmt:message key="filter.fase.${f}" />
                        </option>
                    </c:forTokens>
                </select>
            </div>

            <%-- Seleção de Equipes --%>
            <div class="form-section-title">Equipes em Campo</div>
            <div class="team-management-grid">
                <div class="team-selection">
                    <label>Equipe 1 (Casa)</label>
                    <select name="equipe1Id" class="form-control">
                        <c:forEach var="eq" items="${equipes}">
                            <option value="${eq.id}" ${eq.id eq jogo.equipe1.id ? 'selected' : ''}>${eq.nomePais}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="team-selection-separator">VS</div>
                <div class="team-selection">
                    <label>Equipe 2 (Fora)</label>
                    <select name="equipe2Id" class="form-control">
                        <c:forEach var="eq" items="${equipes}">
                            <option value="${eq.id}" ${eq.id eq jogo.equipe2.id ? 'selected' : ''}>${eq.nomePais}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="drawer-footer">
            <button type="submit" class="button button--primary button--large">
                <fmt:message key="general.save" />
            </button>
            <button type="button" class="button button--ghost"
                    onclick="closeDrawer()">
                <fmt:message key="general.cancel" />
            </button>
            <div id="drawer-save-indicator" class="htmx-indicator progress-spinner"></div>
        </div>
    </form>
</div>

<style>
    .admin-edit-form-vertical { display: flex; flex-direction: column; gap: 1.5rem; }
    .form-group-stack { display: flex; flex-direction: column; gap: 1.2rem; }
    .form-row-vertical { display: flex; flex-direction: column; gap: 0.4rem; }
    .form-row-vertical label { font-size: 0.8rem; font-weight: bold; color: var(--color-text-muted); text-transform: uppercase; }
    .form-control { width: 100%; height: 40px; border: 1px solid var(--color-border); border-radius: var(--radius-base); background: var(--color-background); color: var(--color-text); padding: 0 0.8rem; font-size: 0.95rem; }
    .form-section-title { margin-top: 1rem; font-weight: bold; font-size: 1rem; border-bottom: 2px solid var(--color-primary); padding-bottom: 0.3rem; }
    .team-management-grid { display: grid; grid-template-columns: 1fr auto 1fr; align-items: flex-end; gap: 1rem; padding: 1rem; background: rgba(0,0,0,0.05); border-radius: var(--radius-base); }
    .team-selection-separator { font-weight: bold; padding-bottom: 0.8rem; color: var(--color-text-muted); }
    .drawer-footer { margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--color-border); display: flex; gap: 1rem; align-items: center; }
    .button--large { height: 45px; padding: 0 2rem; font-weight: bold; }
</style>