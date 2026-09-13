package ru.zagvladimir.tgbot.subscription.model;

import java.time.Instant;
import java.time.ZoneId;
import org.jspecify.annotations.Nullable;

public record Subscription(
        long id,
        long chatId,
        SubscriptionType type,
        String payload,
        String cron,
        ZoneId zoneId,
        Instant nextRunAt,
        @Nullable Instant lastRunAt,
        boolean enabled) {}
