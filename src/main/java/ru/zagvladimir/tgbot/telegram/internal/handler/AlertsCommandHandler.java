package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.subscription.RateAlert;
import ru.zagvladimir.tgbot.subscription.RateAlertService;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;

@Component
class AlertsCommandHandler implements CommandHandler {

    private final RateAlertService alerts;
    private final MessageSender sender;

    AlertsCommandHandler(RateAlertService alerts, MessageSender sender) {
        this.alerts = alerts;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/alerts");
    }

    @Override
    public String description() {
        return "Список уведомлений о курсе";
    }

    @Override
    public void handle(CommandContext context) {
        var list = alerts.forChat(context.chatId());
        if (list.isEmpty()) {
            sender.sendText(context.chatId(), "Уведомлений нет. Добавить — /alert usd > 3.30");
            return;
        }

        sender.sendText(
                context.chatId(),
                list.stream().map(AlertsCommandHandler::describe).collect(Collectors.joining("\n")));
    }

    private static String describe(RateAlert alert) {
        var condition =
                switch (alert.condition()) {
                    case ABOVE -> "выше " + alert.threshold().toPlainString();
                    case BELOW -> "ниже " + alert.threshold().toPlainString();
                    case CHANGE_PERCENT -> "изменение от " + alert.threshold().toPlainString() + "%";
                };

        return "#%d %s %s%s".formatted(alert.id(), alert.currency(), condition, alert.enabled() ? "" : " (выключено)");
    }
}
