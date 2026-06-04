<%@include file="/WEB-INF/content/template/menu.jspf" %>

<fmt:message key="ranking.hints.legend" var="legendHints" />
<fmt:message key="ranking.at.legend" var="legendAT" />
<fmt:message key="ranking.ap.legend" var="legendAP" />
<fmt:message key="ranking.apb.legend" var="legendAPB" />
<fmt:message key="ranking.b.legend" var="legendB" />
<fmt:message key="ranking.e.legend" var="legendE" />
<fmt:message key="ranking.apr.legend" var="legendAPR" />

<div class="dashboard-section">
    <opendev:portlet id="participantesPortlet" title="Classificacao">
        <div class="table-responsive">
            <table class="table conteudo">
                <thead>
                    <tr>
                        <th scope="col" class="text-center"><fmt:message key="ranking.position" /></th>
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
                    <c:set var="qtdePontosAnterior" value="-1" />
                    <c:set var="posicaoRanking" value="-1" />
                    <c:forEach var="participante" items="${participantes}" varStatus="loop">
                        <c:set var="pontuacaoTotalDoParticipante" value="${participante.pontuacaoTotal}" />
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
                        <c:choose>
                            <c:when test="${posicaoRanking eq -1}">
                                <c:set var="posicaoRanking" value="1" />
                            </c:when>
                            <c:when test="${pontuacaoTotalDoParticipante.pontuacao eq qtdePontosAnterior}">
                                <c:set var="posicaoRanking" value="" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="posicaoRanking" value="${loop.count}" />
                            </c:otherwise>
                        </c:choose>
                        <tr id="linhaParticipante_${participante.id}" class="${rowClasses}">
                            <td class="text-center"><c:out value="${posicaoRanking}" /></td>
                            <td class="text-left"><c:out value="${pontuacaoTotalDoParticipante.nomeParticipante}" /></td>
                            <td class="text-left"><c:out value="${pontuacaoTotalDoParticipante.loginParticipante}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.pontuacao}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.totalPalpitesConsiderados}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosTotais}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosParciais}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosParciaisComBonus}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeSoBonus}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeErros}" /></td>
                            <td class="text-center"><c:out value="${pontuacaoTotalDoParticipante.aproveitamento}" /></td>
                        </tr>
                        <c:set var="qtdePontosAnterior" value="${pontuacaoTotalDoParticipante.pontuacao}" />
                    </c:forEach>
                </tbody>
            </table>
        </div>
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
    </div>
    <span class="spacer spacer-sm"></span>
</div>
