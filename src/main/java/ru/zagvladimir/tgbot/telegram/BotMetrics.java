package ru.zagvladimir.tgbot.telegram;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class BotMetrics {

    private final MeterRegistry registry;
    private final Map<String, Counter> commandCounters = new ConcurrentHashMap<>();

    BotMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void commandHandled(String command) {
        commandCounters
                .computeIfAbsent(command, name -> Counter.builder("bot.commands")
                        .tag("command", name)
                        .description("Обработанные команды бота")
                        .register(registry))
                .increment();
    }

    public void commandFailed(String command) {
        commandCounters
                .computeIfAbsent("failed:" + command, name -> Counter.builder("bot.command.failures")
                        .tag("command", command)
                        .description("Команды, завершившиеся ошибкой")
                        .register(registry))
                .increment();
    }

    public void unknownCommand() {
        commandCounters
                .computeIfAbsent("unknown", name -> Counter.builder("bot.commands.unknown")
                        .description("Неизвестные команды")
                        .register(registry))
                .increment();
    }
}
