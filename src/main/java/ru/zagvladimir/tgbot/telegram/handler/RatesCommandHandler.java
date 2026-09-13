package ru.zagvladimir.tgbot.telegram.handler;

import java.util.Arrays;
import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.currency.CurrencyService;
import ru.zagvladimir.tgbot.domain.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.format.CurrencyFormatter;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class RatesCommandHandler implements CommandHandler {

    private final CurrencyService currencyService;
    private final ChatSettingsService settings;
    private final CurrencyFormatter formatter;
    private final MessageSender sender;

    RatesCommandHandler(
            CurrencyService currencyService,
            ChatSettingsService settings,
            CurrencyFormatter formatter,
            MessageSender sender) {
        this.currencyService = currencyService;
        this.settings = settings;
        this.formatter = formatter;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/rate", "/rates");
    }

    @Override
    public String description() {
        return "Курсы НБРБ";
    }

    @Override
    public void handle(CommandContext context) {
        var codes = context.hasArguments()
                ? Arrays.stream(context.arguments().split("[,\s]+"))
                        .filter(code -> !code.isBlank())
                        .toList()
                : settings.find(context.chatId()).defaultCurrencies();

        var snapshot = currencyService.latestRates().orElse(null);
        if (snapshot == null) {
            sender.sendText(context.chatId(), "Курсы пока недоступны, попробуйте позже.");
            return;
        }

        var rates = currencyService.ratesWithDelta(codes);
        if (rates.isEmpty()) {
            sender.sendText(context.chatId(), "Не знаю таких валют: " + String.join(", ", codes));
            return;
        }

        sender.sendHtml(context.chatId(), formatter.formatRates(rates, snapshot.onDate()));
    }
}
