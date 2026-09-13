package ru.zagvladimir.tgbot.weather;

import java.time.ZoneId;
import org.jspecify.annotations.Nullable;

public record Place(
        String name,
        @Nullable String country,
        @Nullable String region,
        double latitude,
        double longitude,
        ZoneId zoneId) {

    public String fullName() {
        if (country == null || country.isBlank()) {
            return name;
        }
        return name + ", " + country;
    }
}
