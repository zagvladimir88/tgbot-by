package ru.zagvladimir.tgbot.telegram;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import ru.zagvladimir.tgbot.app.properties.BotProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BotProperties.class)
public class TelegramConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TelegramConfiguration.class);

    @Bean(destroyMethod = "shutdown")
    public ExecutorService botUpdateExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "bot.telegram", name = "token")
    public TelegramClient telegramClient(BotProperties properties) {
        return new OkHttpTelegramClient(properties.token());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "bot.telegram", name = "token")
    public TelegramBotsLongPollingApplication telegramBotsApplication(
            BotProperties properties, UpdateDispatcher dispatcher) throws TelegramApiException {
        var application = new TelegramBotsLongPollingApplication();
        application.registerBot(properties.token(), dispatcher);
        log.info("Long polling запущен");
        return application;
    }
}
