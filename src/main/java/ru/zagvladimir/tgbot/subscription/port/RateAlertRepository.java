package ru.zagvladimir.tgbot.subscription.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import ru.zagvladimir.tgbot.subscription.model.RateAlert;
import ru.zagvladimir.tgbot.subscription.model.RateAlertCondition;

public interface RateAlertRepository {

    RateAlert create(
            long chatId,
            long userId,
            String currency,
            RateAlertCondition condition,
            BigDecimal threshold,
            boolean oneShot);

    List<RateAlert> findActive();

    List<RateAlert> findByChat(long chatId);

    boolean delete(long chatId, long alertId);

    void markFired(long alertId, LocalDate onDate);
}
