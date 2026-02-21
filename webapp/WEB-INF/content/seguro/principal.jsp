<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@include file="/WEB-INF/content/template/menu.jspf" %>

<div class="dashboard-section">
    <c:if test="${empty jogosDeHoje}">
        <opendev:portlet id="jogos_hoje_vazio" title="Jogos de Hoje">
            <div class="info">
                <fmt:message key="match.none.today" />
            </div>
        </opendev:portlet>
    </c:if>

    <c:if test="${not empty jogosDeHoje}">
    <opendev:portlet id="jogos_hoje_portlet" title="Jogos de Hoje">
		<div class="table-responsive">
		<table class="table conteudo">
			<thead>
				<tr>
					<th scope="col"><fmt:message key="match.hour" /></th>
					<th scope="col"><fmt:message key="match.where" /></th>
					<th scope="col"><fmt:message key="match.group" /></th>
					<th scope="colgroup" colspan="3"><fmt:message key="match.teams" /></th>
				</tr>
			</thead>
			<tbody>
            <c:forEach var="jogo" items="${jogosDeHoje}" varStatus="loop">
        		<c:choose>
        			<c:when test="${jogo.equipe1.nomePais eq 'Brasil' or jogo.equipe2.nomePais eq 'Brasil'}">
        				<c:set var="rowStyleClass" value="brasil" />
        			</c:when>
        			<c:when test="${loop.index mod 2 eq 0}">
        				<c:set var="rowStyleClass" value="impar" />
        			</c:when>
        			<c:otherwise>
        				<c:set var="rowStyleClass" value="par" />
        			</c:otherwise>
        		</c:choose>
                <tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
                    <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
					<td class="text-center">${horaJogoFormatada}</td>
					<td>${jogo.local}</td>
					<td class="text-center">${jogo.equipe1.grupo}</td>
					<td class="text-right">
						<div class="team-cell text-right">
							<span>${jogo.equipe1.nomePais}</span>
							<img alt="${jogo.equipe1.nomePais}" src="${base}/img/bandeiras/${jogo.equipe1.id}.gif" />
							<span class="score-value">${jogo.golsEquipe1}</span>
						</div>
					</td>
						<td class="text-center">X</td>
						<td class="text-left">
							<div class="team-cell text-left">
								<span class="score-value">${jogo.golsEquipe2}</span>
								<img alt="${jogo.equipe2.nomePais}" src="${base}/img/bandeiras/${jogo.equipe2.id}.gif" />
								<span>${jogo.equipe2.nomePais}</span>
							</div>
						</td>
					</tr>
            </c:forEach>
            </tbody>
        </table>
		</div>
    </opendev:portlet>
    <span class="spacer-sm"></span>
    </c:if>
    <opendev:portlet id="grafico_lideres_portlet" title="Lideranca">
        <c:url var="graficoLideresUrl" value="/seguro/graficoLiderancaImagem.action" />
        <div class="chart-wrapper">
            <img src="${graficoLideresUrl}" alt="Grafico de lideranca" />
        </div>
    </opendev:portlet>
</div>
