<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

            <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
            <tr class="${rowStyleClass}" id="jogoTr_${jogo.id}" data-jogo-id="${jogo.id}" data-palpite-allowed="false">
                <td class="match-table__time">${horaJogoFormatada}</td>
                <td class="match-table__location">${jogo.local}</td>
                <td class="match-table__group">
                    <c:choose>
                        <c:when test="${jogo.faseDeGrupos}">
                            <c:choose>
                                <c:when test="${not empty jogo.equipe1.grupo}">
                                    <fmt:message key="match.group" var="grupoLabelAdmin" />
                                    <span>${grupoLabelAdmin} ${jogo.equipe1.grupo}</span>
                                </c:when>
                                <c:otherwise>
                                    <span>${jogo.descricaoFase}</span>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <span>${jogo.descricaoFase}</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="match-table__team match-table__team--home">
                    <div class="team-cell text-right">
                        <span>
                            <c:out value="${jogo.equipe1.nomePais}" />
                        </span>
                        <c:choose>
                            <c:when test="${not empty jogo.equipe1.bandeiraUrl}">
                                <img class="flag-icon icon-inline"
                                    src="${pageContext.request.contextPath}${jogo.equipe1.bandeiraUrl}"
                                    alt="Bandeira de ${jogo.equipe1.nomePais}" width="24" height="18" loading="lazy" />
                            </c:when>
                            <c:when test="${not empty jogo.equipe1.emojiBandeira}">
                                <span class="flag-icon icon-inline" role="img" aria-label="${jogo.equipe1.nomePais}">
                                    <c:out value="${jogo.equipe1.emojiBandeira}" />
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="flag-icon flag-icon--fallback icon-inline" aria-hidden="true">
                                    <c:out value="${jogo.equipe1.siglaPais}" />
                                </span>
                            </c:otherwise>
                        </c:choose>
                        <sec:authorize access="hasRole('ADMIN')">
                            <input type="text" name="golsEquipe1" id="golsEquipe1_tf_${jogo.id}"
                                class="text score-input input-centered" maxlength="2" size="2"
                                value="${jogo.golsEquipe1}" data-js="resultado-input" />
                        </sec:authorize>
                        <sec:authorize access="!hasRole('ADMIN')">
                            <span class="score-value">
                                <c:out value="${jogo.golsEquipe1}" />
                            </span>
                        </sec:authorize>
                    </div>
                </td>
                <td class="match-table__separator">X</td>
                <td class="match-table__team match-table__team--away">
                    <div class="team-cell text-left">
                        <sec:authorize access="hasRole('ADMIN')">
                            <input type="text" name="golsEquipe2" id="golsEquipe2_tf_${jogo.id}"
                                class="text score-input input-centered" maxlength="2" size="2"
                                value="${jogo.golsEquipe2}" onblur="atualizarResultado(this);"
                                data-js="resultado-input" />
                        </sec:authorize>
                        <sec:authorize access="!hasRole('ADMIN')">
                            <span class="score-value">
                                <c:out value="${jogo.golsEquipe2}" />
                            </span>
                        </sec:authorize>
                        <c:choose>
                            <c:when test="${not empty jogo.equipe2.bandeiraUrl}">
                                <img class="flag-icon icon-inline"
                                    src="${pageContext.request.contextPath}${jogo.equipe2.bandeiraUrl}"
                                    alt="Bandeira de ${jogo.equipe2.nomePais}" width="24" height="18" loading="lazy" />
                            </c:when>
                            <c:when test="${not empty jogo.equipe2.emojiBandeira}">
                                <span class="flag-icon icon-inline" role="img" aria-label="${jogo.equipe2.nomePais}">
                                    <c:out value="${jogo.equipe2.emojiBandeira}" />
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="flag-icon flag-icon--fallback icon-inline" aria-hidden="true">
                                    <c:out value="${jogo.equipe2.siglaPais}" />
                                </span>
                            </c:otherwise>
                        </c:choose>
                        <span>
                            <c:out value="${jogo.equipe2.nomePais}" />
                        </span>
                    </div>
                </td>
                <sec:authorize access="hasRole('ADMIN')">
                    <td class="match-table__actions">
                        <button type="button" class="icon-button button-ghost"
                            style="position: relative; z-index: 9999; pointer-events: all !important;"
                            onclick="alert('Botão de edição clicado para ID: ${jogo.id}');"
                            hx-get="${base}/admin/prepararEdicaoEstrutural.action?id=${jogo.id}" hx-target="closest tr"
                            hx-swap="outerHTML" hx-indicator="closest tr" title="Editar times/local/data">
                            <img src="${pageContext.request.contextPath}/img/edit.png" alt="Editar" class="icon-inline"
                                style="pointer-events: none;" />
                        </button>
                    </td>
                </sec:authorize>
            </tr>