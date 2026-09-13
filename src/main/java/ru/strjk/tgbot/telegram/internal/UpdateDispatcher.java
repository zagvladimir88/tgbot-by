package ru.strjk.tgbot.telegram.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
class UpdateDispatcher implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

    private final ObjectProvider<TelegramClient> telegramClient;

    UpdateDispatcher(ObjectProvider<TelegramClient> telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var message = update.getMessage();
        var text = message.getText().trim();
        if (!text.startsWith("/start")) {
            return;
        }

        send(message.getChatId(), "Привет. Бот на связи, команды появятся по мере разработки.");
    }

    private void send(Long chatId, String text) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            log.warn("Нечем отправить сообщение в чат {}: TelegramClient не сконфигурирован", chatId);
            return;
        }

        try {
            client.execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }
}
