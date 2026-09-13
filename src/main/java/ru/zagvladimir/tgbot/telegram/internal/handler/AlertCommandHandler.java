package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.subscription.RateAlertCondition;
import ru.zagvladimir.tgbot.subscription.RateAlertService;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;

@Component
class AlertCommandHandler implements CommandHandler {

    private static final String USAGE =
            """
            Уведомление о курсе:
            /alert usd > 3.30
            /alert eur < 3.80
            /alert usd change 1

            Список — /alerts, удалить — /unalert <id>""";

    private final RateAlertService alerts;
    private final ChatAdminChecker adminChecker;
    private final MessageSender sender;

    AlertCommandHandler(RateAlertService alerts, ChatAdminChecker adminChecker, MessageSender sender) {
        this.alerts = alerts;
        this.adminChecker = adminChecker;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/alert");
    }

    @Override
    public String description() {
        return "Уведомить при достижении курса";
    }

    @Override
    public void handle(CommandContext context) {
        if (!adminChecker.canManage(context.chatId(), context.userId(), context.fromGroup())) {
            sender.sendText(context.chatId(), "Менять уведомления в группе могут только администраторы.");
            return;
        }

        if (!context.hasArguments()) {
            sender.sendText(context.chatId(), USAGE);
            return;
        }

        var request = AlertRequest.parse(context.arguments()).orElse(null);
        if (request == null) {
            sender.sendText(context.chatId(), USAGE);
            return;
        }

        var alert = alerts.create(
                context.chatId(),
                context.userId(),
                request.currency(),
                request.condition(),
                request.threshold(),
                false);

        sender.sendText(
                context.chatId(),
                "Буду следить за %s (%s %s). Удалить — /unalert %d"
                        .formatted(
                                alert.currency(),
                                describe(request.condition()),
                                request.threshold().toPlainString(),
                                alert.id()));
    }

    private static String describe(RateAlertCondition condition) {
        return switch (condition) {
            case ABOVE -> "выше";
            case BELOW -> "ниже";
            case CHANGE_PERCENT -> "изменение от";
        };
    }
}
