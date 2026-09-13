package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.subscription.RateAlertService;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;

@Component
class UnalertCommandHandler implements CommandHandler {

    private final RateAlertService alerts;
    private final ChatAdminChecker adminChecker;
    private final MessageSender sender;

    UnalertCommandHandler(RateAlertService alerts, ChatAdminChecker adminChecker, MessageSender sender) {
        this.alerts = alerts;
        this.adminChecker = adminChecker;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/unalert");
    }

    @Override
    public String description() {
        return "Удалить уведомление: /unalert 1";
    }

    @Override
    public void handle(CommandContext context) {
        if (!adminChecker.canManage(context.chatId(), context.userId(), context.fromGroup())) {
            sender.sendText(context.chatId(), "Менять уведомления в группе могут только администраторы.");
            return;
        }

        long id;
        try {
            id = Long.parseLong(context.arguments().trim());
        } catch (NumberFormatException e) {
            sender.sendText(context.chatId(), "Укажите номер: /unalert 1. Список — /alerts");
            return;
        }

        var removed = alerts.remove(context.chatId(), id);
        sender.sendText(context.chatId(), removed ? "Уведомление #" + id + " удалено" : "Такого уведомления нет");
    }
}
