package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.currency.CurrencyService;
import ru.zagvladimir.tgbot.currency.RateSnapshot;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;
import ru.zagvladimir.tgbot.telegram.internal.format.CurrencyFormatter;

@Component
class ConvertCommandHandler implements CommandHandler {

    private final CurrencyService currencyService;
    private final CurrencyFormatter formatter;
    private final MessageSender sender;

    ConvertCommandHandler(CurrencyService currencyService, CurrencyFormatter formatter, MessageSender sender) {
        this.currencyService = currencyService;
        this.formatter = formatter;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/conv", "/convert");
    }

    @Override
    public String description() {
        return "Конвертер валют: /conv 100 usd eur";
    }

    @Override
    public void handle(CommandContext context) {
        if (!context.hasArguments()) {
            sender.sendText(context.chatId(), "Пример: /conv 100 usd eur или /conv 100 usd");
            return;
        }

        var request = ConversionRequest.parse(context.arguments(), RateSnapshot.BASE_CURRENCY)
                .orElse(null);
        if (request == null) {
            sender.sendText(context.chatId(), "Не понял запрос. Пример: /conv 100 usd eur");
            return;
        }

        currencyService
                .convert(request.amount(), request.from(), request.to())
                .ifPresentOrElse(
                        conversion -> sender.sendHtml(context.chatId(), formatter.formatConversion(conversion)),
                        () -> sender.sendText(
                                context.chatId(), "Не знаю такую валюту: " + request.from() + " или " + request.to()));
    }
}
