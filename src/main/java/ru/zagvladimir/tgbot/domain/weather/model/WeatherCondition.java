package ru.zagvladimir.tgbot.domain.weather.model;

public enum WeatherCondition {
    CLEAR("Ясно", "☀️"),
    MAINLY_CLEAR("Малооблачно", "🌤"),
    PARTLY_CLOUDY("Переменная облачность", "⛅"),
    OVERCAST("Пасмурно", "☁️"),
    FOG("Туман", "🌫"),
    DRIZZLE("Морось", "🌦"),
    FREEZING_DRIZZLE("Ледяная морось", "🌧"),
    RAIN("Дождь", "🌧"),
    FREEZING_RAIN("Ледяной дождь", "🌧"),
    SNOW("Снег", "🌨"),
    SNOW_GRAINS("Снежная крупа", "🌨"),
    RAIN_SHOWERS("Ливень", "🌦"),
    SNOW_SHOWERS("Снегопад", "🌨"),
    THUNDERSTORM("Гроза", "⛈"),
    THUNDERSTORM_WITH_HAIL("Гроза с градом", "⛈"),
    UNKNOWN("Неизвестно", "❓");

    private final String description;
    private final String emoji;

    WeatherCondition(String description, String emoji) {
        this.description = description;
        this.emoji = emoji;
    }

    public String description() {
        return description;
    }

    public String emoji() {
        return emoji;
    }

    public static WeatherCondition fromWmoCode(int code) {
        return switch (code) {
            case 0 -> CLEAR;
            case 1 -> MAINLY_CLEAR;
            case 2 -> PARTLY_CLOUDY;
            case 3 -> OVERCAST;
            case 45, 48 -> FOG;
            case 51, 53, 55 -> DRIZZLE;
            case 56, 57 -> FREEZING_DRIZZLE;
            case 61, 63, 65 -> RAIN;
            case 66, 67 -> FREEZING_RAIN;
            case 71, 73, 75 -> SNOW;
            case 77 -> SNOW_GRAINS;
            case 80, 81, 82 -> RAIN_SHOWERS;
            case 85, 86 -> SNOW_SHOWERS;
            case 95 -> THUNDERSTORM;
            case 96, 99 -> THUNDERSTORM_WITH_HAIL;
            default -> UNKNOWN;
        };
    }
}
