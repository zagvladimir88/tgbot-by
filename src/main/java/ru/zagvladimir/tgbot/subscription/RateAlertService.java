package ru.zagvladimir.tgbot.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RateAlertService {

    RateAlert create(
            long chatId,
            long userId,
            String currency,
            RateAlertCondition condition,
            BigDecimal threshold,
            boolean oneShot);

    List<RateAlert> activeAlerts();

    List<RateAlert> forChat(long chatId);

    boolean remove(long chatId, long alertId);

    void markFired(long alertId, LocalDate onDate);
}
