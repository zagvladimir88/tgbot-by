package ru.zagvladimir.tgbot.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.app.properties.BotProperties;

@Component
public class BotStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(BotStartupCheck.class);

    private final BotProperties properties;

    BotStartupCheck(BotProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    void warnAboutMissingToken() {
        if (!properties.hasToken()) {
            log.warn("bot.telegram.token не задан — приложение поднято без подключения к Telegram. "
                    + "Задайте переменную окружения BOT_TELEGRAM_TOKEN, чтобы бот начал принимать сообщения.");
        }
    }
}
