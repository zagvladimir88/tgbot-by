package ru.zagvladimir.tgbot.telegram;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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

    public void sendPhoto(long chatId, byte[] image, String fileName, String caption) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            log.warn("Картинка в чат {} не отправлена: TelegramClient не сконфигурирован", chatId);
            return;
        }

        try {
            client.execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(new java.io.ByteArrayInputStream(image), fileName))
                    .caption(caption)
                    .parseMode("HTML")
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить картинку в чат {}", chatId, e);
        }
    }

    public void sendPhotoByUrl(long chatId, String url, String caption, InlineKeyboardMarkup keyboard) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            log.warn("Картинка в чат {} не отправлена: TelegramClient не сконфигурирован", chatId);
            return;
        }

        try {
            client.execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(url))
                    .caption(caption)
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить картинку в чат {}", chatId, e);
        }
    }

    public void editPhotoByUrl(long chatId, int messageId, String url, String caption, InlineKeyboardMarkup keyboard) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            return;
        }

        try {
            client.execute(EditMessageMedia.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .media(InputMediaPhoto.builder()
                            .media(url)
                            .caption(caption)
                            .parseMode("HTML")
                            .build())
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не удалось обновить картинку в чате {}", chatId, e);
        }
    }

    public void answerCallback(String callbackId, String text) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            return;
        }

        try {
            client.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на callback {}", callbackId, e);
        }
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
