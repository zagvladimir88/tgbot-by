package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.layout.LayoutConverter;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;

@Component
class KeyboardLayoutCommandHandler implements CommandHandler {

    private final LayoutConverter converter;
    private final MessageSender sender;

    KeyboardLayoutCommandHandler(LayoutConverter converter, MessageSender sender) {
        this.converter = converter;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/kb");
    }

    @Override
    public String description() {
        return "Исправить текст, набранный не в той раскладке";
    }

    @Override
    public void handle(CommandContext context) {
        var source = context.argumentsOrReply();
        if (source == null || source.isBlank()) {
            sender.sendText(
                    context.chatId(),
                    "Напишите /kb и текст либо ответьте этой командой на сообщение, которое нужно исправить.");
            return;
        }

        sender.sendText(context.chatId(), converter.convert(source));
    }
}
