package ru.zagvladimir.tgbot.weather;

public record CurrentWeather(
        double temperature, double apparentTemperature, int humidity, double windSpeed, WeatherCondition condition) {}
