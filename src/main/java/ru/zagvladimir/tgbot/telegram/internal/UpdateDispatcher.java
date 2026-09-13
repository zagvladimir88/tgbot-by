package ru.zagvladimir.tgbot.telegram.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.zagvladimir.tgbot.telegram.MessageSender;

@Component
class UpdateDispatcher implements LongPollingUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

    private final ExecutorService executor;
    private final CommandRegistry registry;
    private final MessageSender sender;

    UpdateDispatcher(
            @Qualifier("botUpdateExecutor") ExecutorService executor, CommandRegistry registry, MessageSender sender) {
        this.executor = executor;
        this.registry = registry;
        this.sender = sender;
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> executor.execute(() -> process(update)));
    }

    void process(Update update) {
        var request = BotRequests.from(update).orElse(null);
        if (request == null) {
            return;
        }

        var context = BotContext.of(update.getUpdateId(), request);
        ScopedValue.where(BotContext.CURRENT, context).run(() -> dispatchWithDiagnosticContext(context, request));
    }

    private void dispatchWithDiagnosticContext(BotContext context, BotRequest request) {
        MDC.put("updateId", String.valueOf(context.updateId()));
        MDC.put("userId", String.valueOf(context.userId()));
        if (context.chatId() != null) {
            MDC.put("chatId", String.valueOf(context.chatId()));
        }

        try {
            dispatch(request);
        } catch (Exception e) {
            log.error("Не удалось обработать апдейт", e);
            reportFailure(context);
        } finally {
            MDC.clear();
        }
    }

    private void dispatch(BotRequest request) {
        switch (request) {
            case BotRequest.Command command -> handleCommand(command);
            case BotRequest.Inline inline -> log.debug("Inline-запрос ещё не поддерживается: {}", inline.query());
            case BotRequest.Callback callback -> log.debug("Callback ещё не поддерживается: {}", callback.data());
        }
    }

    private void handleCommand(BotRequest.Command command) {
        var handler = registry.find(command.command()).orElse(null);
        if (handler == null) {
            if (!command.fromGroup()) {
                sender.sendText(command.chatId(), "Не знаю такой команды. Что я умею — /help");
            }
            return;
        }

        handler.handle(CommandContext.from(command));
    }

    private void reportFailure(BotContext context) {
        var chatId = context.chatId();
        if (chatId == null) {
            return;
        }

        try {
            sender.sendText(chatId, "Что-то пошло не так, уже разбираюсь. Попробуйте ещё раз чуть позже.");
        } catch (Exception e) {
            log.error("Не удалось сообщить об ошибке в чат {}", chatId, e);
        }
    }
}
