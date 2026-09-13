package ru.zagvladimir.tgbot.telegram.internal;

import org.jspecify.annotations.Nullable;

public record CommandContext(
        long chatId, long userId, String command, String arguments, @Nullable String replyToText, boolean fromGroup) {

    static CommandContext from(BotRequest.Command command) {
        return new CommandContext(
                command.chatId(),
                command.userId(),
                command.command(),
                command.arguments(),
                command.replyToText(),
                command.fromGroup());
    }

    public boolean hasArguments() {
        return !arguments.isBlank();
    }

    public @Nullable String argumentsOrReply() {
        if (hasArguments()) {
            return arguments;
        }
        return replyToText;
    }
}
