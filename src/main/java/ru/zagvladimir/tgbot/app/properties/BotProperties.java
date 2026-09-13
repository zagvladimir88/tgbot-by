package ru.zagvladimir.tgbot.app.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.telegram")
public record BotProperties(String token, String username) {

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }
}
