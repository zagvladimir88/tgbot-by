package ru.zagvladimir.tgbot.telegram.format;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.currency.model.RateHistoryPoint;

@Component
public class RateChartRenderer {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 500;
    private static final ZoneId ZONE = ZoneId.of("Europe/Minsk");

    public byte[] render(String code, List<RateHistoryPoint> points) {
        var series = new TimeSeries(code);
        for (var point : points) {
            series.addOrUpdate(new Day(Date.from(point.date().atStartOfDay(ZONE).toInstant())), point.ratePerUnit());
        }

        var chart = ChartFactory.createTimeSeriesChart(
                code + " / BYN", null, "BYN за 1 " + code, new TimeSeriesCollection(series), false, false, false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

        var plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        var renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, new Color(31, 119, 180));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        plot.setRenderer(renderer);

        ((DateAxis) plot.getDomainAxis()).setDateFormatOverride(new java.text.SimpleDateFormat("dd.MM"));

        var output = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsPNG(output, chart, WIDTH, HEIGHT);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось нарисовать график курса " + code, e);
        }

        return output.toByteArray();
    }
}
