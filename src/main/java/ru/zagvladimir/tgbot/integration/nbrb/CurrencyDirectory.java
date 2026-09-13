package ru.zagvladimir.tgbot.integration.nbrb;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.integration.nbrb.dto.NbrbCurrencyDto;

@Component
public class CurrencyDirectory {

    private final NbrbApi api;
    private final Clock clock;

    CurrencyDirectory(NbrbApi api, Clock clock) {
        this.api = api;
        this.clock = clock;
    }

    @Cacheable(cacheNames = "currency-directory")
    public List<NbrbCurrencyDto> all() {
        return api.currencies();
    }

    public Optional<NbrbCurrencyDto> pick(List<NbrbCurrencyDto> currencies, String code) {
        var now = LocalDateTime.now(clock);

        return currencies.stream()
                .filter(currency -> currency.abbreviation() != null)
                .filter(currency -> currency.abbreviation().equalsIgnoreCase(code))
                .filter(currency ->
                        currency.dateEnd() == null || currency.dateEnd().isAfter(now))
                .max(Comparator.comparing(NbrbCurrencyDto::curId));
    }
}
