package ru.zagvladimir.tgbot.app.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.currency")
public record CurrencyProperties(String baseUrl, Duration timeout) {

    public CurrencyProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.nbrb.by";
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(10);
        }
    }
}
