package ru.zagvladimir.tgbot.integration.nbrb;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.zagvladimir.tgbot.integration.nbrb.dto.NbrbCurrencyDto;
import ru.zagvladimir.tgbot.integration.nbrb.dto.NbrbDynamicsDto;
import ru.zagvladimir.tgbot.integration.nbrb.dto.NbrbRateDto;

@HttpExchange("/exrates")
public interface NbrbApi {

    @GetExchange("/rates")
    public List<NbrbRateDto> dailyRates(@RequestParam int periodicity);

    @GetExchange("/currencies")
    public List<NbrbCurrencyDto> currencies();

    @GetExchange("/rates/dynamics/{curId}")
    public List<NbrbDynamicsDto> dynamics(
            @PathVariable int curId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
