package ru.strjk.tgbot.telegram;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.strjk.tgbot.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TelegramConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void startsWithoutTelegramTokenConfigured() {
        assertThat(context.getBeanNamesForType(TelegramClient.class)).isEmpty();
        assertThat(context.getBeanNamesForType(TelegramBotsLongPollingApplication.class))
                .isEmpty();
    }
}
