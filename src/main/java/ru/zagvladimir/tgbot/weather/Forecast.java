package ru.zagvladimir.tgbot.weather;

import java.util.List;

public record Forecast(Place place, CurrentWeather current, List<DailyForecast> daily) {}
