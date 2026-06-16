<%@include file="/WEB-INF/content/template/menu.jspf" %>
<c:url var="graficoJsonEndpoint" value="/seguro/obterDadosGraficoJson.action" />
<fmt:message key="performance.chart.loading" var="chartLoadingMessage" />
<fmt:message key="performance.chart.error" var="chartErrorMessage" />
<fmt:message key="performance.chart.empty" var="chartEmptyMessage" />
<fmt:message key="performance.chart.cache" var="chartCacheMessage" />
<fmt:message key="performance.chart.retry" var="chartRetryLabel" />
<fmt:message key="performance.chart.status.idle" var="chartIdleStatus" />
<fmt:message key="performance.chart.status.loading" var="chartLoadingStatus" />
<fmt:message key="performance.chart.status.ready" var="chartReadyStatus" />
<fmt:message key="performance.chart.status.error" var="chartErrorStatus" />

<div class="dashboard-section">
    <opendev:portlet id="chart_portlet" title="Grafico comparativo de desempenho" icon="/img/chart.png">
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
            <div
                class="chart-wrapper chart-wrapper--performance"
                data-chart-endpoint="${graficoJsonEndpoint}"
                data-loading-message="${chartLoadingMessage}"
                data-error-message="${chartErrorMessage}"
                data-empty-message="${chartEmptyMessage}"
                data-cache-message="${chartCacheMessage}"
                data-retry-label="${chartRetryLabel}"
                data-status-idle="${chartIdleStatus}"
                data-status-loading="${chartLoadingStatus}"
                data-status-ready="${chartReadyStatus}"
                data-status-error="${chartErrorStatus}">
                <div id="performance-chart-status" class="chart-status" role="status" aria-live="polite">
                    <c:out value="${chartIdleStatus}" />
                </div>
                <div id="performance-chart" class="performance-chart" aria-describedby="performance-chart-status"></div>
            </div>
        </div>
    </opendev:portlet>
    <span class="spacer spacer-sm"></span>
</div>
