package ru.zagvladimir.tgbot.telegram;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BotContextTest {

    @Test
    void contextIsNotBoundOutsideOfScope() {
        assertThat(BotContext.current()).isNull();
    }

    @Test
    void contextIsVisibleInsideScopeAndGoneAfterwards() {
        var context = new BotContext(1, 100L, 42L);

        ScopedValue.where(BotContext.CURRENT, context).run(() -> {
            assertThat(BotContext.current()).isEqualTo(context);
        });

        assertThat(BotContext.current()).isNull();
    }

    @Test
    void inlineQueryHasNoChat() {
        var context = BotContext.of(5, new BotRequest.Inline("q", 42L, "ghbdtn"));

        assertThat(context.chatId()).isNull();
        assertThat(context.userId()).isEqualTo(42L);
        assertThat(context.updateId()).isEqualTo(5);
    }

    @Test
    void commandCarriesChatAndUser() {
        var context = BotContext.of(6, new BotRequest.Command(100L, 42L, "/w", "Минск", null, false));

        assertThat(context.chatId()).isEqualTo(100L);
        assertThat(context.userId()).isEqualTo(42L);
    }
}
