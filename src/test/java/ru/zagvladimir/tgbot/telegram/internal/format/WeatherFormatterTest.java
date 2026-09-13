package ru.zagvladimir.tgbot.telegram.internal.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.weather.CurrentWeather;
import ru.zagvladimir.tgbot.weather.DailyForecast;
import ru.zagvladimir.tgbot.weather.Forecast;
import ru.zagvladimir.tgbot.weather.Place;
import ru.zagvladimir.tgbot.weather.WeatherCondition;

class WeatherFormatterTest {

    private final WeatherFormatter formatter = new WeatherFormatter();

    @Test
    void rendersPlaceCurrentConditionsAndDays() {
        var text = formatter.format(forecast("Минск", "Беларусь"));

        assertThat(text).contains("<b>Минск, Беларусь</b>");
        assertThat(text).contains("+18°");
        assertThat(text).contains("Пасмурно");
        assertThat(text).contains("влажность 50%");
        assertThat(text).contains("Сегодня");
        assertThat(text).contains("Завтра");
    }

    @Test
    void escapesHtmlInPlaceNameSoMarkupCannotBreak() {
        var text = formatter.format(forecast("<b>Минск", "Беларусь"));

        assertThat(text).contains("&lt;b&gt;Минск");
        assertThat(text).doesNotContain("<b><b>Минск");
    }

    @Test
    void marksNegativeTemperaturesWithoutPlusSign() {
        var forecast = new Forecast(
                place("Минск", "Беларусь"),
                new CurrentWeather(-7.4, -12.0, 80, 5.0, WeatherCondition.SNOW),
                List.of(new DailyForecast(LocalDate.of(2026, 1, 10), -12.0, -5.0, WeatherCondition.SNOW)));

        var text = formatter.format(forecast);

        assertThat(text).contains("-7°");
        assertThat(text).doesNotContain("+-");
    }

    private static Forecast forecast(String city, String country) {
        return new Forecast(
                place(city, country),
                new CurrentWeather(17.9, 16.0, 50, 8.6, WeatherCondition.OVERCAST),
                List.of(
                        new DailyForecast(LocalDate.of(2026, 9, 13), 10.8, 18.7, WeatherCondition.OVERCAST),
                        new DailyForecast(LocalDate.of(2026, 9, 14), 12.8, 16.6, WeatherCondition.DRIZZLE),
                        new DailyForecast(LocalDate.of(2026, 9, 15), 11.0, 19.0, WeatherCondition.CLEAR)));
    }

    private static Place place(String city, String country) {
        return new Place(city, country, city, 53.9, 27.57, ZoneId.of("Europe/Minsk"));
    }
}
