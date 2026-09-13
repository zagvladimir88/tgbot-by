package ru.zagvladimir.tgbot.integration.openmeteo;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.zagvladimir.tgbot.integration.openmeteo.dto.ForecastResponse;

@HttpExchange
public interface OpenMeteoForecastApi {

    @GetExchange("/v1/forecast")
    public ForecastResponse forecast(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam("current") String currentFields,
            @RequestParam("daily") String dailyFields,
            @RequestParam String timezone,
            @RequestParam("forecast_days") int forecastDays);
}
