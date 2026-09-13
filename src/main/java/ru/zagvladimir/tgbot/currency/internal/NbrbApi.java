package ru.zagvladimir.tgbot.currency.internal;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/exrates")
interface NbrbApi {

    @GetExchange("/rates")
    List<NbrbRateDto> dailyRates(@RequestParam int periodicity);

    @GetExchange("/currencies")
    List<NbrbCurrencyDto> currencies();

    @GetExchange("/rates/dynamics/{curId}")
    List<NbrbDynamicsDto> dynamics(
            @PathVariable int curId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
