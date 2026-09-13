package ru.zagvladimir.tgbot.telegram.internal.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
class ChatAdminChecker {

    private static final Logger log = LoggerFactory.getLogger(ChatAdminChecker.class);

    private final ObjectProvider<TelegramClient> telegramClient;

    ChatAdminChecker(ObjectProvider<TelegramClient> telegramClient) {
        this.telegramClient = telegramClient;
    }

    boolean canManage(long chatId, long userId, boolean fromGroup) {
        if (!fromGroup) {
            return true;
        }

        var client = telegramClient.getIfAvailable();
        if (client == null) {
            return false;
        }

        try {
            var member = client.execute(
                    GetChatMember.builder().chatId(chatId).userId(userId).build());

            var status = member.getStatus();
            return "creator".equals(status) || "administrator".equals(status);
        } catch (TelegramApiException e) {
            log.warn("Не удалось проверить права пользователя {} в чате {}", userId, chatId, e);
            return false;
        }
    }
}
