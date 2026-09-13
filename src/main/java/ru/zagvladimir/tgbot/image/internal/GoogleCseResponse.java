package ru.zagvladimir.tgbot.image.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
record GoogleCseResponse(@Nullable List<Item> items, @Nullable Queries queries) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Item(@Nullable String title, @Nullable String link, @Nullable Image image) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Image(
            @Nullable String contextLink,
            @Nullable String thumbnailLink,
            @Nullable Integer width,
            @Nullable Integer height) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Queries(@Nullable List<Page> nextPage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Page(@Nullable Integer startIndex) {}
}
