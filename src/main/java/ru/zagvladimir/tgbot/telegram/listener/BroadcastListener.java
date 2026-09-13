package ru.zagvladimir.tgbot.telegram.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.currency.CurrencyService;
import ru.zagvladimir.tgbot.domain.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.domain.weather.WeatherService;
import ru.zagvladimir.tgbot.subscription.event.SubscriptionDue;
import ru.zagvladimir.tgbot.telegram.format.CurrencyFormatter;
import ru.zagvladimir.tgbot.telegram.format.WeatherFormatter;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class BroadcastListener {

    private static final Logger log = LoggerFactory.getLogger(BroadcastListener.class);

    private final WeatherService weatherService;
    private final CurrencyService currencyService;
    private final ChatSettingsService settings;
    private final WeatherFormatter weatherFormatter;
    private final CurrencyFormatter currencyFormatter;
    private final MessageSender sender;

    BroadcastListener(
            WeatherService weatherService,
            CurrencyService currencyService,
            ChatSettingsService settings,
            WeatherFormatter weatherFormatter,
            CurrencyFormatter currencyFormatter,
            MessageSender sender) {
        this.weatherService = weatherService;
        this.currencyService = currencyService;
        this.settings = settings;
        this.weatherFormatter = weatherFormatter;
        this.currencyFormatter = currencyFormatter;
        this.sender = sender;
    }

    @ApplicationModuleListener
    public void onSubscriptionDue(SubscriptionDue event) {
        log.info("Рассылка {} в чат {}", event.type(), event.chatId());

        var text =
                switch (event.type()) {
                    case WEATHER -> weatherBlock(event.chatId(), event.payload());
                    case RATES -> ratesBlock(event.chatId(), event.payload());
                    case DIGEST -> digest(event.chatId(), event.payload());
                };

        sender.sendHtml(event.chatId(), text);
    }

    private String digest(long chatId, String payload) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<String>> tasks = List.of(() -> weatherBlock(chatId, payload), () -> ratesBlock(chatId, ""));

            var blocks = new ArrayList<String>(tasks.size());
            for (var future : executor.invokeAll(tasks)) {
                blocks.add(future.get());
            }

            return String.join(System.lineSeparator() + System.lineSeparator(), blocks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Не удалось собрать сводку.";
        } catch (Exception e) {
            log.error("Не удалось собрать дайджест для чата {}", chatId, e);
            return "Не удалось собрать сводку.";
        }
    }

    private String weatherBlock(long chatId, String payload) {
        var city = payload.isBlank() ? settings.find(chatId).defaultCity() : payload;
        if (city == null || city.isBlank()) {
            return "Город не задан — пришлите /setcity Минск";
        }

        try {
            return weatherService
                    .forecastFor(city)
                    .map(weatherFormatter::format)
                    .orElse("Не нашёл город " + city);
        } catch (Exception e) {
            log.warn("Блок погоды для чата {} не собрался: {}", chatId, e.toString());
            return "Погода сейчас недоступна.";
        }
    }

    private String ratesBlock(long chatId, String payload) {
        var codes = payload.isBlank() ? settings.find(chatId).defaultCurrencies() : List.of(payload.split(","));

        try {
            var snapshot = currencyService.latestRates().orElse(null);
            if (snapshot == null) {
                return "Курсы сейчас недоступны.";
            }
            return currencyFormatter.formatRates(currencyService.ratesWithDelta(codes), snapshot.onDate());
        } catch (Exception e) {
            log.warn("Блок курсов для чата {} не собрался: {}", chatId, e.toString());
            return "Курсы сейчас недоступны.";
        }
    }
}
