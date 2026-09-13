package ru.zagvladimir.tgbot.telegram.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.domain.currency.model.CurrencyRate;
import ru.zagvladimir.tgbot.domain.currency.model.RateWithDelta;
import ru.zagvladimir.tgbot.subscription.model.RateAlert;
import ru.zagvladimir.tgbot.subscription.model.RateAlertCondition;

class RateAlertListenerTest {

    @Test
    void aboveTriggersOnlyWhenRateExceedsThreshold() {
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.ABOVE, "3.30"), rate("3.3100", null)))
                .isTrue();
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.ABOVE, "3.30"), rate("3.2900", null)))
                .isFalse();
    }

    @Test
    void thresholdItselfIsNotATrigger() {
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.ABOVE, "3.30"), rate("3.3000", null)))
                .isFalse();
    }

    @Test
    void belowTriggersOnlyWhenRateIsUnderThreshold() {
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.BELOW, "3.30"), rate("3.2900", null)))
                .isTrue();
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.BELOW, "3.30"), rate("3.3100", null)))
                .isFalse();
    }

    @Test
    void percentChangeComparesAgainstPreviousDay() {
        var grewByOnePercent = rate("3.3000", "0.0330");

        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.CHANGE_PERCENT, "1"), grewByOnePercent))
                .isTrue();
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.CHANGE_PERCENT, "2"), grewByOnePercent))
                .isFalse();
    }

    @Test
    void percentChangeWorksForFallingRateToo() {
        var fellByOnePercent = rate("3.2670", "-0.0330");

        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.CHANGE_PERCENT, "1"), fellByOnePercent))
                .isTrue();
    }

    @Test
    void percentChangeIsSilentWhenPreviousDayIsUnknown() {
        assertThat(RateAlertListener.triggered(alert(RateAlertCondition.CHANGE_PERCENT, "1"), rate("3.3000", null)))
                .isFalse();
    }

    private static RateAlert alert(RateAlertCondition condition, String threshold) {
        return new RateAlert(1L, 100L, "USD", condition, new BigDecimal(threshold), false, true, null);
    }

    private static RateWithDelta rate(String value, String delta) {
        return new RateWithDelta(
                new CurrencyRate("USD", "Доллар США", new BigDecimal(value)),
                delta == null ? null : new BigDecimal(delta));
    }
}
