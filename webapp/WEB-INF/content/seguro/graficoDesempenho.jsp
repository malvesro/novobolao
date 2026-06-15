<%@include file="/WEB-INF/content/template/menu.jspf" %>

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
            <div class="chart-wrapper" style="background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); border: 1px solid #e0e0e0;">
                <div id="performance-chart"></div>
            </div>
        </div>
    </opendev:portlet>
    <span class="spacer spacer-sm"></span>
</div>

<script src="https://cdn.jsdelivr.net/npm/apexcharts" nonce="${cspNonce}"></script>
<script nonce="${cspNonce}">
    async function loadChart() {
        const performanceChart = document.querySelector("#performance-chart");
        performanceChart.innerHTML = '<div class="alert alert-info">Carregando dados da Copa 2026...</div>';
        
        try {
            const rivalId = document.getElementById('rival').value;
            const url = `${pageContext.request.contextPath}/seguro/obterDadosGraficoJson.action?rival=${rivalId}`;
            
            const response = await fetch(url);
            
            if (!response.ok) throw new Error('Erro ao carregar dados do gráfico: ' + response.statusText);
            
            const data = await response.json();

            // Verificação: existem séries com dados?
            if (!data.series || data.series.length === 0 || data.series.every(s => s.data.length === 0)) {
                performanceChart.innerHTML = 
                    '<div class="alert alert-info" style="padding: 20px; text-align: center;">Ainda não há dados suficientes para gerar o gráfico.</div>';
                return;
            }

            performanceChart.innerHTML = ''; // Limpa o "Carregando"
            const options = {
                chart: { type: 'line', height: 350, fontFamily: 'inherit' },
                series: data.series,
                xaxis: { type: 'datetime' },
                tooltip: { x: { format: 'dd/MM/yyyy' } },
                colors: ['#003366', '#FFD700', '#008000'], // Identidade Copa 2026
                stroke: { curve: 'smooth', width: 3 }
            };

            const chart = new ApexCharts(performanceChart, options);
            chart.render();
        } catch (error) {
            performanceChart.innerHTML = '<div class="alert alert-danger">Erro ao carregar o gráfico. Tente novamente mais tarde.</div>';
            console.error("DEBUG: Error:", error);
        }
    }

    // Carregamento inicial e ao mudar o rival
    document.addEventListener('DOMContentLoaded', loadChart);
    document.getElementById('rival').addEventListener('change', loadChart);
</script>
