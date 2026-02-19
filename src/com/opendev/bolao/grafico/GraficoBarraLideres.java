package com.opendev.bolao.grafico;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class GraficoBarraLideres {

    private final DefaultCategoryDataset dataset;

    public GraficoBarraLideres(DefaultCategoryDataset dataset) {
        if (dataset == null) {
            this.dataset = new DefaultCategoryDataset();
        } else {
            this.dataset = dataset;
        }
    }

    public DefaultCategoryDataset getDataset() {
        return this.dataset;
    }

    public JFreeChart criarChart() {
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Participante",
            "Pontos",
            this.dataset,
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );

        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));
        renderer.setMaximumBarWidth(0.15);

        return chart;
    }
}
