package ru.zagvladimir.tgbot.subscription.port;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import ru.zagvladimir.tgbot.subscription.model.Subscription;
import ru.zagvladimir.tgbot.subscription.model.SubscriptionType;

public interface SubscriptionRepository {

    Subscription create(
            long chatId,
            long userId,
            SubscriptionType type,
            String payload,
            String cron,
            ZoneId zoneId,
            Instant nextRunAt);

    List<Subscription> findByChat(long chatId);

    boolean delete(long chatId, long subscriptionId);

    List<Subscription> lockDue(Instant now, int limit);

    void reschedule(long subscriptionId, Instant nextRunAt, Instant lastRunAt);
}
