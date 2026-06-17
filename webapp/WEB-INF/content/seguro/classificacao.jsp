<%@include file="/WEB-INF/content/template/menu.jspf" %>

<fmt:message key="ranking.hints.legend" var="legendHints" />
<fmt:message key="ranking.at.legend" var="legendAT" />
<fmt:message key="ranking.ap.legend" var="legendAP" />
<fmt:message key="ranking.apb.legend" var="legendAPB" />
<fmt:message key="ranking.b.legend" var="legendB" />
<fmt:message key="ranking.e.legend" var="legendE" />
<fmt:message key="ranking.apr.legend" var="legendAPR" />
<fmt:message key="ranking.delta.legend" var="legendDelta" />
<fmt:message key="ranking.top10.title" var="rankingTop10Title" />
<fmt:message key="ranking.top10.subtitle" var="rankingTop10Subtitle" />
<fmt:message key="ranking.top10.podium" var="rankingTop10PodiumLabel" />
<fmt:message key="ranking.top10.rest" var="rankingTop10RestLabel" />
<fmt:message key="ranking.top10.user" var="rankingTop10UserLabel" />
<fmt:message key="ranking.top10.tieNotice" var="rankingTieNotice" />
<fmt:message key="ranking.medal.gold" var="rankingMedalGold" />
<fmt:message key="ranking.medal.silver" var="rankingMedalSilver" />
<fmt:message key="ranking.medal.bronze" var="rankingMedalBronze" />
<fmt:message key="ranking.top10.points" var="rankingTop10PointsLabel" />
<fmt:message key="ranking.table.title" var="rankingTableTitle" />
<fmt:message key="ranking.delta.compactLegend" var="rankingDeltaCompactLegend" />

