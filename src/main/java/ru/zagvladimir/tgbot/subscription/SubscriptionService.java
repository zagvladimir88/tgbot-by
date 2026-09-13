package ru.zagvladimir.tgbot.subscription;

import java.time.ZoneId;
import java.util.List;

public interface SubscriptionService {

    Subscription subscribe(long chatId, long userId, SubscriptionType type, String payload, String cron, ZoneId zoneId);

    List<Subscription> forChat(long chatId);

    boolean unsubscribe(long chatId, long subscriptionId);
}
