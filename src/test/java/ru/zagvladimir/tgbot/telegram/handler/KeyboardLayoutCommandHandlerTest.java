package ru.zagvladimir.tgbot.telegram.handler;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.domain.layout.LayoutConverter;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

class KeyboardLayoutCommandHandlerTest {

    private static final long CHAT_ID = 100L;

    private final MessageSender sender = mock(MessageSender.class);
    private final KeyboardLayoutCommandHandler handler =
            new KeyboardLayoutCommandHandler(new LayoutConverter(), sender);

    @Test
    void convertsTextPassedAsArgument() {
        handler.handle(context("ghbdtn", null));

        verify(sender).sendText(eq(CHAT_ID), eq("привет"));
    }

    @Test
    void convertsRepliedMessageWhenArgumentIsMissing() {
        handler.handle(context("", "rfr ltkf"));

        verify(sender).sendText(eq(CHAT_ID), eq("как дела"));
    }

    @Test
    void prefersExplicitArgumentOverReply() {
        handler.handle(context("ghbdtn", "rfr ltkf"));

        verify(sender).sendText(eq(CHAT_ID), eq("привет"));
    }

    @Test
    void explainsUsageWhenThereIsNothingToConvert() {
        handler.handle(context("", null));

        verify(sender).sendText(eq(CHAT_ID), contains("ответьте этой командой"));
    }

    private static CommandContext context(String arguments, String replyToText) {
        return new CommandContext(CHAT_ID, 42L, "/kb", arguments, replyToText, false);
    }
}
