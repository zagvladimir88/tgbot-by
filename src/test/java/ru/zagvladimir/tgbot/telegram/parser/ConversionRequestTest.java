package ru.zagvladimir.tgbot.telegram.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConversionRequestTest {

    @Test
    void parsesAmountWithTwoCurrencies() {
        var request = ConversionRequest.parse("100 usd eur", "BYN");

        assertThat(request).get().satisfies(parsed -> {
            assertThat(parsed.amount()).isEqualByComparingTo("100");
            assertThat(parsed.from()).isEqualTo("USD");
            assertThat(parsed.to()).isEqualTo("EUR");
        });
    }

    @Test
    void fallsBackToBaseCurrencyWhenTargetOmitted() {
        var request = ConversionRequest.parse("100 usd", "BYN");

        assertThat(request).get().satisfies(parsed -> {
            assertThat(parsed.from()).isEqualTo("USD");
            assertThat(parsed.to()).isEqualTo("BYN");
        });
    }

    @Test
    void acceptsAmountGluedToCurrency() {
        assertThat(ConversionRequest.parse("100usd eur", "BYN")).get().satisfies(parsed -> assertThat(parsed.from())
                .isEqualTo("USD"));
    }

    @Test
    void acceptsBothDecimalSeparators() {
        assertThat(ConversionRequest.parse("10.5 usd", "BYN")).get().satisfies(parsed -> assertThat(parsed.amount())
                .isEqualByComparingTo("10.5"));

        assertThat(ConversionRequest.parse("10,5 usd", "BYN")).get().satisfies(parsed -> assertThat(parsed.amount())
                .isEqualByComparingTo("10.5"));
    }

    @Test
    void acceptsNaturalLanguageSeparator() {
        assertThat(ConversionRequest.parse("100 usd в eur", "BYN")).get().satisfies(parsed -> assertThat(parsed.to())
                .isEqualTo("EUR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "usd eur", "100", "сто usd eur", "100 dollars euro", "100 usd eur rub"})
    void rejectsGarbageInput(String input) {
        assertThat(ConversionRequest.parse(input, "BYN")).isEmpty();
    }
}
