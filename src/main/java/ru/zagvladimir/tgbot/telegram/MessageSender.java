package ru.zagvladimir.tgbot.telegram;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
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

    public void sendHtml(long chatId, String html) {
        send(SendMessage.builder().chatId(chatId).text(html).parseMode("HTML").build());
    }

    public void sendMarkdown(long chatId, String markdown) {
        send(SendMessage.builder()
                .chatId(chatId)
                .text(markdown)
                .parseMode("MarkdownV2")
                .build());
    }

    public void answerInlineQuery(String queryId, List<InlineQueryResult> results) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            log.warn("Ответ на inline-запрос {} не отправлен: TelegramClient не сконфигурирован", queryId);
            return;
        }

        try {
            client.execute(AnswerInlineQuery.builder()
                    .inlineQueryId(queryId)
                    .results(results)
                    .cacheTime(0)
                    .isPersonal(true)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на inline-запрос {}", queryId, e);
        }
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
