package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.subscription.SubscriptionService;
import ru.zagvladimir.tgbot.subscription.SubscriptionType;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;

@Component
class SubscribeCommandHandler implements CommandHandler {

    private static final String USAGE =
            """
            Как подписаться:
            /subscribe weather 09:00 Минск
            /subscribe rates 10:00 USD,EUR
            /subscribe digest 09:00

            Список — /subs, отписаться — /unsubscribe <id>""";

    private final SubscriptionService subscriptions;
    private final ChatSettingsService settings;
    private final ChatAdminChecker adminChecker;
    private final MessageSender sender;

    SubscribeCommandHandler(
            SubscriptionService subscriptions,
            ChatSettingsService settings,
            ChatAdminChecker adminChecker,
            MessageSender sender) {
        this.subscriptions = subscriptions;
        this.settings = settings;
        this.adminChecker = adminChecker;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/subscribe");
    }

    @Override
    public String description() {
        return "Подписать чат на рассылку по расписанию";
    }

    @Override
    public void handle(CommandContext context) {
        if (!adminChecker.canManage(context.chatId(), context.userId(), context.fromGroup())) {
            sender.sendText(context.chatId(), "Менять подписки в группе могут только администраторы.");
            return;
        }

        if (!context.hasArguments()) {
            sender.sendText(context.chatId(), USAGE);
            return;
        }

        var parts = context.arguments().trim().split("\\s+", 3);
        var type = parseType(parts[0]);
        if (type == null) {
            sender.sendText(context.chatId(), USAGE);
            return;
        }

        var time = parts.length > 1 ? parseTime(parts[1]) : Optional.<LocalTime>empty();
        if (time.isEmpty()) {
            sender.sendText(context.chatId(), "Не понял время. Пример: /subscribe rates 10:00 USD,EUR");
            return;
        }

        var payload = parts.length > 2 ? parts[2].trim() : "";
        var zoneId = settings.find(context.chatId()).zoneId();
        var cron = toCron(time.get());

        var subscription = subscriptions.subscribe(context.chatId(), context.userId(), type, payload, cron, zoneId);

        sender.sendText(
                context.chatId(),
                "Готово. Подписка #%d: %s в %s (%s). Отписаться — /unsubscribe %d"
                        .formatted(
                                subscription.id(),
                                type.name().toLowerCase(Locale.ROOT),
                                time.get(),
                                zoneId.getId(),
                                subscription.id()));
    }

    static String toCron(LocalTime time) {
        return "0 %d %d * * *".formatted(time.getMinute(), time.getHour());
    }

    @Nullable
    static SubscriptionType parseType(String raw) {
        try {
            return SubscriptionType.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static Optional<LocalTime> parseTime(String raw) {
        try {
            return Optional.of(LocalTime.parse(raw));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
