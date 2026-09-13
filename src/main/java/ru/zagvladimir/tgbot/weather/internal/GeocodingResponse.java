package ru.zagvladimir.tgbot.weather.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

record GeocodingResponse(@Nullable List<Result> results) {

    record Result(
            String name,
            double latitude,
            double longitude,
            @Nullable String country,
            @JsonProperty("country_code") @Nullable String countryCode,
            @Nullable String admin1,
            @Nullable String timezone) {}
}
