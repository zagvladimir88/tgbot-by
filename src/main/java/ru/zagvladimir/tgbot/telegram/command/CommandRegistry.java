package ru.zagvladimir.tgbot.telegram.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistry {

    private final Map<String, CommandHandler> byCommand;
    private final List<CommandHandler> handlers;

    public CommandRegistry(List<CommandHandler> handlers) {
        this.handlers = List.copyOf(handlers);
        this.byCommand = index(handlers);
    }

    private static Map<String, CommandHandler> index(List<CommandHandler> handlers) {
        Map<String, CommandHandler> index = new HashMap<>();
        for (var handler : handlers) {
            for (var command : handler.commands()) {
                var previous = index.put(command, handler);
                if (previous != null) {
                    throw new IllegalStateException("Команда %s объявлена дважды: %s и %s"
                            .formatted(
                                    command,
                                    previous.getClass().getSimpleName(),
                                    handler.getClass().getSimpleName()));
                }
            }
        }
        return Map.copyOf(index);
    }

    public Optional<CommandHandler> find(String command) {
        return Optional.ofNullable(byCommand.get(command));
    }

    public List<CommandHandler> handlers() {
        return handlers;
    }
}
