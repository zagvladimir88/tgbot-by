package ru.zagvladimir.tgbot.settings;

import java.time.ZoneId;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record ChatSettings(long chatId, @Nullable String defaultCity, List<String> defaultCurrencies, ZoneId zoneId) {

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Minsk");
    public static final List<String> DEFAULT_CURRENCIES = List.of("USD", "EUR", "RUB");

    public static ChatSettings defaults(long chatId) {
        return new ChatSettings(chatId, null, DEFAULT_CURRENCIES, DEFAULT_ZONE);
    }
}
