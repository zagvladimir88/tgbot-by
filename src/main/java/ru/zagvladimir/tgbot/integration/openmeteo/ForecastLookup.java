package ru.zagvladimir.tgbot.integration.openmeteo;

import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.domain.weather.model.CurrentWeather;
import ru.zagvladimir.tgbot.domain.weather.model.DailyForecast;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;
import ru.zagvladimir.tgbot.domain.weather.model.Place;
import ru.zagvladimir.tgbot.domain.weather.model.WeatherCondition;
import ru.zagvladimir.tgbot.integration.openmeteo.dto.ForecastResponse;

@Service
public class ForecastLookup {

    private static final String CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m";
    private static final String DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min";
    private static final int FORECAST_DAYS = 4;

    private final OpenMeteoForecastApi api;

    ForecastLookup(OpenMeteoForecastApi api) {
        this.api = api;
    }

    @Cacheable(cacheNames = "forecast")
    public Forecast forecastAt(Place place) {
        var response =
                api.forecast(place.latitude(), place.longitude(), CURRENT_FIELDS, DAILY_FIELDS, "auto", FORECAST_DAYS);

        return new Forecast(place, current(response), daily(response));
    }

    private static CurrentWeather current(ForecastResponse response) {
        var current = response.current();
        return new CurrentWeather(
                current.temperature(),
                current.apparentTemperature(),
                current.humidity(),
                current.windSpeed(),
                WeatherCondition.fromWmoCode(current.weatherCode()));
    }

    private static List<DailyForecast> daily(ForecastResponse response) {
        var daily = response.daily();
        var days = new ArrayList<DailyForecast>(daily.time().size());

        for (var i = 0; i < daily.time().size(); i++) {
            days.add(new DailyForecast(
                    daily.time().get(i),
                    daily.minTemperature().get(i),
                    daily.maxTemperature().get(i),
                    WeatherCondition.fromWmoCode(daily.weatherCode().get(i))));
        }

        return List.copyOf(days);
    }
}
