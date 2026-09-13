package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.CommandContext;
import ru.zagvladimir.tgbot.telegram.internal.CommandHandler;
import ru.zagvladimir.tgbot.weather.WeatherService;

@Component
class SetCityCommandHandler implements CommandHandler {

    private final WeatherService weatherService;
    private final ChatSettingsService settings;
    private final MessageSender sender;

    SetCityCommandHandler(WeatherService weatherService, ChatSettingsService settings, MessageSender sender) {
        this.weatherService = weatherService;
        this.settings = settings;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/setcity");
    }

    @Override
    public String description() {
        return "Запомнить город для этого чата";
    }

    @Override
    public void handle(CommandContext context) {
        if (!context.hasArguments()) {
            sender.sendText(context.chatId(), "Напишите город: /setcity Минск");
            return;
        }

        var query = context.arguments();
        weatherService
                .findPlace(query)
                .ifPresentOrElse(
                        place -> {
                            settings.setDefaultCity(context.chatId(), place.name());
                            sender.sendText(
                                    context.chatId(),
                                    "Запомнил: " + place.fullName() + ". Теперь /w работает без города.");
                        },
                        () -> sender.sendText(context.chatId(), "Не нашёл такой город: " + query));
    }
}
