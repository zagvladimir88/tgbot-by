package ru.zagvladimir.tgbot.domain.weather.model;

import java.util.List;

public record Forecast(Place place, CurrentWeather current, List<DailyForecast> daily) {}
