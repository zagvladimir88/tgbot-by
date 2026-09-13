package ru.zagvladimir.tgbot.domain.currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import ru.zagvladimir.tgbot.domain.currency.model.Conversion;
import ru.zagvladimir.tgbot.domain.currency.model.RateHistoryPoint;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.domain.currency.model.RateWithDelta;

public interface CurrencyService {

    public Optional<RateSnapshot> latestRates();

    public List<RateWithDelta> ratesWithDelta(List<String> codes);

    public Optional<Conversion> convert(BigDecimal amount, String from, String to);

    public List<RateHistoryPoint> history(String code, int days);
}
