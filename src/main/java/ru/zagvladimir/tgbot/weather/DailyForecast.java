package ru.zagvladimir.tgbot.weather;

import java.time.LocalDate;

public record DailyForecast(LocalDate date, double minTemperature, double maxTemperature, WeatherCondition condition) {}
