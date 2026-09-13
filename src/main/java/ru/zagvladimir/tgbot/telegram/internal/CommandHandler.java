package ru.zagvladimir.tgbot.telegram.internal;

import java.util.Set;

public interface CommandHandler {

    Set<String> commands();

    String description();

    void handle(CommandContext context);

    default boolean listedInMenu() {
        return true;
    }
}
