package ru.zagvladimir.tgbot.integration.nbrb;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import ru.zagvladimir.tgbot.domain.currency.model.RateHistoryPoint;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.domain.currency.port.RatesPort;

@Component
@Retryable(
        includes = {ResourceAccessException.class, HttpServerErrorException.class},
        maxRetries = 2,
        delay = 300,
        multiplier = 2.0,
        jitter = 100)
public class NbrbRatesAdapter implements RatesPort {

    private static final Logger log = LoggerFactory.getLogger(NbrbRatesAdapter.class);

    private final NbrbRatesFetcher fetcher;
    private final CurrencyDirectory directory;
    private final NbrbApi api;

    NbrbRatesAdapter(NbrbRatesFetcher fetcher, CurrencyDirectory directory, NbrbApi api) {
        this.fetcher = fetcher;
        this.directory = directory;
        this.api = api;
    }

    @Override
    public RateSnapshot fetchDaily() {
        return fetcher.fetchDaily();
    }

    @Override
    public List<RateHistoryPoint> history(String currencyCode, LocalDate startDate, LocalDate endDate) {
        var code = currencyCode.toUpperCase(Locale.ROOT);
        var currency = directory.pick(directory.all(), code).orElse(null);
        if (currency == null) {
            return List.of();
        }

        try {
            return api.dynamics(currency.curId(), startDate, endDate).stream()
                    .filter(point -> point.officialRate() != null)
                    .map(point -> new RateHistoryPoint(
                            point.date().toLocalDate(),
                            point.officialRate()
                                    .divide(BigDecimal.valueOf(currency.scale()), NbrbRatesFetcher.PRECISION)))
                    .toList();
        } catch (Exception e) {
            log.warn("Не удалось получить историю курса {}: {}", code, e.toString());
            return List.of();
        }
    }
}
