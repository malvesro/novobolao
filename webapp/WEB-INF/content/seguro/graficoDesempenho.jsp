<%@include file="/WEB-INF/content/template/menu.jspf" %>

<div class="dashboard-section">
    <opendev:portlet id="chart_portlet" title="Grafico comparativo de desempenho" icon="/img/chart.png">
    	<c:url var="graficoAction" value="/seguro/graficoDesempenho.action" />
    	<form action="${graficoAction}" method="get">
            <div class="portlet-body">
                <div class="form-section">
                    <span>
                        <fmt:message key="performance.user">
                            <fmt:param value="${participanteLogado.nomeFormatado}" />
                        </fmt:message>
                    </span>
                </div>
                <div class="form-section">
                    <label for="rival">
                        <fmt:message key="performance.compare" />
                    </label>
                    <select class="form-control" name="rival" id="rival">
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
                </div>
                <div class="form-section is-centered">
                    <fmt:message var="chartSubmitLabel" key="chat.submitcompare" />
                    <input type="submit" name="graficoSubmit" id="grafico_submit" class="button" value="${chartSubmitLabel}" />
                </div>
                <div class="chart-wrapper">
                    <c:url var="graficoImgUrl" value="/seguro/graficoDesempenhoImagem.action">
                        <c:param name="rival" value="${param.rival}" />
                    </c:url>
                    <img src="${graficoImgUrl}" alt="Grafico comparativo de desempenho" />
                </div>
            </div>
        </form>
    </opendev:portlet>
    <span class="spacer spacer-sm"></span>
</div>
