package ru.zagvladimir.tgbot.telegram.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.zagvladimir.tgbot.telegram.MessageSender;

class UpdateDispatcherTest {

    private static final long CHAT_ID = 100L;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final MessageSender sender = mock(MessageSender.class);

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void routesCommandToItsHandler() {
        var seen = new AtomicReference<CommandContext>();
        var dispatcher = dispatcherWith(new RecordingHandler(seen, false));

        dispatcher.process(commandUpdate("/w Минск", "private"));

        assertThat(seen.get()).isNotNull();
        assertThat(seen.get().command()).isEqualTo("/w");
        assertThat(seen.get().arguments()).isEqualTo("Минск");
    }

    @Test
    void answersOnUnknownCommandInPrivateChat() {
        var dispatcher = dispatcherWith(new RecordingHandler(new AtomicReference<>(), false));

        dispatcher.process(commandUpdate("/nope", "private"));

        verify(sender).sendText(anyLong(), contains("/help"));
    }

    @Test
    void staysSilentOnUnknownCommandInGroup() {
        var dispatcher = dispatcherWith(new RecordingHandler(new AtomicReference<>(), false));

        dispatcher.process(commandUpdate("/nope", "supergroup"));

        verify(sender, never()).sendText(anyLong(), contains("/help"));
    }

    @Test
    void reportsFailureToUserWhenHandlerThrows() {
        var dispatcher = dispatcherWith(new RecordingHandler(new AtomicReference<>(), true));

        dispatcher.process(commandUpdate("/w Минск", "private"));

        verify(sender).sendText(anyLong(), contains("Что-то пошло не так"));
    }

    @Test
    void survivesUpdateWithoutMessage() {
        var dispatcher = dispatcherWith(new RecordingHandler(new AtomicReference<>(), false));

        dispatcher.process(new Update());

        verify(sender, never()).sendText(anyLong(), contains("Что-то пошло не так"));
    }

    private UpdateDispatcher dispatcherWith(CommandHandler handler) {
        return new UpdateDispatcher(executor, new CommandRegistry(List.of(handler)), sender);
    }

    private static Update commandUpdate(String text, String chatType) {
        var update = new Update();
        update.setUpdateId(1);
        update.setMessage(Message.builder()
                .messageId(7)
                .date(0)
                .chat(Chat.builder().id(CHAT_ID).type(chatType).build())
                .from(User.builder().id(42L).firstName("tester").isBot(false).build())
                .text(text)
                .build());
        return update;
    }

    private record RecordingHandler(AtomicReference<CommandContext> seen, boolean failing) implements CommandHandler {

        @Override
        public Set<String> commands() {
            return Set.of("/w");
        }

        @Override
        public String description() {
            return "погода";
        }

        @Override
        public void handle(CommandContext context) {
            if (failing) {
                throw new IllegalStateException("тестовый сбой");
            }
            seen.set(context);
        }
    }
}
