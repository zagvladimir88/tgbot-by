package ru.zagvladimir.tgbot.telegram.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.subscription.SubscriptionService;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class UnsubscribeCommandHandler implements CommandHandler {

    private final SubscriptionService subscriptions;
    private final ChatAdminChecker adminChecker;
    private final MessageSender sender;

    UnsubscribeCommandHandler(SubscriptionService subscriptions, ChatAdminChecker adminChecker, MessageSender sender) {
        this.subscriptions = subscriptions;
        this.adminChecker = adminChecker;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/unsubscribe");
    }

    @Override
    public String description() {
        return "Отписаться: /unsubscribe 1";
    }

    @Override
    public void handle(CommandContext context) {
        if (!adminChecker.canManage(context.chatId(), context.userId(), context.fromGroup())) {
            sender.sendText(context.chatId(), "Менять подписки в группе могут только администраторы.");
            return;
        }

        long id;
        try {
            id = Long.parseLong(context.arguments().trim());
        } catch (NumberFormatException e) {
            sender.sendText(context.chatId(), "Укажите номер подписки: /unsubscribe 1. Список — /subs");
            return;
        }

        var removed = subscriptions.unsubscribe(context.chatId(), id);
        sender.sendText(context.chatId(), removed ? "Подписка #" + id + " удалена" : "Подписки #" + id + " тут нет");
    }
}
