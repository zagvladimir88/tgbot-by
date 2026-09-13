package ru.zagvladimir.tgbot.telegram.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.domain.currency.model.Conversion;
import ru.zagvladimir.tgbot.domain.currency.model.CurrencyRate;
import ru.zagvladimir.tgbot.domain.currency.model.RateWithDelta;

class CurrencyFormatterTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 13);

    private final CurrencyFormatter formatter = new CurrencyFormatter();

    @Test
    void showsUpArrowForGrownRate() {
        var text = formatter.formatRates(List.of(rate("USD", "3.2841", "0.0041")), DATE);

        assertThat(text).contains("13.09.2026");
        assertThat(text).contains("USD");
        assertThat(text).contains("3.2841");
        assertThat(text).contains("↑ +0.0041");
    }

    @Test
    void showsDownArrowForFallenRate() {
        var text = formatter.formatRates(List.of(rate("EUR", "3.8412", "-0.0021")), DATE);

        assertThat(text).contains("↓ -0.0021");
    }

    @Test
    void omitsArrowWhenRateDidNotChange() {
        var text = formatter.formatRates(List.of(rate("RUB", "0.0389", "0")), DATE);

        assertThat(text).doesNotContain("↑").doesNotContain("↓");
    }

    @Test
    void omitsArrowWhenPreviousDayIsUnknown() {
        var text = formatter.formatRates(List.of(rate("PLN", "0.9012", null)), DATE);

        assertThat(text).contains("0.9012").doesNotContain("↑").doesNotContain("↓");
    }

    @Test
    void explainsAbsenceOfRatesInsteadOfShowingEmptyMessage() {
        assertThat(formatter.formatRates(List.of(), DATE)).contains("недоступны");
    }

    @Test
    void roundsConversionResultToKopecks() {
        var conversion = new Conversion(new BigDecimal("100"), "USD", "EUR", new BigDecimal("85.49671977507029"), DATE);

        var text = formatter.formatConversion(conversion);

        assertThat(text).contains("100.00 USD");
        assertThat(text).contains("85.50 EUR");
        assertThat(text).contains("13.09.2026");
    }

    private static RateWithDelta rate(String code, String value, String delta) {
        return new RateWithDelta(
                new CurrencyRate(code, code, new BigDecimal(value)), delta == null ? null : new BigDecimal(delta));
    }
}
