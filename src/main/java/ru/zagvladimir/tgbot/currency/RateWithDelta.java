package ru.zagvladimir.tgbot.currency;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record RateWithDelta(CurrencyRate rate, @Nullable BigDecimal delta) {

    public boolean grew() {
        return delta != null && delta.signum() > 0;
    }

    public boolean fell() {
        return delta != null && delta.signum() < 0;
    }
}
