package ru.zagvladimir.tgbot.weather.internal;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
interface OpenMeteoForecastApi {

    @GetExchange("/v1/forecast")
    ForecastResponse forecast(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam("current") String currentFields,
            @RequestParam("daily") String dailyFields,
            @RequestParam String timezone,
            @RequestParam("forecast_days") int forecastDays);
}
