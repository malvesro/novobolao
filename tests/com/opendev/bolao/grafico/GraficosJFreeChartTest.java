package com.opendev.bolao.grafico;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.jfree.chart.ChartUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Valida a geração dos gráficos migrados do Cewolf para JFreeChart puro.
 * Garante que os charts são criados sem exceções e que a serialização em PNG funciona.
 */
class GraficosJFreeChartTest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("GraficoComparativoDesempenho deve gerar PNG com dados de série temporal")
    void deveGerarGraficoDeDesempenho() throws Exception {
        TimeSeries serieParticipante = new TimeSeries("Participante A");
        LocalDate dataBase = LocalDate.of(2026, 2, 1);
        serieParticipante.add(new Day(dataBase.getDayOfMonth(), dataBase.getMonthValue(), dataBase.getYear()), 5);
        serieParticipante.add(new Day(dataBase.plusDays(1).getDayOfMonth(), dataBase.plusDays(1).getMonthValue(),
                dataBase.plusDays(1).getYear()), 7);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(serieParticipante);

        GraficoComparativoDesempenho grafico = new GraficoComparativoDesempenho(dataset);
        var chart = grafico.criarChart();
        assertThat(chart).as("Instância do gráfico não pode ser nula").isNotNull();

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(png, chart, 560, 240);

        assertThat(png.size()).as("Renderização PNG deve gerar dados").isGreaterThan(0);
    }

    @Test
    @DisplayName("GraficoBarraLideres deve gerar PNG com dados categóricos")
    void deveGerarGraficoDeLideranca() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(12, "Participante A", "Participante A");
        dataset.addValue(10, "Participante B", "Participante B");

        GraficoBarraLideres grafico = new GraficoBarraLideres(dataset);
        var chart = grafico.criarChart();
        assertThat(chart).as("Instância do gráfico não pode ser nula").isNotNull();

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(png, chart, 560, 180);

        assertThat(png.size()).as("Renderização PNG deve gerar dados").isGreaterThan(0);
    }
}
