package ru.zagvladimir.tgbot.telegram.command;

import org.jspecify.annotations.Nullable;
import ru.zagvladimir.tgbot.telegram.BotRequest;

public record CommandContext(
        long chatId, long userId, String command, String arguments, @Nullable String replyToText, boolean fromGroup) {

    public static CommandContext from(BotRequest.Command command) {
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
