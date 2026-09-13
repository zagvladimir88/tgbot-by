package ru.zagvladimir.tgbot.integration.google.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleCseResponse(@Nullable List<Item> items, @Nullable Queries queries) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(@Nullable String title, @Nullable String link, @Nullable Image image) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(
            @Nullable String contextLink,
            @Nullable String thumbnailLink,
            @Nullable Integer width,
            @Nullable Integer height) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Queries(@Nullable List<Page> nextPage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(@Nullable Integer startIndex) {}
}
