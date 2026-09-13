package ru.zagvladimir.tgbot.telegram.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

class BotRequestsTest {

    private static final long CHAT_ID = 100L;
    private static final long USER_ID = 42L;

    @Test
    void parsesCommandWithArguments() {
        var request = BotRequests.from(messageUpdate("/w Минск", "private"));

        assertThat(request).get().isInstanceOfSatisfying(BotRequest.Command.class, command -> {
            assertThat(command.command()).isEqualTo("/w");
            assertThat(command.arguments()).isEqualTo("Минск");
            assertThat(command.chatId()).isEqualTo(CHAT_ID);
            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.fromGroup()).isFalse();
        });
    }

    @Test
    void stripsBotMentionFromCommand() {
        var request = BotRequests.from(messageUpdate("/w@tgbot_by_bot Гродно", "supergroup"));

        assertThat(request).get().isInstanceOfSatisfying(BotRequest.Command.class, command -> {
            assertThat(command.command()).isEqualTo("/w");
            assertThat(command.arguments()).isEqualTo("Гродно");
            assertThat(command.fromGroup()).isTrue();
        });
    }

    @Test
    void parsesCommandWithoutArguments() {
        var request = BotRequests.from(messageUpdate("/start", "private"));

        assertThat(request).get().isInstanceOfSatisfying(BotRequest.Command.class, command -> {
            assertThat(command.command()).isEqualTo("/start");
            assertThat(command.arguments()).isEmpty();
            assertThat(command.replyToText()).isNull();
        });
    }

    @Test
    void lowercasesCommand() {
        var request = BotRequests.from(messageUpdate("/START", "private"));

        assertThat(request).get().isInstanceOfSatisfying(BotRequest.Command.class, command -> assertThat(
                        command.command())
                .isEqualTo("/start"));
    }

    @Test
    void keepsRepliedTextForLayoutConversion() {
        var update = messageUpdate("/kb", "private");
        var replied = Message.builder()
                .messageId(6)
                .date(0)
                .chat(Chat.builder().id(CHAT_ID).type("private").build())
                .text("ghbdtn")
                .build();
        update.getMessage().setReplyToMessage(replied);

        var request = BotRequests.from(update);

        assertThat(request).get().isInstanceOfSatisfying(BotRequest.Command.class, command -> assertThat(
                        command.replyToText())
                .isEqualTo("ghbdtn"));
    }

    @Test
    void ignoresPlainTextThatIsNotACommand() {
        assertThat(BotRequests.from(messageUpdate("просто сообщение", "private")))
                .isEmpty();
    }

    @Test
    void ignoresUpdateWithoutAnythingUseful() {
        assertThat(BotRequests.from(new Update())).isEmpty();
    }

    @Test
    void parsesInlineQuery() {
        var update = new Update();
        var inline = InlineQuery.builder()
                .id("inline-1")
                .from(user())
                .query("  ghbdtn  ")
                .offset("")
                .build();
        update.setInlineQuery(inline);

        assertThat(BotRequests.from(update)).get().isInstanceOfSatisfying(BotRequest.Inline.class, query -> {
            assertThat(query.queryId()).isEqualTo("inline-1");
            assertThat(query.query()).isEqualTo("ghbdtn");
        });
    }

    @Test
    void parsesCallbackQuery() {
        var update = new Update();
        var callback = new CallbackQuery();
        callback.setId("cb-1");
        callback.setFrom(user());
        callback.setData("img:7");
        callback.setMessage(message("предыдущее", "private"));
        update.setCallbackQuery(callback);

        assertThat(BotRequests.from(update)).get().isInstanceOfSatisfying(BotRequest.Callback.class, cb -> {
            assertThat(cb.data()).isEqualTo("img:7");
            assertThat(cb.chatId()).isEqualTo(CHAT_ID);
            assertThat(cb.callbackId()).isEqualTo("cb-1");
        });
    }

    private static Update messageUpdate(String text, String chatType) {
        var update = new Update();
        update.setUpdateId(1);
        update.setMessage(message(text, chatType));
        return update;
    }

    private static Message message(String text, String chatType) {
        return Message.builder()
                .messageId(7)
                .date(0)
                .chat(Chat.builder().id(CHAT_ID).type(chatType).build())
                .from(user())
                .text(text)
                .build();
    }

    private static User user() {
        return User.builder().id(USER_ID).firstName("tester").isBot(false).build();
    }
}
