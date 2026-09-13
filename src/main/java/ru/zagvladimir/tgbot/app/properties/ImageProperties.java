package ru.zagvladimir.tgbot.app.properties;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.image")
public record ImageProperties(
        String baseUrl, @Nullable String apiKey, @Nullable String cx, int dailyLimit, int pageSize, Duration timeout) {

    public ImageProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://www.googleapis.com";
        }
        if (dailyLimit <= 0) {
            dailyLimit = 100;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        if (timeout == null) {
            timeout = Duration.ofSeconds(8);
        }
    }

    public boolean configured() {
        return apiKey != null && !apiKey.isBlank() && cx != null && !cx.isBlank();
    }
}
