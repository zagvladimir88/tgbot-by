package ru.zagvladimir.tgbot.telegram.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
class UpdateDispatcher implements LongPollingUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

    private final ExecutorService executor;
    private final ObjectProvider<TelegramClient> telegramClient;

    UpdateDispatcher(
            @Qualifier("botUpdateExecutor") ExecutorService executor, ObjectProvider<TelegramClient> telegramClient) {
        this.executor = executor;
        this.telegramClient = telegramClient;
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
        } catch (RuntimeException e) {
            log.error("Не удалось обработать апдейт", e);
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
        if (!"/start".equals(command.command())) {
            log.debug("Команда {} ещё не реализована", command.command());
            return;
        }

        send(command.chatId(), "Привет. Бот на связи, команды появятся по мере разработки.");
    }

    private void send(long chatId, String text) {
        var client = telegramClient.getIfAvailable();
        if (client == null) {
            log.warn("Нечем отправить сообщение в чат {}: TelegramClient не сконфигурирован", chatId);
            return;
        }

        try {
            client.execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }
}
