package ru.zagvladimir.tgbot.telegram.internal;

import org.jspecify.annotations.Nullable;

record BotContext(int updateId, @Nullable Long chatId, long userId) {

    static final ScopedValue<BotContext> CURRENT = ScopedValue.newInstance();

    static @Nullable BotContext current() {
        return CURRENT.isBound() ? CURRENT.get() : null;
    }

    static BotContext of(int updateId, BotRequest request) {
        return switch (request) {
            case BotRequest.Command command -> new BotContext(updateId, command.chatId(), command.userId());
            case BotRequest.Callback callback -> new BotContext(updateId, callback.chatId(), callback.userId());
            case BotRequest.Inline inline -> new BotContext(updateId, null, inline.userId());
        };
    }
}
