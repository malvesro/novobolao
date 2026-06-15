package com.opendev.bolao.grafico;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> getSeriesData() {
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            Map<String, Object> serie = new HashMap<>();
            serie.put("name", dataset.getSeries(i).getKey());
            List<Map<String, Object>> data = new ArrayList<>();
            for (Object item : dataset.getSeries(i).getItems()) {
                org.jfree.data.time.TimeSeriesDataItem dataItem = (org.jfree.data.time.TimeSeriesDataItem) item;
                Map<String, Object> point = new HashMap<>();
                point.put("x", dataItem.getPeriod().getStart().getTime());
                point.put("y", dataItem.getValue());
                data.add(point);
            }
            serie.put("data", data);
            series.add(serie);
        }
        return series;
    }

    public List<String> getCategories() {
        return Collections.emptyList();
    }
}
