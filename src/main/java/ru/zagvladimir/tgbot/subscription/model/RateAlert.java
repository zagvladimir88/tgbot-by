package ru.zagvladimir.tgbot.subscription.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record RateAlert(
        long id,
        long chatId,
        String currency,
        RateAlertCondition condition,
        BigDecimal threshold,
        boolean oneShot,
        boolean enabled,
        @Nullable LocalDate lastFiredOnDate) {}
