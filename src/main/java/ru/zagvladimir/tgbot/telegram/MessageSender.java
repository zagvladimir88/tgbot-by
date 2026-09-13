package ru.zagvladimir.tgbot.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final ObjectProvider<TelegramClient> telegramClient;

    MessageSender(ObjectProvider<TelegramClient> telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendText(long chatId, String text) {
        send(SendMessage.builder().chatId(chatId).text(text).build());
    }

    public void sendMarkdown(long chatId, String markdown) {
        send(SendMessage.builder()
                .chatId(chatId)
                .text(markdown)
                .parseMode("MarkdownV2")
                .build());
    }

    private void send(SendMessage message) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            log.warn("Сообщение в чат {} не отправлено: TelegramClient не сконфигурирован", message.getChatId());
            return;
        }

        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", message.getChatId(), e);
        }
    }
}
