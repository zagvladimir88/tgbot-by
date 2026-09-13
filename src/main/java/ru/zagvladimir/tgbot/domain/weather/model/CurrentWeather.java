package ru.zagvladimir.tgbot.domain.weather.model;

public record CurrentWeather(
        double temperature, double apparentTemperature, int humidity, double windSpeed, WeatherCondition condition) {}
