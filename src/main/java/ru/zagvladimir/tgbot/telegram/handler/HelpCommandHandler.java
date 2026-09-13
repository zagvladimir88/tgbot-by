package ru.zagvladimir.tgbot.telegram.handler;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.command.CommandRegistry;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class HelpCommandHandler implements CommandHandler {

    private final MessageSender sender;
    private final ObjectProvider<CommandRegistry> registry;

    HelpCommandHandler(MessageSender sender, ObjectProvider<CommandRegistry> registry) {
        this.sender = sender;
        this.registry = registry;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/help");
    }

    @Override
    public String description() {
        return "Список команд";
    }

    @Override
    public void handle(CommandContext context) {
        var text = registry.getObject().handlers().stream()
                .filter(CommandHandler::listedInMenu)
                .flatMap(handler -> handler.commands().stream()
                        .map(command -> new CommandDescription(command, handler.description())))
                .sorted(Comparator.comparing(CommandDescription::command))
                .map(entry -> entry.command() + " — " + entry.description())
                .collect(Collectors.joining("\n"));

        sender.sendText(context.chatId(), text.isEmpty() ? "Команд пока нет" : text);
    }

    private record CommandDescription(String command, String description) {}
}
