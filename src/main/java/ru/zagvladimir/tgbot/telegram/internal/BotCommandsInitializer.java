package ru.zagvladimir.tgbot.telegram.internal;

import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
class BotCommandsInitializer {

    private static final Logger log = LoggerFactory.getLogger(BotCommandsInitializer.class);

    private final CommandRegistry registry;
    private final ObjectProvider<TelegramClient> telegramClient;

    BotCommandsInitializer(CommandRegistry registry, ObjectProvider<TelegramClient> telegramClient) {
        this.registry = registry;
        this.telegramClient = telegramClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    void publishCommandMenu() {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            return;
        }

        var commands = registry.handlers().stream()
                .filter(CommandHandler::listedInMenu)
                .flatMap(handler ->
                        handler.commands().stream().map(command -> new BotCommand(command, handler.description())))
                .sorted(Comparator.comparing(BotCommand::getCommand))
                .toList();

        try {
            client.execute(SetMyCommands.builder().commands(commands).build());
            log.info("Меню команд обновлено: {} шт.", commands.size());
        } catch (TelegramApiException e) {
            log.error("Не удалось обновить меню команд", e);
        }
    }
}
