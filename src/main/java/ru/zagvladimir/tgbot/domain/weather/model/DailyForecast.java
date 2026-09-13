package ru.zagvladimir.tgbot.domain.weather.model;

import java.time.LocalDate;

public record DailyForecast(LocalDate date, double minTemperature, double maxTemperature, WeatherCondition condition) {}
