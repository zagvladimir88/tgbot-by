package ru.zagvladimir.tgbot.image.internal;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
interface GoogleCseApi {

    @GetExchange("/customsearch/v1")
    GoogleCseResponse search(
            @RequestParam String key,
            @RequestParam String cx,
            @RequestParam String q,
            @RequestParam("searchType") String searchType,
            @RequestParam int num,
            @RequestParam int start,
            @RequestParam String safe);
}
