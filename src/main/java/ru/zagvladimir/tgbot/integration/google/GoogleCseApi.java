package ru.zagvladimir.tgbot.integration.google;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.zagvladimir.tgbot.integration.google.dto.GoogleCseResponse;

@HttpExchange
public interface GoogleCseApi {

    @GetExchange("/customsearch/v1")
    public GoogleCseResponse search(
            @RequestParam String key,
            @RequestParam String cx,
            @RequestParam String q,
            @RequestParam("searchType") String searchType,
            @RequestParam int num,
            @RequestParam int start,
            @RequestParam String safe);
}
