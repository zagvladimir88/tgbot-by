package ru.zagvladimir.tgbot.currency.internal;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.currency.Conversion;
import ru.zagvladimir.tgbot.currency.CurrencyRate;
import ru.zagvladimir.tgbot.currency.CurrencyService;
import ru.zagvladimir.tgbot.currency.RateHistoryPoint;
import ru.zagvladimir.tgbot.currency.RateSnapshot;
import ru.zagvladimir.tgbot.currency.RateWithDelta;

@Service
class NbrbCurrencyService implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(NbrbCurrencyService.class);

    private final RateSnapshotStore store;
    private final RatesRefresher refresher;
    private final CurrencyDirectory directory;
    private final NbrbApi api;
    private final Clock clock;

    NbrbCurrencyService(
            RateSnapshotStore store, RatesRefresher refresher, CurrencyDirectory directory, NbrbApi api, Clock clock) {
        this.store = store;
        this.refresher = refresher;
        this.directory = directory;
        this.api = api;
        this.clock = clock;
    }

    @Override
    public Optional<RateSnapshot> latestRates() {
        var stored = store.findLatest();
        return stored.isPresent() ? stored : refresher.refresh();
    }

    @Override
    public List<RateWithDelta> ratesWithDelta(List<String> codes) {
        var latest = latestRates().orElse(null);
        if (latest == null) {
            return List.of();
        }

        var previous = store.findPrevious(latest.onDate()).orElse(null);
        var result = new ArrayList<RateWithDelta>(codes.size());

        for (var code : codes) {
            latest.rate(code).ifPresent(rate -> result.add(new RateWithDelta(rate, deltaFor(previous, rate))));
        }

        return List.copyOf(result);
    }

    @Override
    public Optional<Conversion> convert(BigDecimal amount, String from, String to) {
        var snapshot = latestRates().orElse(null);
        if (snapshot == null) {
            return Optional.empty();
        }

        var source = normalizeCode(from);
        var target = normalizeCode(to);
        if (!snapshot.supports(source) || !snapshot.supports(target)) {
            return Optional.empty();
        }

        var inBase = amount.multiply(rateOf(snapshot, source), NbrbRatesFetcher.PRECISION);
        var result = inBase.divide(rateOf(snapshot, target), NbrbRatesFetcher.PRECISION);

        return Optional.of(new Conversion(amount, source, target, result, snapshot.onDate()));
    }

    @Override
    public List<RateHistoryPoint> history(String code, int days) {
        var normalized = normalizeCode(code);
        var currency = directory.pick(directory.all(), normalized).orElse(null);
        if (currency == null) {
            return List.of();
        }

        var endDate = LocalDate.now(clock);
        var startDate = endDate.minusDays(days);

        try {
            return api.dynamics(currency.curId(), startDate, endDate).stream()
                    .filter(point -> point.officialRate() != null)
                    .map(point -> new RateHistoryPoint(
                            point.date().toLocalDate(),
                            point.officialRate()
                                    .divide(BigDecimal.valueOf(currency.scale()), NbrbRatesFetcher.PRECISION)))
                    .toList();
        } catch (Exception e) {
            log.warn("Не удалось получить историю курса {}: {}", normalized, e.toString());
            return List.of();
        }
    }

    private static BigDecimal rateOf(RateSnapshot snapshot, String code) {
        if (RateSnapshot.BASE_CURRENCY.equals(code)) {
            return BigDecimal.ONE;
        }
        return snapshot.rate(code).orElseThrow().ratePerUnit();
    }

    private static BigDecimal deltaFor(RateSnapshot previous, CurrencyRate rate) {
        if (previous == null) {
            return null;
        }
        return previous.rate(rate.code())
                .map(old -> rate.ratePerUnit().subtract(old.ratePerUnit()))
                .orElse(null);
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
