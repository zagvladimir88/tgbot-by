package ru.zagvladimir.tgbot.telegram.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.zagvladimir.tgbot.subscription.RateAlertCondition;

class AlertRequestTest {

    @Test
    void parsesAboveThreshold() {
        assertThat(AlertRequest.parse("usd > 3.30")).get().satisfies(request -> {
            assertThat(request.currency()).isEqualTo("USD");
            assertThat(request.condition()).isEqualTo(RateAlertCondition.ABOVE);
            assertThat(request.threshold()).isEqualByComparingTo("3.30");
        });
    }

    @Test
    void parsesBelowThreshold() {
        assertThat(AlertRequest.parse("eur < 3.80")).get().satisfies(request -> assertThat(request.condition())
                .isEqualTo(RateAlertCondition.BELOW));
    }

    @Test
    void understandsRussianWording() {
        assertThat(AlertRequest.parse("usd больше 3.3")).get().satisfies(request -> assertThat(request.condition())
                .isEqualTo(RateAlertCondition.ABOVE));

        assertThat(AlertRequest.parse("usd меньше 3.3")).get().satisfies(request -> assertThat(request.condition())
                .isEqualTo(RateAlertCondition.BELOW));
    }

    @Test
    void parsesPercentChange() {
        assertThat(AlertRequest.parse("usd change 1")).get().satisfies(request -> {
            assertThat(request.condition()).isEqualTo(RateAlertCondition.CHANGE_PERCENT);
            assertThat(request.threshold()).isEqualByComparingTo("1");
        });

        assertThat(AlertRequest.parse("usd изменение > 1,5%")).get().satisfies(request -> {
            assertThat(request.condition()).isEqualTo(RateAlertCondition.CHANGE_PERCENT);
            assertThat(request.threshold()).isEqualByComparingTo("1.5");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "usd", "> 3.30", "dollar > 3.30", "usd >", "usd > много"})
    void rejectsGarbage(String input) {
        assertThat(AlertRequest.parse(input)).isEmpty();
    }
}
