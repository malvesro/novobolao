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
                    <input type="button" name="graficoSubmit" id="grafico_submit" class="button" value="${chartSubmitLabel}" />
                </div>
                <div class="chart-wrapper">
                    <div id="performance-chart"></div>
                </div>
            </div>
        </form>
    </opendev:portlet>
    <span class="spacer spacer-sm"></span>
</div>

<script src="https://cdn.jsdelivr.net/npm/apexcharts" nonce="${cspNonce}"></script>
<script nonce="${cspNonce}">
    async function loadChart() {
        console.log("DEBUG: loadChart called");
        const performanceChart = document.querySelector("#performance-chart");
        performanceChart.innerHTML = '<div class="alert alert-info">Carregando...</div>';
        
        try {
            const rivalId = document.getElementById('rival').value;
            const url = `${pageContext.request.contextPath}/seguro/obterDadosGraficoJson.action?rival=${rivalId}`;
            console.log("DEBUG: Fetching from: " + url);
            
            const response = await fetch(url);
            console.log("DEBUG: Response status: " + response.status);
            
            if (!response.ok) throw new Error('Erro ao carregar dados do gráfico: ' + response.statusText);
            
            const data = await response.json();
            console.log("DEBUG: Data received: ", data);

            // Verificação de segurança: existem séries com dados?
            if (!data.series || data.series.length === 0 || data.series.every(s => s.data.length === 0)) {
                performanceChart.innerHTML = 
                    '<div class="alert alert-info" style="padding: 20px; text-align: center;">Ainda não há dados suficientes para gerar o gráfico.</div>';
                return;
            }

            performanceChart.innerHTML = ''; // Limpa o "Carregando"
            const options = {
                chart: { type: 'line', height: 350 },
                series: data.series,
                xaxis: { type: 'datetime' },
                tooltip: { x: { format: 'dd/MM/yyyy' } }
            };

            const chart = new ApexCharts(performanceChart, options);
            chart.render();
        } catch (error) {
            performanceChart.innerHTML = '<div class="alert alert-danger">Erro ao carregar o gráfico. Tente novamente mais tarde.</div>';
            console.error("DEBUG: Error:", error);
        }
    }

    document.getElementById('grafico_submit').addEventListener('click', (e) => {
        loadChart();
    });
</script>
