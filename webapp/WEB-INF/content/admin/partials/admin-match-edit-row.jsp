<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

            <c:set var="jogo" value="${jogos[0]}" />
            <fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
            <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
            <tr class="match-row-edit" id="jogoTr_${jogo.id}">
                <td colspan="7">
                    <form hx-post="${pageContext.request.contextPath}/admin/salvarEdicaoEstrutural.action"
                        hx-target="closest tr" hx-swap="outerHTML" hx-indicator="closest tr" class="admin-edit-form">

                        <input type="hidden" name="id" value="${jogo.id}" />
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:if>

                        <div class="admin-edit-grid">
                            <div class="admin-edit-field">
                                <label>
                                    <fmt:message key="match.day" />
                                </label>
                                <select name="data">
                                    <c:forTokens var="d" delims="," items="${initParam.datas}">
                                        <option value="${d}" ${d eq dataJogoFormatada ? 'selected' : '' }>${d}</option>
                                    </c:forTokens>
                                </select>
                            </div>
                            <div class="admin-edit-field">
                                <label>
                                    <fmt:message key="match.hour" />
                                </label>
                                <select name="hora">
                                    <c:forTokens var="h" delims="," items="${initParam.horarios}">
                                        <option value="${h}" ${h eq horaJogoFormatada ? 'selected' : '' }>${h}</option>
                                    </c:forTokens>
                                </select>
                            </div>
                            <div class="admin-edit-field">
                                <label>
                                    <fmt:message key="match.where" />
                                </label>
                                <input type="text" name="local" value="${jogo.local}" maxlength="120" />
                            </div>
                            <div class="admin-edit-field">
                                <label>
                                    <fmt:message key="match.phase" />
                                </label>
                                <select name="fase">
                                    <c:forTokens var="f" items="11,12,13,16,8,4,2,3,1" delims=",">
                                        <option value="${f}" ${f eq jogo.fase ? 'selected' : '' }>
                                            <fmt:message key="filter.fase.${f}" />
                                        </option>
                                    </c:forTokens>
                                </select>
                            </div>
                            <div class="admin-edit-field admin-edit-field--teams">
                                <label>
                                    <fmt:message key="match.teams" />
                                </label>
                                <div class="team-select-wrapper">
                                    <select name="equipe1Id">
                                        <c:forEach var="eq" items="${equipes}">
                                            <option value="${eq.id}" ${eq.id eq jogo.equipe1.id ? 'selected' : '' }>
                                                ${eq.nomePais}</option>
                                        </c:forEach>
                                    </select>
                                    <span class="form-field-separator">X</span>
                                    <select name="equipe2Id">
                                        <c:forEach var="eq" items="${equipes}">
                                            <option value="${eq.id}" ${eq.id eq jogo.equipe2.id ? 'selected' : '' }>
                                                ${eq.nomePais}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="admin-edit-actions">
                                <button type="submit" class="button button--small">
                                    <fmt:message key="general.save" />
                                </button>
                                <button type="button" class="button button--small button--ghost"
                                    hx-get="${pageContext.request.contextPath}/admin/carregarLinhaJogoAdmin.action?id=${jogo.id}"
                                    hx-target="closest tr" hx-swap="outerHTML" hx-indicator="closest tr">
                                    <fmt:message key="general.cancel" />
                                </button>
                            </div>
                        </div>
                    </form>
                </td>
            </tr>