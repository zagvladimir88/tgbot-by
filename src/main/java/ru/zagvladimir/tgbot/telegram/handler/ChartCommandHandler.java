package ru.zagvladimir.tgbot.telegram.handler;

import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.currency.CurrencyService;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.format.RateChartRenderer;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class ChartCommandHandler implements CommandHandler {

    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 365;

    private final CurrencyService currencyService;
    private final RateChartRenderer renderer;
    private final MessageSender sender;

    ChartCommandHandler(CurrencyService currencyService, RateChartRenderer renderer, MessageSender sender) {
        this.currencyService = currencyService;
        this.renderer = renderer;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/chart");
    }

    @Override
    public String description() {
        return "График курса: /chart usd 30";
    }

    @Override
    public void handle(CommandContext context) {
        if (!context.hasArguments()) {
            sender.sendText(context.chatId(), "Пример: /chart usd 30");
            return;
        }

        var parts = context.arguments().trim().split("\\s+");
        var code = parts[0].toUpperCase(Locale.ROOT);
        var days = parseDays(parts);

        var history = currencyService.history(code, days);
        if (history.size() < 2) {
            sender.sendText(context.chatId(), "Нет данных для графика по " + code);
            return;
        }

        var image = renderer.render(code, history);
        sender.sendPhoto(
                context.chatId(),
                image,
                "chart-%s-%d.png".formatted(code.toLowerCase(Locale.ROOT), days),
                "<b>%s</b> за %d дн. по курсам НБРБ".formatted(code, days));
    }

    public static int parseDays(String[] parts) {
        if (parts.length < 2) {
            return DEFAULT_DAYS;
        }

        try {
            var requested = Integer.parseInt(parts[1]);
            return Math.clamp(requested, 2, MAX_DAYS);
        } catch (NumberFormatException e) {
            return DEFAULT_DAYS;
        }
    }
}
