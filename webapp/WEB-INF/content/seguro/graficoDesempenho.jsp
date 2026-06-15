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
                    <div id="performance-chart"></div>
                </div>
            </div>
        </form>
    </opendev:portlet>
    <span class="spacer spacer-sm"></span>
</div>

<script src="https://cdn.jsdelivr.net/npm/apexcharts"></script>
<script>
    async function loadChart() {
        const rivalId = document.getElementById('rival').value;
        const response = await fetch(`${pageContext.request.contextPath}/seguro/obterDadosGraficoJson.action?rival=${rivalId}`);
        const data = await response.json();

        const options = {
            chart: {
                type: 'line',
                height: 350
            },
            series: data.series,
            xaxis: {
                type: 'datetime'
            },
            tooltip: {
                x: { format: 'dd/MM/yyyy' }
            }
        };

        const chart = new ApexCharts(document.querySelector("#performance-chart"), options);
        chart.render();
    }

    document.getElementById('grafico_submit').addEventListener('click', (e) => {
        e.preventDefault();
        loadChart();
    });
</script>
