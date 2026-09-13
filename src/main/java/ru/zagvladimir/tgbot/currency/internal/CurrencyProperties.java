package ru.zagvladimir.tgbot.currency.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.currency")
record CurrencyProperties(String baseUrl, Duration timeout) {

    CurrencyProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.nbrb.by";
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(10);
        }
    }
}
