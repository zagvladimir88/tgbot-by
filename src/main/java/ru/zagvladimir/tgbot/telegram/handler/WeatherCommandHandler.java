package ru.zagvladimir.tgbot.telegram.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.domain.weather.WeatherService;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.format.WeatherFormatter;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class WeatherCommandHandler implements CommandHandler {

    private final WeatherService weatherService;
    private final ChatSettingsService settings;
    private final WeatherFormatter formatter;
    private final MessageSender sender;

    WeatherCommandHandler(
            WeatherService weatherService,
            ChatSettingsService settings,
            WeatherFormatter formatter,
            MessageSender sender) {
        this.weatherService = weatherService;
        this.settings = settings;
        this.formatter = formatter;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/w", "/weather");
    }

    @Override
    public String description() {
        return "Погода: /w Минск";
    }

    @Override
    public void handle(CommandContext context) {
        var city = context.hasArguments()
                ? context.arguments()
                : settings.find(context.chatId()).defaultCity();

        if (city == null || city.isBlank()) {
            sender.sendText(context.chatId(), "Укажите город: /w Минск. Чтобы не писать каждый раз — /setcity Минск");
            return;
        }

        weatherService
                .forecastFor(city)
                .ifPresentOrElse(
                        forecast -> sender.sendHtml(context.chatId(), formatter.format(forecast)),
                        () -> sender.sendText(context.chatId(), "Не нашёл такой город: " + city));
    }
}
