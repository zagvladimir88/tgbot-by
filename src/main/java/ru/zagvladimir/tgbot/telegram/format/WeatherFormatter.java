package ru.zagvladimir.tgbot.telegram.format;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.weather.model.DailyForecast;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;

@Component
public class WeatherFormatter {

    private static final Locale RU = Locale.of("ru");

    public String format(Forecast forecast) {
        var current = forecast.current();
        var text = new StringBuilder();

        text.append(current.condition().emoji())
                .append(" <b>")
                .append(HtmlEscaper.escape(forecast.place().fullName()))
                .append("</b>")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        text.append("Сейчас: <b>")
                .append(temperature(current.temperature()))
                .append("</b>, ощущается ")
                .append(temperature(current.apparentTemperature()))
                .append(System.lineSeparator());

        text.append(current.condition().description())
                .append(", ветер ")
                .append(Math.round(current.windSpeed()))
                .append(" км/ч, влажность ")
                .append(current.humidity())
                .append("%")
                .append(System.lineSeparator());

        if (!forecast.daily().isEmpty()) {
            text.append(System.lineSeparator());
            var today = forecast.daily().getFirst().date();
            for (var day : forecast.daily()) {
                text.append(formatDay(day, today)).append(System.lineSeparator());
            }
        }

        return text.toString().strip();
    }

    private String formatDay(DailyForecast day, LocalDate today) {
        return "%s %s  %s … %s"
                .formatted(
                        dayLabel(day.date(), today),
                        day.condition().emoji(),
                        temperature(day.minTemperature()),
                        temperature(day.maxTemperature()));
    }

    private String dayLabel(LocalDate date, LocalDate today) {
        if (date.equals(today)) {
            return "Сегодня";
        }
        if (date.equals(today.plusDays(1))) {
            return "Завтра";
        }
        return "%s %02d.%02d"
                .formatted(
                        date.getDayOfWeek().getDisplayName(TextStyle.SHORT, RU),
                        date.getDayOfMonth(),
                        date.getMonthValue());
    }

    private static String temperature(double value) {
        var rounded = Math.round(value);
        return (rounded > 0 ? "+" : "") + rounded + "°";
    }
}
