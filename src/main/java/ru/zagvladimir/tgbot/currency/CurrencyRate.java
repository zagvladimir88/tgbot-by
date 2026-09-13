package ru.zagvladimir.tgbot.currency;

import java.math.BigDecimal;

public record CurrencyRate(String code, String name, BigDecimal ratePerUnit) {

    public CurrencyRate {
        if (ratePerUnit.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Курс %s должен быть положительным, получено %s".formatted(code, ratePerUnit));
        }
    }
}
