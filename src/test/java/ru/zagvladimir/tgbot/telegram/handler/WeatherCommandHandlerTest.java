package ru.zagvladimir.tgbot.telegram.handler;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.domain.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.domain.settings.model.ChatSettings;
import ru.zagvladimir.tgbot.domain.weather.WeatherService;
import ru.zagvladimir.tgbot.domain.weather.model.CurrentWeather;
import ru.zagvladimir.tgbot.domain.weather.model.DailyForecast;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;
import ru.zagvladimir.tgbot.domain.weather.model.Place;
import ru.zagvladimir.tgbot.domain.weather.model.WeatherCondition;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.format.WeatherFormatter;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

class WeatherCommandHandlerTest {

    private static final long CHAT_ID = 100L;

    private final WeatherService weatherService = mock(WeatherService.class);
    private final ChatSettingsService settings = mock(ChatSettingsService.class);
    private final MessageSender sender = mock(MessageSender.class);
    private final WeatherCommandHandler handler =
            new WeatherCommandHandler(weatherService, settings, new WeatherFormatter(), sender);

    @Test
    void usesCityFromArguments() {
        when(weatherService.forecastFor("Гродно")).thenReturn(Optional.of(forecast()));

        handler.handle(context("Гродно"));

        verify(sender).sendHtml(eq(CHAT_ID), contains("Минск"));
        verify(settings, never()).find(anyLong());
    }

    @Test
    void fallsBackToSavedCityWhenArgumentsAreEmpty() {
        when(settings.find(CHAT_ID))
                .thenReturn(new ChatSettings(CHAT_ID, "Брест", List.of("USD"), ZoneId.of("Europe/Minsk")));
        when(weatherService.forecastFor("Брест")).thenReturn(Optional.of(forecast()));

        handler.handle(context(""));

        verify(weatherService).forecastFor("Брест");
    }

    @Test
    void asksForCityWhenNothingIsKnown() {
        when(settings.find(CHAT_ID)).thenReturn(ChatSettings.defaults(CHAT_ID));

        handler.handle(context(""));

        verify(sender).sendText(eq(CHAT_ID), contains("/setcity"));
        verify(weatherService, never()).forecastFor(anyString());
    }

    @Test
    void reportsUnknownCity() {
        when(weatherService.forecastFor("Нетакогогорода")).thenReturn(Optional.empty());

        handler.handle(context("Нетакогогорода"));

        verify(sender).sendText(eq(CHAT_ID), contains("Не нашёл такой город"));
    }

    @Test
    void answersToBothCommandAliases() {
        org.assertj.core.api.Assertions.assertThat(handler.commands()).containsExactlyInAnyOrder("/w", "/weather");
    }

    private static CommandContext context(String arguments) {
        return new CommandContext(CHAT_ID, 42L, "/w", arguments, null, false);
    }

    private static Forecast forecast() {
        return new Forecast(
                new Place("Минск", "Беларусь", "Минск", 53.9, 27.57, ZoneId.of("Europe/Minsk")),
                new CurrentWeather(17.9, 16.0, 50, 8.6, WeatherCondition.OVERCAST),
                List.of(new DailyForecast(LocalDate.of(2026, 9, 13), 10.8, 18.7, WeatherCondition.OVERCAST)));
    }
}
