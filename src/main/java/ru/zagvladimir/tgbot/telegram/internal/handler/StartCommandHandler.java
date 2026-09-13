package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;

@Component
class StartCommandHandler implements CommandHandler {

    private final MessageSender sender;

    StartCommandHandler(MessageSender sender) {
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/start");
    }

    @Override
    public String description() {
        return "Начать работу с ботом";
    }

    @Override
    public void handle(CommandContext context) {
        sender.sendText(
                context.chatId(),
                """
                Привет. Я умею:

                • конвертировать текст, набранный не в той раскладке
                • показывать погоду
                • показывать курсы НБРБ и конвертировать валюты
                • искать картинки
                • присылать погоду и курсы в чат по расписанию

                Полный список команд — /help""");
    }
}
