package ru.zagvladimir.tgbot.telegram.command;

import java.util.Set;

public interface CommandHandler {

    public Set<String> commands();

    public String description();

    public void handle(CommandContext context);

    public default boolean listedInMenu() {
        return true;
    }
}
