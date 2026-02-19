<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@include file="/template/menu.jspf" %>

<c:if test="${empty jogosDeHoje}">
    <opendev:portlet id="jogos_hoje_vazio" title="Jogos de Hoje" style="width: 600px; margin: 0 auto;">
        <div class="info">
            <fmt:message key="match.none.today" />
        </div>
    </opendev:portlet>
</c:if>

<div style="float: right; width: 605px;">
    <c:if test="${not empty jogosDeHoje}">
    <opendev:portlet id="jogos_hoje_portlet" title="Jogos de Hoje" style="width: 600px; margin: 0 auto;">
		<table width="100%" cellspacing="1" cellpadding="2" class="conteudo" align="center">
			<thead>
				<tr>
					<th><fmt:message key="match.hour" /></th>
					<th><fmt:message key="match.where" /></th>
					<th><fmt:message key="match.group" /></th>
					<th colspan="3"><fmt:message key="match.teams" /></th>
				</tr>
			</thead>
			<tbody>
            <c:forEach var="jogo" items="${jogosDeHoje}">
        		<c:choose>
        			<c:when test="${jogo.equipe1.nomePais eq 'Brasil' or jogo.equipe2.nomePais eq 'Brasil'}">
        				<c:set var="rowStyleClass" value="brasil" />
        			</c:when>
        			<c:when test="${rowIndex mod 2 eq 0}">
        				<c:set var="rowStyleClass" value="impar" />
        			</c:when>
        			<c:otherwise>
        				<c:set var="rowStyleClass" value="par" />
        			</c:otherwise>
        		</c:choose>
                <tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
                    <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
					<td align="center" style="width: 10%;">${horaJogoFormatada}</td>
					<td style="width: 16%;">${jogo.local}</td>
					<td align="center" style="width: 10%;">${jogo.equipe1.grupo}</td>
					<td align="right" style="width: 30%;">
						<span>${jogo.equipe1.nomePais}</span>
						<span><img alt="" src="${base}/img/bandeiras/${jogo.equipe1.id}.gif" /></span>							
                        <span style="padding-left: 5px; padding-right: 5px; text-align: center;">${jogo.golsEquipe1}</span>
					</td>
						<td align="center" style="width: 4%;">X</td>
						<td align="left" style="width: 30%;">
							<span style="padding-left: 5px; padding-right: 5px; text-align: center;">${jogo.golsEquipe2}</span>
							<span><img alt="" src="${base}/img/bandeiras/${jogo.equipe2.id}.gif" /></span>
							<span>${jogo.equipe2.nomePais}</span>
						</td>
					</tr>
            </c:forEach>
            </tbody>
        </table>
    </opendev:portlet>
    <div style="height: 20px;"></div>
    </c:if>
    <opendev:portlet id="grafico_lideres_portlet" title="Lideranca" style="width: 600px; margin: 0 auto; height: 220px; background-color: #FFFFFF;">
        <c:url var="graficoLideresUrl" value="/seguro/graficoLiderancaImagem.action" />
        <div style="text-align: center; padding: 10px 0;">
            <img src="${graficoLideresUrl}" alt="Grafico de lideranca" style="max-width: 100%; height: auto;" />
        </div>
    </opendev:portlet>
</div>
<opendev:isIE>
    <div style="height: 200px;"></div>
</opendev:isIE>
