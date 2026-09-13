package ru.zagvladimir.tgbot.currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CurrencyService {

    Optional<RateSnapshot> latestRates();

    List<RateWithDelta> ratesWithDelta(List<String> codes);

    Optional<Conversion> convert(BigDecimal amount, String from, String to);

    List<RateHistoryPoint> history(String code, int days);
}
