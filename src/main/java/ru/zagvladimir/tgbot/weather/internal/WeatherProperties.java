package ru.zagvladimir.tgbot.weather.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.weather")
record WeatherProperties(String geocodingUrl, String forecastUrl, Duration timeout) {

    WeatherProperties {
        if (geocodingUrl == null || geocodingUrl.isBlank()) {
            geocodingUrl = "https://geocoding-api.open-meteo.com";
        }
        if (forecastUrl == null || forecastUrl.isBlank()) {
            forecastUrl = "https://api.open-meteo.com";
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(5);
        }
    }
}
