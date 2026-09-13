package ru.zagvladimir.tgbot.telegram;

import org.jspecify.annotations.Nullable;

public sealed interface BotRequest {

    public long userId();

    public record Command(
            long chatId, long userId, String command, String arguments, @Nullable String replyToText, boolean fromGroup)
            implements BotRequest {}

    public record Inline(String queryId, long userId, String query) implements BotRequest {}

    public record Callback(String callbackId, long chatId, long userId, String data, int messageId)
            implements BotRequest {}
}
