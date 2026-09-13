package ru.zagvladimir.tgbot.integration.openmeteo;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.zagvladimir.tgbot.integration.openmeteo.dto.GeocodingResponse;

@HttpExchange
public interface OpenMeteoGeocodingApi {

    @GetExchange("/v1/search")
    public GeocodingResponse search(
            @RequestParam String name,
            @RequestParam int count,
            @RequestParam String language,
            @RequestParam String format);
}
