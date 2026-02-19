<%@include file="/template/menu.jspf" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<div style="float: right; width: 605px;">
<opendev:portlet id="chart_portlet" title="Grafico comparativo de desempenho" icon="/img/chart.png" style="width: 600px; margin: 0 auto;">
	<c:url var="graficoAction" value="/seguro/graficoDesempenho.action" />
	<form action="${graficoAction}" method="get">
	<div style="padding-top: 10px; padding-left: 10px; padding-bottom: 10px; text-align: left;">
		<span>
			<fmt:message key="performance.user">
				<fmt:param value="${participanteLogado.nomeFormatado}" />
			</fmt:message>
		</span>
	</div>
	<div style="padding-bottom: 10px; padding-left: 10px; text-align: left;">
		<span>
			<fmt:message key="performance.compare" />
		</span>
        <span id="span_combos">
            <select name="rival">
                <option value=""></option>
                <c:forEach var="participante" items="${participantes}">
                    <c:choose>
                        <c:when test="${param.rival eq participante.id}">
                            <option value="${participante.id}" selected="selected">${participante.nomeFormatado}</option>
                        </c:when>
                        <c:otherwise>
                            <option value="${participante.id}">${participante.nomeFormatado}</option>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </select>
        </span>
	</div>
	<div style="padding-bottom: 10px; text-align: center;">
		<span>
            <fmt:message var="chartSubmitLabel" key="chat.submitcompare" />
			<input type="submit" name="graficoSubmit" id="grafico_submit" class="button" value="${chartSubmitLabel}" />
		</span>
	</div>
	<div style="padding-top: 10px; padding-bottom: 10px; text-align: center;">
        <c:url var="graficoImgUrl" value="/seguro/graficoDesempenhoImagem.action">
            <c:param name="rival" value="${param.rival}" />
        </c:url>
        <img src="${graficoImgUrl}" alt="Grafico comparativo de desempenho" style="max-width: 100%; height: auto;" />
	</div>
</opendev:portlet>
<div style="height: 20px;"></div>
</div>

<opendev:isIE>
	<div style="height: 300px;"></div>
</opendev:isIE>
