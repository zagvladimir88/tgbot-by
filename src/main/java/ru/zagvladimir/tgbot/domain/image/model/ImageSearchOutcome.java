package ru.zagvladimir.tgbot.domain.image.model;

import org.jspecify.annotations.Nullable;

public sealed interface ImageSearchOutcome {

    public record Found(ImageSearchPage page) implements ImageSearchOutcome {}

    public record NothingFound(String query) implements ImageSearchOutcome {}

    public record QuotaExceeded(int dailyLimit) implements ImageSearchOutcome {}

    public record Unavailable(@Nullable String reason) implements ImageSearchOutcome {}
}
