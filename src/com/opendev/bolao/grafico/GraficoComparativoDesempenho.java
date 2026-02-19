package com.opendev.bolao.grafico;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;

public class GraficoComparativoDesempenho {

    private final TimeSeriesCollection dataset;

    public GraficoComparativoDesempenho(TimeSeriesCollection dataset) {
        if (dataset == null) {
            this.dataset = new TimeSeriesCollection();
        } else {
            this.dataset = dataset;
        }
    }

    public TimeSeriesCollection getDataset() {
        return this.dataset;
    }

    public JFreeChart criarChart() {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            null,
            "Data",
            "Pontos",
            this.dataset,
            true,
            false,
            false
        );

        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;
    }
}
