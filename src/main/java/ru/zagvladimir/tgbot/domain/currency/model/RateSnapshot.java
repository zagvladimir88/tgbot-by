package ru.zagvladimir.tgbot.domain.currency.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record RateSnapshot(LocalDate onDate, Instant fetchedAt, Map<String, CurrencyRate> rates) {

    public static final String BASE_CURRENCY = "BYN";

    public RateSnapshot {
        rates = Map.copyOf(rates);
    }

    public Optional<CurrencyRate> rate(String code) {
        return Optional.ofNullable(rates.get(code.toUpperCase(Locale.ROOT)));
    }

    public boolean supports(String code) {
        var normalized = code.toUpperCase(Locale.ROOT);
        return BASE_CURRENCY.equals(normalized) || rates.containsKey(normalized);
    }
}
