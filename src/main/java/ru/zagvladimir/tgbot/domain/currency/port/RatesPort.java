package ru.zagvladimir.tgbot.domain.currency.port;

import java.time.LocalDate;
import java.util.List;
import ru.zagvladimir.tgbot.domain.currency.model.RateHistoryPoint;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;

public interface RatesPort {

    RateSnapshot fetchDaily();

    List<RateHistoryPoint> history(String currencyCode, LocalDate startDate, LocalDate endDate);
}
