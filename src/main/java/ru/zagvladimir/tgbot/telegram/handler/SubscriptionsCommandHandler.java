package ru.zagvladimir.tgbot.telegram.handler;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.subscription.SubscriptionService;
import ru.zagvladimir.tgbot.subscription.model.Subscription;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class SubscriptionsCommandHandler implements CommandHandler {

    private static final DateTimeFormatter NEXT_RUN = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final SubscriptionService subscriptions;
    private final MessageSender sender;

    SubscriptionsCommandHandler(SubscriptionService subscriptions, MessageSender sender) {
        this.subscriptions = subscriptions;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/subs");
    }

    @Override
    public String description() {
        return "Список подписок этого чата";
    }

    @Override
    public void handle(CommandContext context) {
        var list = subscriptions.forChat(context.chatId());
        if (list.isEmpty()) {
            sender.sendText(context.chatId(), "Подписок нет. Добавить — /subscribe");
            return;
        }

        var text = list.stream().map(SubscriptionsCommandHandler::describe).collect(Collectors.joining("\n"));
        sender.sendText(context.chatId(), text);
    }

    private static String describe(Subscription subscription) {
        var zone = subscription.zoneId();
        var next = NEXT_RUN.format(subscription.nextRunAt().atZone(zone));
        var payload = subscription.payload().isBlank() ? "" : " (" + subscription.payload() + ")";

        return "#%d %s%s — следующая отправка %s %s"
                .formatted(
                        subscription.id(),
                        subscription.type().name().toLowerCase(Locale.ROOT),
                        payload,
                        next,
                        zoneLabel(zone));
    }

    private static String zoneLabel(ZoneId zone) {
        return zone.getId();
    }
}
