package ru.zagvladimir.tgbot.telegram.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.domain.currency.CurrencyService;
import ru.zagvladimir.tgbot.domain.currency.model.Conversion;
import ru.zagvladimir.tgbot.domain.currency.model.CurrencyRate;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.domain.currency.model.RateWithDelta;
import ru.zagvladimir.tgbot.domain.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.domain.settings.model.ChatSettings;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.format.CurrencyFormatter;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

class CurrencyCommandHandlersTest {

    private static final long CHAT_ID = 100L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 13);

    private final CurrencyService currencyService = mock(CurrencyService.class);
    private final ChatSettingsService settings = mock(ChatSettingsService.class);
    private final MessageSender sender = mock(MessageSender.class);
    private final CurrencyFormatter formatter = new CurrencyFormatter();

    private final RatesCommandHandler rates = new RatesCommandHandler(currencyService, settings, formatter, sender);
    private final ConvertCommandHandler convert = new ConvertCommandHandler(currencyService, formatter, sender);

    @Test
    void ratesUsesChatDefaultsWhenNoArguments() {
        when(settings.find(CHAT_ID))
                .thenReturn(new ChatSettings(CHAT_ID, null, List.of("USD"), ZoneId.of("Europe/Minsk")));
        when(currencyService.latestRates()).thenReturn(Optional.of(snapshot()));
        when(currencyService.ratesWithDelta(List.of("USD")))
                .thenReturn(List.of(new RateWithDelta(usd(), new BigDecimal("0.0041"))));

        rates.handle(context("/rate", ""));

        verify(sender).sendHtml(eq(CHAT_ID), contains("USD"));
    }

    @Test
    void ratesSplitsArgumentsByCommaAndWhitespace() {
        when(currencyService.latestRates()).thenReturn(Optional.of(snapshot()));
        when(currencyService.ratesWithDelta(anyList())).thenReturn(List.of(new RateWithDelta(usd(), null)));

        rates.handle(context("/rate", "usd, eur  pln"));

        verify(currencyService).ratesWithDelta(List.of("usd", "eur", "pln"));
    }

    @Test
    void ratesReportsUnavailableSource() {
        when(settings.find(CHAT_ID)).thenReturn(ChatSettings.defaults(CHAT_ID));
        when(currencyService.latestRates()).thenReturn(Optional.empty());

        rates.handle(context("/rate", ""));

        verify(sender).sendText(eq(CHAT_ID), contains("недоступны"));
    }

    @Test
    void ratesReportsUnknownCurrencies() {
        when(currencyService.latestRates()).thenReturn(Optional.of(snapshot()));
        when(currencyService.ratesWithDelta(anyList())).thenReturn(List.of());

        rates.handle(context("/rate", "xxx"));

        verify(sender).sendText(eq(CHAT_ID), contains("Не знаю таких валют"));
    }

    @Test
    void convertShowsUsageWithoutArguments() {
        convert.handle(context("/conv", ""));

        verify(sender).sendText(eq(CHAT_ID), contains("Пример"));
        verify(currencyService, never()).convert(any(), anyString(), anyString());
    }

    @Test
    void convertRejectsGarbage() {
        convert.handle(context("/conv", "абракадабра"));

        verify(sender).sendText(eq(CHAT_ID), contains("Не понял запрос"));
    }

    @Test
    void convertRendersResult() {
        when(currencyService.convert(new BigDecimal("100"), "USD", "EUR"))
                .thenReturn(Optional.of(
                        new Conversion(new BigDecimal("100"), "USD", "EUR", new BigDecimal("85.4967"), DATE)));

        convert.handle(context("/conv", "100 usd eur"));

        verify(sender).sendHtml(eq(CHAT_ID), contains("85.50 EUR"));
    }

    @Test
    void convertReportsUnknownCurrency() {
        when(currencyService.convert(new BigDecimal("100"), "USD", "XXX")).thenReturn(Optional.empty());

        convert.handle(context("/conv", "100 usd xxx"));

        verify(sender).sendText(eq(CHAT_ID), contains("Не знаю такую валюту"));
    }

    private static CommandContext context(String command, String arguments) {
        return new CommandContext(CHAT_ID, 42L, command, arguments, null, false);
    }

    private static CurrencyRate usd() {
        return new CurrencyRate("USD", "Доллар США", new BigDecimal("3.2841"));
    }

    private static RateSnapshot snapshot() {
        return new RateSnapshot(DATE, Instant.parse("2026-09-13T10:00:00Z"), Map.of("USD", usd()));
    }
}
