package ru.zagvladimir.tgbot.subscription;

public record SubscriptionDue(long subscriptionId, long chatId, SubscriptionType type, String payload) {}
