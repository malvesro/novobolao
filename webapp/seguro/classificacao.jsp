<%@include file="/template/menu.jspf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div style="float: right; width: 605px;">
<opendev:portlet id="participantesPortlet" title="Classificação" style="width: 600px; margin: 0 auto;">
    <table cellpadding="2" cellspacing="1" width="100%;" class="conteudo">
        <thead>
            <tr>
                <th><fmt:message key="ranking.position" /></th>
                <th><fmt:message key="member.name" /></th>
                <th><fmt:message key="member.login" /></th>                
                <th><fmt:message key="ranking.points" /></th>
                <th><fmt:message key="ranking.hints" /></th>
                <th><fmt:message key="ranking.at" /></th>
                <th><fmt:message key="ranking.ap" /></th>
                <th><fmt:message key="ranking.apb" /></th>
                <th><fmt:message key="ranking.b" /></th>
                <th><fmt:message key="ranking.e" /></th>
                <th><fmt:message key="ranking.apr" /></th>
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
                <c:choose>
                    <c:when test="${participante.login eq pageContext.request.userPrincipal.name}">
                        <tr id="linhaParticipante_${participante.id}" class="${rowStyleClass}" style="font-weight: bold;">
                    </c:when>
                    <c:otherwise>
                        <tr id="linhaParticipante_${participante.id}" class="${rowStyleClass}">
                    </c:otherwise>
                </c:choose>
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
                    <td style="text-align: center; width: 50px;"><c:out value="${posicaoRanking}" /></td>
                    <td style="text-align: left; width: 180px;"><c:out value="${pontuacaoTotalDoParticipante.nomeParticipante}" /></td>
                    <td style="text-align: left; width: 90px;"><c:out value="${pontuacaoTotalDoParticipante.loginParticipante}" /></td>
                    <td style="text-align: center; width: 70px;"><c:out value="${pontuacaoTotalDoParticipante.pontuacao}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.totalPalpitesConsiderados}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosTotais}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosParciais}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeAcertosParciaisComBonus}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.quantidadeSoBonus}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.quantidadeDeErros}" /></td>
                    <td style="text-align: center; width: 30px;"><c:out value="${pontuacaoTotalDoParticipante.aproveitamento}" /></td>
                </tr>
                <c:set var="qtdePontosAnterior" value="${pontuacaoTotalDoParticipante.pontuacao}" />
            </c:forEach>
        </tbody>
    </table>
</opendev:portlet>
<div style="height: 20px;"></div>
<div class="legenda">
	<p><span class="abrev"><fmt:message key="ranking.hints" /> - </span><span><fmt:message key="ranking.hints.legend" /></span></p>
    <p><span class="abrev"><fmt:message key="ranking.at" /> - </span><span><fmt:message key="ranking.at.legend" /></span></p>
    <p><span class="abrev"><fmt:message key="ranking.ap" /> - </span><span><fmt:message key="ranking.ap.legend" /></span></p>
    <p><span class="abrev"><fmt:message key="ranking.apb" /> - </span><span><fmt:message key="ranking.apb.legend" /></span></p>
    <p><span class="abrev"><fmt:message key="ranking.b" /> - </span><span><fmt:message key="ranking.b.legend" /></span></p>
    <p><span class="abrev"><fmt:message key="ranking.e" /> - </span><span><fmt:message key="ranking.e.legend" /></span></p>
    <p><span class="abrev"><fmt:message key="ranking.apr" /> - </span><span><fmt:message key="ranking.apr.legend" /></span></p>
</div>
<div style="height: 20px;"></div>
<opendev:isIE>
<div style="height: 350px;"></div>
</opendev:isIE>
</div>