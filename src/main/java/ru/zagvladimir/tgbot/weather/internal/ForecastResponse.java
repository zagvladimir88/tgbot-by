package ru.zagvladimir.tgbot.weather.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

record ForecastResponse(@Nullable String timezone, Current current, Daily daily) {

    record Current(
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("apparent_temperature") double apparentTemperature,
            @JsonProperty("relative_humidity_2m") int humidity,
            @JsonProperty("weather_code") int weatherCode,
            @JsonProperty("wind_speed_10m") double windSpeed) {}

    record Daily(
            List<LocalDate> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> maxTemperature,
            @JsonProperty("temperature_2m_min") List<Double> minTemperature) {}
}
