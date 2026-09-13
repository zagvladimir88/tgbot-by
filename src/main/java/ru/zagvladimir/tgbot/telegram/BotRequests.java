package ru.zagvladimir.tgbot.telegram;

import java.util.Optional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public final class BotRequests {

    private BotRequests() {}

    public static Optional<BotRequest> from(Update update) {
        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            var message = callback.getMessage();
            if (message == null || callback.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(new BotRequest.Callback(
                    callback.getId(),
                    message.getChatId(),
                    callback.getFrom().getId(),
                    callback.getData(),
                    message.getMessageId()));
        }

        if (update.hasInlineQuery()) {
            var inline = update.getInlineQuery();
            return Optional.of(new BotRequest.Inline(
                    inline.getId(), inline.getFrom().getId(), inline.getQuery().trim()));
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            return fromMessage(update.getMessage());
        }

        return Optional.empty();
    }

    private static Optional<BotRequest> fromMessage(Message message) {
        var text = message.getText().trim();
        if (!text.startsWith("/")) {
            return Optional.empty();
        }

        var separator = indexOfFirstWhitespace(text);
        var rawCommand = separator < 0 ? text : text.substring(0, separator);
        var arguments = separator < 0 ? "" : text.substring(separator + 1).trim();

        var mention = rawCommand.indexOf('@');
        var command = mention < 0 ? rawCommand : rawCommand.substring(0, mention);

        var replyTo = message.getReplyToMessage();
        var replyToText = replyTo == null ? null : replyTo.getText();

        var chat = message.getChat();
        var fromGroup = chat != null && (chat.isGroupChat() || chat.isSuperGroupChat());

        return Optional.of(new BotRequest.Command(
                message.getChatId(),
                message.getFrom().getId(),
                command.toLowerCase(),
                arguments,
                replyToText,
                fromGroup));
    }

    private static int indexOfFirstWhitespace(String text) {
        for (var i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
