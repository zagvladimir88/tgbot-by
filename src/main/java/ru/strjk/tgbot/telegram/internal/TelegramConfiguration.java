package ru.strjk.tgbot.telegram.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.strjk.tgbot.telegram.BotProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BotProperties.class)
class TelegramConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TelegramConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "bot.telegram", name = "token")
    TelegramClient telegramClient(BotProperties properties) {
        return new OkHttpTelegramClient(properties.token());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "bot.telegram", name = "token")
    TelegramBotsLongPollingApplication telegramBotsApplication(BotProperties properties, UpdateDispatcher dispatcher)
            throws TelegramApiException {
        var application = new TelegramBotsLongPollingApplication();
        application.registerBot(properties.token(), dispatcher);
        log.info("Long polling запущен");
        return application;
    }

    @Bean
    BotStartupCheck botStartupCheck(BotProperties properties) {
        return new BotStartupCheck(properties);
    }

    record BotStartupCheck(BotProperties properties) {

        BotStartupCheck {
            if (properties == null || !properties.hasToken()) {
                log.warn("bot.telegram.token не задан — приложение поднято без подключения к Telegram. "
                        + "Задайте переменную окружения BOT_TELEGRAM_TOKEN, чтобы бот начал принимать сообщения.");
            }
        }
    }
}
