package ru.zagvladimir.tgbot.weather.internal;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
interface OpenMeteoGeocodingApi {

    @GetExchange("/v1/search")
    GeocodingResponse search(
            @RequestParam String name,
            @RequestParam int count,
            @RequestParam String language,
            @RequestParam String format);
}
