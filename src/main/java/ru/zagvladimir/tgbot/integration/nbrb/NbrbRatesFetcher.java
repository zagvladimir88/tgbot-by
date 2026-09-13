package ru.zagvladimir.tgbot.integration.nbrb;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.currency.model.CurrencyRate;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.integration.nbrb.dto.NbrbRateDto;

@Component
public class NbrbRatesFetcher {

    public static final MathContext PRECISION = MathContext.DECIMAL64;

    private final NbrbApi api;
    private final Clock clock;

    NbrbRatesFetcher(NbrbApi api, Clock clock) {
        this.api = api;
        this.clock = clock;
    }

    public RateSnapshot fetchDaily() {
        var rates = api.dailyRates(0);
        if (rates.isEmpty()) {
            throw new IllegalStateException("НБРБ вернул пустой список курсов");
        }

        var byCode = new LinkedHashMap<String, CurrencyRate>(rates.size());
        for (var dto : rates) {
            var code = dto.abbreviation().toUpperCase(Locale.ROOT);
            byCode.put(code, new CurrencyRate(code, dto.name(), normalize(dto)));
        }

        var onDate = rates.getFirst().date().toLocalDate();
        return new RateSnapshot(onDate, Instant.now(clock), byCode);
    }

    public static BigDecimal normalize(NbrbRateDto dto) {
        if (dto.scale() <= 0) {
            throw new IllegalStateException(
                    "Некорректный Cur_Scale для %s: %d".formatted(dto.abbreviation(), dto.scale()));
        }
        return dto.officialRate().divide(BigDecimal.valueOf(dto.scale()), PRECISION);
    }
}
