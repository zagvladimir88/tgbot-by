package ru.zagvladimir.tgbot.domain.currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.domain.currency.model.Conversion;
import ru.zagvladimir.tgbot.domain.currency.model.CurrencyRate;
import ru.zagvladimir.tgbot.domain.currency.model.RateHistoryPoint;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.domain.currency.model.RateWithDelta;
import ru.zagvladimir.tgbot.domain.currency.port.RateSnapshotRepository;
import ru.zagvladimir.tgbot.domain.currency.port.RatesPort;

@Service
public class DefaultCurrencyService implements CurrencyService {

    public static final MathContext PRECISION = MathContext.DECIMAL64;

    private final RateSnapshotRepository repository;
    private final RatesRefresher refresher;
    private final RatesPort ratesPort;
    private final Clock clock;

    DefaultCurrencyService(
            RateSnapshotRepository repository, RatesRefresher refresher, RatesPort ratesPort, Clock clock) {
        this.repository = repository;
        this.refresher = refresher;
        this.ratesPort = ratesPort;
        this.clock = clock;
    }

    @Override
    public Optional<RateSnapshot> latestRates() {
        var stored = repository.findLatest();
        return stored.isPresent() ? stored : refresher.refresh();
    }

    @Override
    public List<RateWithDelta> ratesWithDelta(List<String> codes) {
        var latest = latestRates().orElse(null);
        if (latest == null) {
            return List.of();
        }

        var previous = repository.findPrevious(latest.onDate()).orElse(null);
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

        var inBase = amount.multiply(rateOf(snapshot, source), PRECISION);
        var result = inBase.divide(rateOf(snapshot, target), PRECISION);

        return Optional.of(new Conversion(amount, source, target, result, snapshot.onDate()));
    }

    @Override
    public List<RateHistoryPoint> history(String code, int days) {
        var endDate = LocalDate.now(clock);
        return ratesPort.history(normalizeCode(code), endDate.minusDays(days), endDate);
    }

    private static BigDecimal rateOf(RateSnapshot snapshot, String code) {
        if (RateSnapshot.BASE_CURRENCY.equals(code)) {
            return BigDecimal.ONE;
        }
        return snapshot.rate(code).orElseThrow().ratePerUnit();
    }

    @Nullable
    private static BigDecimal deltaFor(@Nullable RateSnapshot previous, CurrencyRate rate) {
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