<div class="dashboard-section">
    <opendev:portlet id="participantesPortlet" title="Classificacao">
        <c:choose>
            <c:when test="${empty participantes}">
                <div class="notice-card notice-card--info">
                    <p><fmt:message key="home.leaders.summary.empty" /></p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="ranking-impact" aria-labelledby="ranking-impact-title">
                    <div class="ranking-impact__header">
                        <h2 id="ranking-impact-title" class="ranking-impact__title"><c:out value="${rankingTop10Title}" /></h2>
                        <p class="ranking-impact__subtitle"><c:out value="${rankingTop10Subtitle}" /></p>
                        <c:if test="${participantes[0] ne null and participantes[1] ne null and participantes[0].pontuacaoTotal.pontuacao eq participantes[1].pontuacaoTotal.pontuacao}">
                            <p class="ranking-impact__tie-note">
                                <c:out value="${rankingTieNotice}" />
                            </p>
                        </c:if>
                    </div>

                    <div class="ranking-podium" aria-label="${rankingTop10PodiumLabel}">
                        <c:forEach var="participanteTop" items="${participantes}" begin="0" end="2" varStatus="loopTop3">
                            <c:set var="pontuacaoTop3" value="${participanteTop.pontuacaoTotal}" />
                            <c:set var="podiumPosition" value="${loopTop3.count}" />
                            <c:set var="medalLabel" value="${rankingMedalBronze}" />
                            <c:choose>
                                <c:when test="${podiumPosition eq 1}">
                                    <c:set var="medalLabel" value="${rankingMedalGold}" />
                                </c:when>
                                <c:when test="${podiumPosition eq 2}">
                                    <c:set var="medalLabel" value="${rankingMedalSilver}" />
                                </c:when>
                            </c:choose>
                            <article class="ranking-podium__item ranking-podium__item--${podiumPosition}">
                                <p class="ranking-podium__medal">${medalLabel}</p>
                                <p class="ranking-podium__position">${podiumPosition}º</p>
                                <p class="ranking-podium__name"><c:out value="${pontuacaoTop3.nomeParticipante}" /></p>
                                <p class="ranking-podium__login">@<c:out value="${pontuacaoTop3.loginParticipante}" /></p>
                                <p class="ranking-podium__points">
                                    <strong><c:out value="${pontuacaoTop3.pontuacao}" /></strong>
                                    <span><c:out value="${rankingTop10PointsLabel}" /></span>
                                </p>
                                <c:set var="variacaoTop3" value="${pontuacaoTop3.variacaoPosicao}" />
                                <c:choose>
                                    <c:when test="${empty variacaoTop3}">
                                        <span class="ranking-variation ranking-variation-new" aria-label="${legendDelta}">NOVO</span>
                                    </c:when>
                                    <c:when test="${variacaoTop3 gt 0}">
                                        <span class="ranking-variation ranking-variation-up" aria-label="${legendDelta}">▲ +<c:out value="${variacaoTop3}" /></span>
                                    </c:when>
                                    <c:when test="${variacaoTop3 lt 0}">
                                        <span class="ranking-variation ranking-variation-down" aria-label="${legendDelta}">▼ -<c:out value="${variacaoTop3 * -1}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ranking-variation ranking-variation-stable" aria-label="${legendDelta}">• 0</span>
                                    </c:otherwise>
                                </c:choose>
                            </article>
                        </c:forEach>
                    </div>

                    <div class="ranking-top10-track" aria-label="${rankingTop10RestLabel}">
                        <c:forEach var="participanteTop10" items="${participantes}" begin="3" end="9" varStatus="loopTop10">
                            <c:set var="pontuacaoTop10" value="${participanteTop10.pontuacaoTotal}" />
                            <c:set var="top10Position" value="${loopTop10.count + 3}" />
                            <article class="ranking-top10-card ${participanteTop10.login eq pageContext.request.userPrincipal.name ? 'ranking-top10-card--me' : ''}">
                                <p class="ranking-top10-card__position">${top10Position}º</p>
                                <p class="ranking-top10-card__name"><c:out value="${pontuacaoTop10.nomeParticipante}" /></p>
                                <p class="ranking-top10-card__meta">@<c:out value="${pontuacaoTop10.loginParticipante}" /></p>
                                <p class="ranking-top10-card__points">
                                    <strong><c:out value="${pontuacaoTop10.pontuacao}" /></strong>
                                    <span><c:out value="${rankingTop10PointsLabel}" /></span>
                                </p>
                                <c:set var="variacaoTop10" value="${pontuacaoTop10.variacaoPosicao}" />
                                <c:choose>
                                    <c:when test="${empty variacaoTop10}">
                                        <span class="ranking-variation ranking-variation-new" aria-label="${legendDelta}">NOVO</span>
                                    </c:when>
                                    <c:when test="${variacaoTop10 gt 0}">
                                        <span class="ranking-variation ranking-variation-up" aria-label="${legendDelta}">▲ +<c:out value="${variacaoTop10}" /></span>
                                    </c:when>
                                    <c:when test="${variacaoTop10 lt 0}">
                                        <span class="ranking-variation ranking-variation-down" aria-label="${legendDelta}">▼ -<c:out value="${variacaoTop10 * -1}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ranking-variation ranking-variation-stable" aria-label="${legendDelta}">• 0</span>
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${participanteTop10.login eq pageContext.request.userPrincipal.name}">
                                    <span class="ranking-top10-card__me-label"><c:out value="${rankingTop10UserLabel}" /></span>
                                </c:if>
                            </article>
                        </c:forEach>
                    </div>
                </div>

                <div class="ranking-table-headline">
                    <h3><c:out value="${rankingTableTitle}" /></h3>
                    <p><c:out value="${rankingDeltaCompactLegend}" /></p>
                </div>

                <div class="table-responsive">
                    <table class="table conteudo ranking-table">
                        <thead>
                            <tr>
                                <th scope="col" class="text-center"><fmt:message key="ranking.position" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendDelta}"><fmt:message key="ranking.delta" /></th>
                                <th scope="col" class="text-left"><fmt:message key="member.name" /></th>
                                <th scope="col" class="text-left"><fmt:message key="member.login" /></th>
                                <th scope="col" class="text-center"><fmt:message key="ranking.points" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendHints}"><fmt:message key="ranking.hints" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendAT}"><fmt:message key="ranking.at" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendAP}"><fmt:message key="ranking.ap" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendAPB}"><fmt:message key="ranking.apb" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendB}"><fmt:message key="ranking.b" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendE}"><fmt:message key="ranking.e" /></th>
                                <th scope="col" class="text-center" data-tooltip="${legendAPR}"><fmt:message key="ranking.apr" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="participante" items="${participantes}" varStatus="loop">
                                <c:set var="pontuacaoTotalDoParticipante" value="${participante.pontuacaoTotal}" />
                                <c:set var="posicaoRanking" value="${loop.count}" />
                                <c:choose>
                                    <c:when test="${loop.count mod 2 eq 0}">
                                        <c:set var="rowStyleClass" value="par" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="rowStyleClass" value="impar" />
                                    </c:otherwise>
                                </c:choose>
                                <c:set var="rowClasses" value="${rowStyleClass}" />
                                <c:if test="${participante.login eq pageContext.request.userPrincipal.name}">
                                    <c:set var="rowClasses" value="${rowClasses} ranking-highlight" />
                                </c:if>
                                <c:if test="${loop.count le 10}">
                                    <c:set var="rowClasses" value="${rowClasses} ranking-top10-row" />
                                </c:if>
                                <c:if test="${loop.count le 3}">
                                    <c:set var="rowClasses" value="${rowClasses} ranking-podium-row ranking-podium-row--${loop.count}" />
                                </c:if>
                                <tr id="linhaParticipante_${participante.id}" class="${rowClasses}">
                                    <td class="text-center ranking-position-cell">
                                        <span class="ranking-position-badge"><c:out value="${posicaoRanking}" /></span>
                                    </td>
                                    <td class="text-center ranking-variation-cell">
                                        <c:set var="variacaoPosicao" value="${pontuacaoTotalDoParticipante.variacaoPosicao}" />
                                        <c:choose>
                                            <c:when test="${empty variacaoPosicao}">
                                                <span class="ranking-variation ranking-variation-new" aria-hidden="true">NOVO</span>
                                                <span class="sr-only"><fmt:message key="ranking.delta.noHistory" /></span>
                                            </c:when>
                                            <c:when test="${variacaoPosicao gt 0}">
                                                <fmt:message key="ranking.delta.up" var="deltaDescricao">
                                                    <fmt:param value="${variacaoPosicao}" />
                                                </fmt:message>
                                                <span class="ranking-variation ranking-variation-up" aria-hidden="true">▲ +<c:out value="${variacaoPosicao}" /></span>
                                                <span class="sr-only"><c:out value="${deltaDescricao}" /></span>
                                            </c:when>
                                            <c:when test="${variacaoPosicao lt 0}">
                                                <fmt:message key="ranking.delta.down" var="deltaDescricao">
                                                    <fmt:param value="${variacaoPosicao * -1}" />
                                                </fmt:message>
                                                <span class="ranking-variation ranking-variation-down" aria-hidden="true">▼ -<c:out value="${variacaoPosicao * -1}" /></span>
                                                <span class="sr-only"><c:out value="${deltaDescricao}" /></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="ranking-variation ranking-variation-stable" aria-hidden="true">• 0</span>
                                                <span class="sr-only"><fmt:message key="ranking.delta.stable" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-left ranking-name-cell"><c:out value="${pontuacaoTotalDoParticipante.nomeParticipante}" /></td>
                                    <td class="text-left ranking-login-cell"><c:out value="${pontuacaoTotalDoParticipante.loginParticipante}" /></td>
                                    <td class="text-center ranking-points-cell"><c:out value="${pontuacaoTotalDoParticipante.pontuacao}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.totalPalpitesConsiderados}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosTotais}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosParciais}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosParciaisComBonus}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeSoBonus}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeErros}" /></td>
                                    <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.aproveitamento}" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </opendev:portlet>
    <span class="spacer spacer-sm"></span>
    <div class="legenda">
        <p><span class="abrev"><fmt:message key="ranking.hints" /> - </span><span><fmt:message key="ranking.hints.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.at" /> - </span><span><fmt:message key="ranking.at.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.ap" /> - </span><span><fmt:message key="ranking.ap.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.apb" /> - </span><span><fmt:message key="ranking.apb.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.b" /> - </span><span><fmt:message key="ranking.b.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.e" /> - </span><span><fmt:message key="ranking.e.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.apr" /> - </span><span><fmt:message key="ranking.apr.legend" /></span></p>
        <p><span class="abrev"><fmt:message key="ranking.delta" /> - </span><span><fmt:message key="ranking.delta.legend" /></span></p>
    </div>
    <span class="spacer spacer-sm"></span>
</div>
