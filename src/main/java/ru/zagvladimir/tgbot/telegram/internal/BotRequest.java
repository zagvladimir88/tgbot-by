package ru.zagvladimir.tgbot.telegram.internal;

import org.jspecify.annotations.Nullable;

sealed interface BotRequest {

    long userId();

    record Command(
            long chatId, long userId, String command, String arguments, @Nullable String replyToText, boolean fromGroup)
            implements BotRequest {}

    record Inline(String queryId, long userId, String query) implements BotRequest {}

    record Callback(String callbackId, long chatId, long userId, String data, int messageId) implements BotRequest {}
}
