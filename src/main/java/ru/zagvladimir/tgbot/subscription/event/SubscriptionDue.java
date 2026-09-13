package ru.zagvladimir.tgbot.subscription.event;

import ru.zagvladimir.tgbot.subscription.model.SubscriptionType;

public record SubscriptionDue(long subscriptionId, long chatId, SubscriptionType type, String payload) {}
